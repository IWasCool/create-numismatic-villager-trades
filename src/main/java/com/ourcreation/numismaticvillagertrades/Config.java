package com.ourcreation.numismaticvillagertrades;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_COIN_TRADES = BUILDER
            .comment("Whether villagers receive Create: Numismatics coin-based trades.")
            .define("enableCoinTrades", true);

    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA_TRADES = BUILDER
            .comment("Whether coin-based trades replace vanilla villager trades for supported professions.")
            .define("replaceVanillaTrades", true);

    public static final ModConfigSpec.IntValue MAX_USES = BUILDER
            .comment("Maximum uses for each coin-based villager trade.")
            .defineInRange("maxUses", 999, 1, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}
