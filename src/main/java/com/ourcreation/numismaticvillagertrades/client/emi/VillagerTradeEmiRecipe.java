package com.ourcreation.numismaticvillagertrades.client.emi;

import java.util.ArrayList;
import java.util.List;

import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades.TradePage;
import com.ourcreation.numismaticvillagertrades.client.viewer.TradeViewerPresentation;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class VillagerTradeEmiRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final TradePage page;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiIngredient> catalysts;

    public VillagerTradeEmiRecipe(EmiRecipeCategory category, TradePage page) {
        this.category = category;
        this.page = page;
        List<EmiIngredient> costs = new ArrayList<>();
        page.trades().forEach(trade -> {
            costs.add(EmiStack.of(trade.costA()));
            if (!trade.costB().isEmpty()) costs.add(EmiStack.of(trade.costB()));
        });
        inputs = List.copyOf(costs);
        outputs = page.trades().stream().map(trade -> EmiStack.of(trade.result())).toList();
        catalysts = TradeViewerPresentation.jobSites(page.professionId()).stream()
                .<EmiIngredient>map(EmiStack::of).toList();
    }

    @Override public EmiRecipeCategory getCategory() { return category; }
    @Override public ResourceLocation getId() { return page.id(); }
    @Override public List<EmiIngredient> getInputs() { return inputs; }
    @Override public List<EmiStack> getOutputs() { return outputs; }
    @Override public List<EmiIngredient> getCatalysts() { return catalysts; }
    @Override public int getDisplayWidth() { return TradeViewerPresentation.WIDTH; }
    @Override public int getDisplayHeight() { return TradeViewerPresentation.HEIGHT; }

    // These are alternative merchant offers, not one multi-output crafting recipe.
    @Override public boolean supportsRecipeTree() { return false; }
    @Override public boolean hideCraftable() { return true; }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var portrait = TradeViewerPresentation.createPortrait(page.professionId());
        widgets.addDrawable(0, 0, getDisplayWidth(), getDisplayHeight(), (graphics, mouseX, mouseY, delta) ->
                TradeViewerPresentation.drawPage(graphics, page, portrait, mouseX, mouseY));
        if (!catalysts.isEmpty()) {
            widgets.addSlot(EmiIngredient.of(catalysts.stream().flatMap(c -> c.getEmiStacks().stream()).toList()), 24, 121);
            widgets.addTooltipText(List.of(Component.translatable(TradeViewerPresentation.JOB_SITE_KEY)), 24, 121, 18, 18);
        }
        for (int row = 0; row < page.trades().size(); row++) {
            var trade = page.trades().get(row);
            int y = 21 + row * 20;
            widgets.addSlot(EmiStack.of(trade.costA()), 74, y);
            if (!trade.costB().isEmpty()) widgets.addSlot(EmiStack.of(trade.costB()), 94, y);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 119, y + 1);
            widgets.addSlot(EmiStack.of(trade.result()), 150, y).recipeContext(this);
        }
    }
}
