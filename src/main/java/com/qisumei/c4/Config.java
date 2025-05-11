package com.qisumei.c4;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = qis4c4.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 示例布尔值：是否开启额外效果
    public static final ModConfigSpec.BooleanValue ENABLE_EXTRA =
        BUILDER
            .comment("是否开启示例物品的额外效果")
            .define("enableExtraEffect", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableExtraEffect;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        enableExtraEffect = ENABLE_EXTRA.get();
    }
}