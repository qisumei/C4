package com.qisumei.c4.handler;

import com.qisumei.c4.qis4c4;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = qis4c4.MODID)
public class C4EventHandlers {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().getBlock() == qis4c4.QIS_C4.get()) {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                C4CountdownHandler.onBlockDestroyed(
                    event.getPos(), 
                    serverLevel,
                    true // 标记为玩家破坏
                );
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getPlacedBlock().getBlock() == qis4c4.QIS_C4.get()) {
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                boolean isPlayer = event.getEntity() instanceof Player;
                C4CountdownHandler.startCountdown(
                    event.getPos(),
                    serverLevel,
                    isPlayer
                );
            }
        }
    }
}