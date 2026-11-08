package com.ourcreation.numismaticvillagertrades.compat;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;

public final class VillagerPreviewFactory {
    private static final ResourceLocation REBORN_MALE = ResourceLocation.parse("slimpatch:male_villager");
    private static final ResourceLocation REBORN_FEMALE = ResourceLocation.parse("slimpatch:female_villager");

    private VillagerPreviewFactory() {
    }

    public static Villager create(Level level, ResourceLocation professionId) {
        int seed = professionId.toString().hashCode();
        Villager villager = createModel(level, seed);
        villager.setVillagerData(new VillagerData(VillagerType.PLAINS,
                BuiltInRegistries.VILLAGER_PROFESSION.get(professionId), 1));
        // Villagers Reborn chooses a bundled skin lazily in its own renderer.
        // Seed only this unspawned preview so each profession keeps the same skin.
        villager.getRandom().setSeed(seed);
        villager.setAge(0);
        villager.setNoAi(true);
        villager.setSilent(true);
        return villager;
    }

    private static Villager createModel(Level level, int seed) {
        List<ResourceLocation> models = (seed & 1) == 0
                ? List.of(REBORN_MALE, REBORN_FEMALE)
                : List.of(REBORN_FEMALE, REBORN_MALE);
        for (ResourceLocation model : models) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(model).orElse(null);
            if (type != null && type.create(level) instanceof Villager villager) {
                return villager;
            }
        }
        // Registry lookups keep the integration optional, with no Reborn class linkage.
        return new Villager(EntityType.VILLAGER, level);
    }
}
