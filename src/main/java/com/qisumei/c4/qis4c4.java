package com.qisumei.c4;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.qisumei.c4.block.C4Block;  // ← 导入自定义方块类
import com.qisumei.c4.item.C4Item;
import com.qisumei.c4.sound.ModSounds;

@Mod(qis4c4.MODID)
public class qis4c4 {
    public static final String MODID = "qis4c4";
    private static final Logger LOGGER = LogUtils.getLogger();

    // 注册器
    public static final DeferredRegister<Item> ITEMS  = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(MODID);

    // C4 物品：最大堆叠 1
    public static final DeferredHolder<Item, C4Item> QISC4_ITEM = 
    ITEMS.register("c4", C4Item::new);

    // C4 方块：使用自定义 C4Block 类，并在注册时直接指定属性
    
    public static final DeferredBlock<C4Block> QIS_C4 =
        (DeferredBlock<C4Block>) BLOCKS.register("c4", () -> new C4Block(BlockBehaviour.Properties.of()
        .destroyTime(6.45f)
        .explosionResistance(10.0f)
        .sound(SoundType.GRASS)
        .lightLevel(s -> 7)
        .noOcclusion()  // 允许部分透明，不遮挡视线
    )
    );
    
    public qis4c4(IEventBus modBus) {
        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        ModSounds.register(modBus);
        LOGGER.info("Loaded mod {}", MODID);
    }
}
