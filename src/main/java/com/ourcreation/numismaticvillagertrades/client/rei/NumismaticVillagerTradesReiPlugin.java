package com.ourcreation.numismaticvillagertrades.client.rei;

import com.ourcreation.numismaticvillagertrades.CreateNumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.client.viewer.TradeViewerPresentation;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.Minecraft;

@REIPluginClient
public final class NumismaticVillagerTradesReiPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<VillagerTradeReiDisplay> CATEGORY = CategoryIdentifier.of(
            CreateNumismaticVillagerTrades.MODID, "villager_trades");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new VillagerTradeReiCategory());
        var level = Minecraft.getInstance().level;
        if (level != null) {
            NumismaticVillagerTrades.buildProfessionTrades(level.registryAccess()).stream()
                    .flatMap(profession -> TradeViewerPresentation.jobSites(profession.professionId()).stream())
                    .forEach(stack -> registry.addWorkstations(CATEGORY, EntryStacks.of(stack)));
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        NumismaticVillagerTrades.buildTradePages(level.registryAccess(), TradeViewerPresentation.PAGE_SIZE)
                .forEach(page -> registry.add(new VillagerTradeReiDisplay(page)));
    }
}
