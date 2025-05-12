package com.qisumei.c4.block;

import com.qisumei.c4.handler.C4CountdownHandler;
import com.qisumei.c4.sound.ModSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class C4Block extends Block {
    private static final int DELAY_TICKS = 20 * 40; // 40 秒
    private static final VoxelShape C4_SHAPE = Block.box(0, 0, 0, 15, 5, 12);

    public C4Block(BlockBehaviour.Properties props) {
        super(props);
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return C4_SHAPE;
        }
        // 重写选中/光标高亮时的外形
        @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return C4_SHAPE;
        }
        @Override
        public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
            super.onPlace(state, world, pos, oldState, moved);
            if (!world.isClientSide) {
                if (world instanceof ServerLevel serverLevel) {
                    // 移除旧的倒计时
                    C4CountdownHandler.onBlockDestroyed(pos, serverLevel, false);
                    // 启动新的倒计时
                    C4CountdownHandler.startCountdown(pos, serverLevel, true);
                    world.scheduleTick(pos, this, DELAY_TICKS);
                }
                world.playSound(null, pos, ModSounds.C4_PLACE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (!world.isClientSide && world instanceof ServerLevel serverLevel) {
                C4CountdownHandler.onBlockDestroyed(pos, serverLevel, false);
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }
    @Override
public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
    float baseSpeed = super.getDestroyProgress(state, player, world, pos);
    
    // 检查是否使用拆弹钳（使用新的TagKey创建方式）
    if (player.getMainHandItem().is(Items.SHEARS)) {
        return baseSpeed * 2.0f;
    }
    
    return baseSpeed;
}
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        world.playSound(
            null,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            ModSounds.TW_SOUND.get(),
            SoundSource.BLOCKS,
            5.0f,
            0.8f
        );
        world.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
            20.0f, false, Level.ExplosionInteraction.NONE);
        world.removeBlock(pos, false);

    // 追加额外伤害（范围 10 格内的实体）
    AABB area = new AABB(pos).inflate(10.0); // 爆炸范围扩展 10 格
    world.getEntitiesOfClass(LivingEntity.class, area).forEach(entity -> {
        // 使用原版爆炸伤害类型
        Holder<DamageType> explosionType = world.registryAccess()
            .registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(DamageTypes.EXPLOSION);
        
        DamageSource damageSource = new DamageSource(explosionType, null, null);
        entity.hurt(damageSource, 100.0f); 
    });

    world.removeBlock(pos, false);
}
}