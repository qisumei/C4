package com.qisumei.c4.handler;

import com.qisumei.c4.qis4c4;
import com.qisumei.c4.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = qis4c4.MODID)
public class C4CountdownHandler {
    private static class Countdown {
        int ticksLeft;
        int nextBeepTick;
        BlockPos pos;
        boolean playerPlaced;
        boolean announced20;  // 是否已广播 20 秒提醒
        boolean announced4;   // 是否已广播 4 秒提醒

        Countdown(BlockPos pos, boolean playerPlaced) {
            this.pos = pos;
            this.playerPlaced = playerPlaced;
            this.ticksLeft = 20 * 40; // 40 秒
            this.nextBeepTick = calculateNextInterval(ticksLeft);
            this.announced20 = false;
            this.announced4 = false;
        }
    }

    private static final Map<BlockPos, Countdown> countdowns = new ConcurrentHashMap<>();

    /** 获取剩余 ticks */
    public static int getRemainingTicks(BlockPos pos) {
        Countdown c = countdowns.get(pos);
        return c != null ? c.ticksLeft : -1;
    }

    /** 是否还在倒计时 */
    public static boolean isCounting(BlockPos pos) {
        return countdowns.containsKey(pos);
    }

    /** 启动倒计时并广播“已安放” */
    public static void startCountdown(BlockPos pos, ServerLevel world, boolean playerPlaced) {
        Countdown c = new Countdown(pos, playerPlaced);
        countdowns.put(pos, c);

        // 广播安放消息
        Component placedMsg = Component.literal("§c[警报] 炸弹已在 " + pos.toShortString() + " 安放，40 秒后爆炸！");
        world.getServer().getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(placedMsg));

        playAlarmSound(pos, world);
    }

    /** 拆除时广播并移除倒计时 */
    public static void onBlockDestroyed(BlockPos pos, ServerLevel world, boolean byPlayer) {
        Countdown c = countdowns.remove(pos);
        if (c != null && c.playerPlaced && byPlayer) {
            // 播放拆除音效
            world.playSound(null,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                ModSounds.CTW_SOUND.get(),
                SoundSource.BLOCKS, 1.5f, 1.0f);

            // 广播拆除消息
            Component defusedMsg = Component.literal("§a[信息] 炸弹已被拆除！");
            world.getServer().getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(defusedMsg));
        }
    }

    private static void playAlarmSound(BlockPos pos, ServerLevel world) {
        world.playSound(null,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            ModSounds.ALARM_SOUND(),
            SoundSource.BLOCKS, 1.5f, 1.0f);
    }

    private static int calculateNextInterval(int remainingTicks) {
        float progress = 1 - (remainingTicks / (40f * 20));
        return Math.max(8, (int)(40 * (1 - progress * 0.8f)));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel world = event.getServer().getLevel(event.getServer().overworld().dimension());
        if (world == null) return;

        Iterator<Map.Entry<BlockPos, Countdown>> iter = countdowns.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BlockPos, Countdown> entry = iter.next();
            Countdown c = entry.getValue();

            c.ticksLeft--;

            // 每当到达下一个节拍播放音效
            if (c.ticksLeft <= c.nextBeepTick) {
                playAlarmSound(c.pos, world);
                c.nextBeepTick = c.ticksLeft - calculateNextInterval(c.ticksLeft);
            }

            // 剩余 20 秒时广播一次
            if (!c.announced20 && c.ticksLeft <= 20 * 20) {
                Component msg20 = Component.literal("§e[警告] 还剩 20 秒，快离开炸弹区域！");
                world.getServer().getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(msg20));
                c.announced20 = true;
            }

            // 剩余 4 秒时广播一次
            if (!c.announced4 && c.ticksLeft <= 4 * 20) {
                Component msg4 = Component.literal("§c[紧急] 炸弹就要爆炸啦，快离开那里！");
                world.getServer().getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(msg4));
                c.announced4 = true;
            }

            // 时间到，移除倒计时
            if (c.ticksLeft <= 0) {
                iter.remove();
            }
        }
    }
}
