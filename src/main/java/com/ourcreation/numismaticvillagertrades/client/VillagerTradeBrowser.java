package com.ourcreation.numismaticvillagertrades.client;

import java.util.List;
import java.util.function.BooleanSupplier;

import com.ourcreation.numismaticvillagertrades.CreateNumismaticVillagerTrades;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class VillagerTradeBrowser {
    public static final String TITLE_KEY = "gui." + CreateNumismaticVillagerTrades.MODID + ".jei.villager_trades";
    private static BooleanSupplier recipeViewer = () -> false;

    private VillagerTradeBrowser() {
    }

    public static void setRecipeViewer(BooleanSupplier opener) {
        recipeViewer = opener;
    }

    public static void open(Screen parent) {
        if (!recipeViewer.getAsBoolean()) {
            Minecraft.getInstance().setScreen(new VillagerTradesScreen(parent));
        }
    }

    public static Component professionName(ResourceLocation professionId) {
        String name = "minecraft".equals(professionId.getNamespace())
                ? professionId.getPath()
                : professionId.getNamespace() + "." + professionId.getPath();
        return Component.translatable("entity.minecraft.villager." + name);
    }

    public static List<ItemStack> jobSites(ResourceLocation professionId) {
        return BuiltInRegistries.VILLAGER_PROFESSION.getOptional(professionId)
                .map(profession -> BuiltInRegistries.POINT_OF_INTEREST_TYPE.holders()
                        .filter(profession.heldJobSite())
                        .flatMap(holder -> holder.value().matchingStates().stream())
                        .map(state -> state.getBlock().asItem())
                        .distinct()
                        .map(ItemStack::new)
                        .filter(stack -> !stack.isEmpty())
                        .toList())
                .orElse(List.of());
    }
}
