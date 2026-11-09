package com.ourcreation.numismaticvillagertrades.client.emi;

import java.util.Comparator;

import com.ourcreation.numismaticvillagertrades.CreateNumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.client.viewer.TradeViewerPresentation;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@EmiEntrypoint
public final class NumismaticVillagerTradesEmiPlugin implements EmiPlugin {
    public static final ResourceLocation CATEGORY_ID = ResourceLocation.fromNamespaceAndPath(
            CreateNumismaticVillagerTrades.MODID, "villager_trades");

    @Override
    public void register(EmiRegistry registry) {
        var icon = EmiStack.of(TradeViewerPresentation.iconStack());
        var category = new EmiRecipeCategory(CATEGORY_ID, icon, icon, Comparator.comparing(EmiRecipe::getId)) {
            @Override
            public Component getName() {
                return Component.translatable(TradeViewerPresentation.TITLE_KEY);
            }
        };
        registry.addCategory(category);
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var pages = NumismaticVillagerTrades.buildTradePages(level.registryAccess(), TradeViewerPresentation.PAGE_SIZE);
        for (var page : pages) registry.addRecipe(new VillagerTradeEmiRecipe(category, page));
        pages.stream().map(page -> page.professionId()).distinct()
                .flatMap(id -> TradeViewerPresentation.jobSites(id).stream())
                .forEach(stack -> registry.addWorkstation(category, EmiStack.of(stack)));
    }
}
