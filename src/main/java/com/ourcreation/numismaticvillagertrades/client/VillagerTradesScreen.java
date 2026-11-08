package com.ourcreation.numismaticvillagertrades.client;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.ourcreation.numismaticvillagertrades.CreateNumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades.TradeDisplay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class VillagerTradesScreen extends Screen {
    private static final String PREFIX = "gui." + CreateNumismaticVillagerTrades.MODID + ".trades.";
    private static final int ROW_HEIGHT = 32;

    private final Screen parent;
    private List<SearchableTrade> trades = List.of();
    private List<SearchableTrade> filteredTrades = List.of();
    private List<ResourceLocation> professions = List.of();
    private EditBox search;
    private Button professionButton;
    private Button previousPage;
    private Button nextPage;
    private int professionIndex;
    private int page;
    private int visibleRows;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public VillagerTradesScreen(Screen parent) {
        super(Component.translatable(VillagerTradeBrowser.TITLE_KEY));
        this.parent = parent;
    }

    @Override
    protected void init() {
        String query = search == null ? "" : search.getValue();
        visibleRows = Mth.clamp((height - 132) / ROW_HEIGHT, 1, 8);
        panelWidth = Math.min(340, width - 16);
        panelHeight = 112 + visibleRows * ROW_HEIGHT;
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;

        trades = NumismaticVillagerTrades.buildTradeDisplays(minecraft.level.registryAccess()).stream()
                .sorted(Comparator.comparing((TradeDisplay trade) -> VillagerTradeBrowser.professionName(trade.professionId()).getString()))
                .map(trade -> new SearchableTrade(trade, searchableText(trade)))
                .toList();
        professions = trades.stream().map(trade -> trade.display().professionId()).distinct().toList();
        professionIndex = Mth.clamp(professionIndex, 0, professions.size());

        search = addRenderableWidget(new EditBox(font, panelLeft + 12, panelTop + 28, panelWidth - 24, 20,
                Component.translatable(PREFIX + "search")));
        search.setHint(Component.translatable(PREFIX + "search"));
        search.setMaxLength(128);
        search.setValue(query);
        professionButton = addRenderableWidget(Button.builder(professionLabel(), button -> {
            professionIndex = (professionIndex + 1) % (professions.size() + 1);
            professionButton.setMessage(professionLabel());
            page = 0;
            filterTrades();
        }).bounds(panelLeft + 12, panelTop + 52, panelWidth - 24, 20).build());

        int footerY = panelTop + panelHeight - 28;
        previousPage = addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
                .bounds(panelLeft + 12, footerY, 20, 20).build());
        nextPage = addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
                .bounds(panelLeft + 36, footerY, 20, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(panelLeft + panelWidth - 62, footerY, 50, 20).build());
        search.setResponder(value -> {
            page = 0;
            filterTrades();
        });
        filterTrades();
        setInitialFocus(search);
    }

    private Component professionLabel() {
        return professionIndex == 0 ? Component.translatable(PREFIX + "all_professions")
                : VillagerTradeBrowser.professionName(professions.get(professionIndex - 1));
    }

    private String searchableText(TradeDisplay trade) {
        String items = List.of(trade.costA(), trade.costB(), trade.result()).stream()
                .filter(stack -> !stack.isEmpty())
                .flatMap(stack -> stack.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, TooltipFlag.NORMAL).stream())
                .map(Component::getString)
                .collect(Collectors.joining(" "));
        return (VillagerTradeBrowser.professionName(trade.professionId()).getString() + " " + trade.professionId() + " " + items)
                .toLowerCase(Locale.ROOT);
    }

    private void filterTrades() {
        String query = search.getValue().strip().toLowerCase(Locale.ROOT);
        ResourceLocation profession = professionIndex == 0 ? null : professions.get(professionIndex - 1);
        filteredTrades = trades.stream()
                .filter(trade -> profession == null || profession.equals(trade.display().professionId()))
                .filter(trade -> trade.searchText().contains(query))
                .toList();
        changePage(0);
    }

    private int pageCount() {
        return Math.max(1, (filteredTrades.size() + visibleRows - 1) / visibleRows);
    }

    private void changePage(int delta) {
        page = Mth.clamp(page + delta, 0, pageCount() - 1);
        previousPage.active = page > 0;
        nextPage.active = page < pageCount() - 1;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= panelLeft && mouseX < panelLeft + panelWidth && mouseY >= panelTop + 80
                && mouseY < panelTop + panelHeight - 32 && scrollY != 0) {
            changePage(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(panelLeft - 1, panelTop - 1, panelLeft + panelWidth + 1, panelTop + panelHeight + 1, 0xFF373737);
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xFFC6C6C6);
        graphics.drawCenteredString(font, title, width / 2, panelTop + 10, 0xFFFFFFFF);

        ItemStack hovered = ItemStack.EMPTY;
        for (int row = 0; row < visibleRows && page * visibleRows + row < filteredTrades.size(); row++) {
            TradeDisplay trade = filteredTrades.get(page * visibleRows + row).display();
            int y = panelTop + 80 + row * ROW_HEIGHT;
            graphics.fill(panelLeft + 10, y, panelLeft + panelWidth - 10, y + ROW_HEIGHT - 2, 0xFFABABAB);
            ItemStack[] stacks = { trade.costA(), trade.costB(), trade.result() };
            int[] offsets = { 14, 36, 82 };
            for (int slot = 0; slot < stacks.length; slot++) {
                int x = panelLeft + offsets[slot];
                if (!stacks[slot].isEmpty()) {
                    graphics.renderItem(stacks[slot], x, y + 7);
                    graphics.renderItemDecorations(font, stacks[slot], x, y + 7);
                    if (mouseX >= x && mouseX < x + 16 && mouseY >= y + 7 && mouseY < y + 23) {
                        hovered = stacks[slot];
                    }
                }
            }
            graphics.drawString(font, "\u2192", panelLeft + 62, y + 11, 0xFF373737, false);
            int textWidth = panelWidth - 122;
            graphics.drawString(font, font.plainSubstrByWidth(VillagerTradeBrowser.professionName(trade.professionId()).getString(), textWidth),
                    panelLeft + 106, y + 4, 0xFF555555, false);
            graphics.drawString(font, font.plainSubstrByWidth(trade.result().getHoverName().getString(), textWidth),
                    panelLeft + 106, y + 16, 0xFF202020, false);
        }
        if (filteredTrades.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable(PREFIX + "empty"), width / 2, panelTop + 88, 0xFFFFFFFF);
        }
        graphics.drawCenteredString(font, Component.translatable(PREFIX + "page", page + 1, pageCount()),
                width / 2, panelTop + panelHeight - 22, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!hovered.isEmpty()) {
            graphics.renderTooltip(font, hovered, mouseX, mouseY);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record SearchableTrade(TradeDisplay display, String searchText) {
    }
}
