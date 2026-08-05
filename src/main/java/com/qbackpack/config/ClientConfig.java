package com.qbackpack.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_SORT_BUTTON_TOOLTIP;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SHOW_SORT_BUTTON_TOOLTIP = builder
                .comment("Show the tooltip when hovering over the backpack sort button.")
                .define("showSortButtonTooltip", true);
        SPEC = builder.build();
    }

    private ClientConfig() {}
}
