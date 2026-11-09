package com.ourcreation.numismaticvillagertrades.client.jei;

import java.util.List;

import com.ourcreation.numismaticvillagertrades.CreateNumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades.ProfessionTrades;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public final class NumismaticVillagerTradesJeiPlugin implements IModPlugin {
    public static final RecipeType<ProfessionTrades> VILLAGER_TRADES = RecipeType.create(
            CreateNumismaticVillagerTrades.MODID,
            "villager_trades",
            ProfessionTrades.class);

    private static final List<ResourceLocation> COIN_ITEMS = List.of(
            ResourceLocation.parse("numismatics:spur"),
            ResourceLocation.parse("numismatics:bevel"),
            ResourceLocation.parse("numismatics:sprocket"),
            ResourceLocation.parse("numismatics:cog"),
            ResourceLocation.parse("numismatics:crown"),
            ResourceLocation.parse("numismatics:sun"));

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(CreateNumismaticVillagerTrades.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new NumismaticVillagerTradeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RegistryAccess registryAccess = registryAccess();
        if (registryAccess != null) {
            registration.addRecipes(VILLAGER_TRADES, NumismaticVillagerTrades.buildProfessionTrades(registryAccess));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (ResourceLocation coinId : COIN_ITEMS) {
            BuiltInRegistries.ITEM.getOptional(coinId)
                    .map(ItemStack::new)
                    .ifPresent(stack -> registration.addRecipeCatalyst(stack, VILLAGER_TRADES));
        }
    }

    private static RegistryAccess registryAccess() {
        if (Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.registryAccess();
        }
        return null;
    }
}
