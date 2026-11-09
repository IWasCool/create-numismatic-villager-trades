package com.ourcreation.numismaticvillagertrades.client.rei;

import java.util.ArrayList;
import java.util.List;

import com.ourcreation.numismaticvillagertrades.client.viewer.TradeViewerPresentation;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

public final class VillagerTradeReiCategory implements DisplayCategory<VillagerTradeReiDisplay> {
    @Override public CategoryIdentifier<VillagerTradeReiDisplay> getCategoryIdentifier() { return NumismaticVillagerTradesReiPlugin.CATEGORY; }
    @Override public Component getTitle() { return Component.translatable(TradeViewerPresentation.TITLE_KEY); }
    @Override public Renderer getIcon() { return EntryStacks.of(TradeViewerPresentation.iconStack()); }
    @Override public int getDisplayWidth(VillagerTradeReiDisplay display) { return TradeViewerPresentation.WIDTH + 8; }
    @Override public int getDisplayHeight() { return TradeViewerPresentation.HEIGHT + 8; }

    @Override
    public List<Widget> setupDisplay(VillagerTradeReiDisplay display, Rectangle bounds) {
        var page = display.page();
        int x = bounds.x + 4;
        int y = bounds.y + 4;
        var portrait = TradeViewerPresentation.createPortrait(page.professionId());
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            TradeViewerPresentation.drawPage(graphics, page, portrait, mouseX - x, mouseY - y);
            graphics.pose().popPose();
        }));
        var sites = TradeViewerPresentation.jobSites(page.professionId());
        if (!sites.isEmpty()) {
            widgets.add(Widgets.createSlot(new Point(x + 25, y + 122)).entries(sites.stream().map(EntryStacks::of).toList()));
            widgets.add(Widgets.createTooltip(new Rectangle(x + 24, y + 121, 18, 18),
                    Component.translatable(TradeViewerPresentation.JOB_SITE_KEY)));
        }
        for (int row = 0; row < page.trades().size(); row++) {
            var trade = page.trades().get(row);
            int rowY = y + 22 + row * 20;
            widgets.add(Widgets.createSlot(new Point(x + 75, rowY)).entry(EntryStacks.of(trade.costA())).markInput());
            if (!trade.costB().isEmpty()) {
                widgets.add(Widgets.createSlot(new Point(x + 95, rowY)).entry(EntryStacks.of(trade.costB())).markInput());
            }
            widgets.add(Widgets.createArrow(new Point(x + 119, rowY)).disableAnimation());
            widgets.add(Widgets.createSlot(new Point(x + 151, rowY)).entry(EntryStacks.of(trade.result())).markOutput());
        }
        return widgets;
    }
}
