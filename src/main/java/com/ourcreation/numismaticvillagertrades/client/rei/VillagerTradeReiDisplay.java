package com.ourcreation.numismaticvillagertrades.client.rei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades.TradePage;
import com.ourcreation.numismaticvillagertrades.client.viewer.TradeViewerPresentation;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;

public final class VillagerTradeReiDisplay extends BasicDisplay {
    private final TradePage page;

    public VillagerTradeReiDisplay(TradePage page) {
        super(inputs(page), page.trades().stream().map(trade -> EntryIngredient.of(EntryStacks.of(trade.result()))).toList(),
                Optional.of(page.id()));
        this.page = page;
    }

    public TradePage page() { return page; }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return NumismaticVillagerTradesReiPlugin.CATEGORY;
    }

    // Merchant listings represent alternative purchases, never a crafting requirement.
    @Override
    public List<EntryIngredient> getRequiredEntries() { return List.of(); }

    private static List<EntryIngredient> inputs(TradePage page) {
        List<EntryIngredient> result = new ArrayList<>();
        page.trades().forEach(trade -> {
            result.add(EntryIngredient.of(EntryStacks.of(trade.costA())));
            if (!trade.costB().isEmpty()) result.add(EntryIngredient.of(EntryStacks.of(trade.costB())));
        });
        TradeViewerPresentation.jobSites(page.professionId())
                .forEach(stack -> result.add(EntryIngredient.of(EntryStacks.of(stack))));
        return List.copyOf(result);
    }
}
