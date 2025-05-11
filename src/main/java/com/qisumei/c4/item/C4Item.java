package com.qisumei.c4.item;

import com.qisumei.c4.handler.C4CountdownHandler;
import com.qisumei.c4.qis4c4;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.LivingEntity;

public class C4Item extends Item {
    private static final int USE_DURATION = 70; // 4秒=80ticks
    
    public C4Item() {
        super(new Item.Properties().stacksTo(1));
    }

    // 关键修改1：添加使用动画
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK; // 使用类似盾牌的动画
    }

    // 关键修改2：右键开始使用（不直接放置）
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand); // 开始计时
        return InteractionResultHolder.consume(stack);
    }

    // 关键修改3：使用完成时放置方块
    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof Player player) || world.isClientSide) return;
        
        // 完成时执行放置逻辑
        if (remainingUseTicks == 1) {
            BlockHitResult hitResult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos placePos = hitResult.getBlockPos().relative(hitResult.getDirection());
                
                world.playSound(null, placePos, 
                    SoundEvents.NOTE_BLOCK_HAT.value(),
                    SoundSource.BLOCKS, 0.8f, 0.9f);
                
                world.setBlock(placePos, qis4c4.QIS_C4.get().defaultBlockState(), 11);
                C4CountdownHandler.startCountdown(placePos, (ServerLevel)world, true);
                
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
    }

    @Override
public int getUseDuration(ItemStack stack, LivingEntity user) {
    return USE_DURATION;
}
}