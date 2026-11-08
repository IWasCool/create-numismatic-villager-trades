package com.ourcreation.numismaticvillagertrades.client.jei;

import java.util.List;
import java.util.Optional;

import com.ourcreation.numismaticvillagertrades.CreateNumismaticVillagerTrades;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

final class VillagerTradeListWidget implements ISlottedRecipeWidget, IJeiGuiEventListener {
    static final int VISIBLE_ROWS = 6;
    private static final int ROW_HEIGHT = 20;
    private static final int WIDTH = 108;
    private static final int HEIGHT = ROW_HEIGHT * VISIBLE_ROWS;
    private static final int SCROLLBAR_X = WIDTH - 6;
    private static final ScreenPosition POSITION = new ScreenPosition(72, 20);

    private final List<TradeRow> rows;
    private final IDrawable arrow;
    private final TradeListScrollState scroll;
    private final int focusedRow;
    private boolean dragging;
    private double grabOffset;

    VillagerTradeListWidget(List<TradeRow> rows, IDrawable arrow, int focusedRow) {
        this.rows = List.copyOf(rows);
        this.arrow = arrow;
        this.focusedRow = focusedRow;
        this.scroll = new TradeListScrollState(rows.size(), VISIBLE_ROWS);
        scroll.reveal(focusedRow);
        positionSlots();
    }

    @Override
    public ScreenPosition getPosition() {
        return POSITION;
    }

    @Override
    public ScreenRectangle getArea() {
        return new ScreenRectangle(POSITION, WIDTH, HEIGHT);
    }

    @Override
    public ScreenRectangle getScreenRectangle() {
        return getArea();
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (!Minecraft.getInstance().mouseHandler.isLeftPressed()) {
            dragging = false;
        }
        for (int index = scroll.firstRow(); index < scroll.endRow(); index++) {
            int y = (index - scroll.firstRow()) * ROW_HEIGHT;
            if (index == focusedRow) {
                guiGraphics.fill(0, y, SCROLLBAR_X - 2, y + ROW_HEIGHT, 0x30E0BA56);
            } else if (index % 2 == 0) {
                guiGraphics.fill(0, y, SCROLLBAR_X - 2, y + ROW_HEIGHT, 0x12404040);
            }
            arrow.draw(guiGraphics, 47, y + 2);
            for (var slot : rows.get(index).slots()) {
                slot.draw(guiGraphics, slot.isMouseOver(mouseX, mouseY));
            }
        }

        if (scroll.hasOverflow()) {
            guiGraphics.fill(SCROLLBAR_X, 0, WIDTH, HEIGHT, 0xFF555555);
            int top = thumbTop();
            guiGraphics.fill(SCROLLBAR_X, top, WIDTH, top + thumbHeight(), 0xFF999999);
            guiGraphics.fill(SCROLLBAR_X + 1, top + 1, WIDTH - 1, top + thumbHeight() - 1,
                    dragging ? 0xFFFFFFFF : 0xFFDDDDDD);
        }

        var font = Minecraft.getInstance().font;
        Component range = Component.translatable("gui." + CreateNumismaticVillagerTrades.MODID + ".trades.range",
                scroll.firstRow() + 1, scroll.endRow(), rows.size());
        guiGraphics.drawString(font, range, (SCROLLBAR_X - font.width(range)) / 2, HEIGHT + 5, 0xFF606060, false);
    }

    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        if (mouseX < 0 || mouseX >= SCROLLBAR_X - 2 || mouseY < 0 || mouseY >= HEIGHT) {
            return Optional.empty();
        }
        for (int index = scroll.firstRow(); index < scroll.endRow(); index++) {
            for (var slot : rows.get(index).slots()) {
                if (slot.isMouseOver(mouseX, mouseY)) {
                    return Optional.of(new RecipeSlotUnderMouse(slot, POSITION));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!scroll.hasOverflow() || scrollY == 0) {
            return false;
        }
        scroll.scroll(scrollY);
        positionSlots();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        dragging = false;
        if (!scroll.hasOverflow() || mouseX < SCROLLBAR_X || mouseX >= WIDTH) {
            return false;
        }
        int top = thumbTop();
        grabOffset = mouseY >= top && mouseY < top + thumbHeight() ? mouseY - top : thumbHeight() / 2.0;
        dragging = true;
        dragThumb(mouseY);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0 || !dragging) {
            return false;
        }
        dragThumb(mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || !dragging) {
            return false;
        }
        dragging = false;
        return true;
    }

    private int thumbHeight() {
        return Math.max(12, HEIGHT * VISIBLE_ROWS / rows.size());
    }

    private int thumbTop() {
        return (int) Math.round((HEIGHT - thumbHeight()) * scroll.fraction());
    }

    private void dragThumb(double mouseY) {
        scroll.scrollToFraction((mouseY - grabOffset) / (HEIGHT - thumbHeight()));
        positionSlots();
    }

    private void positionSlots() {
        for (int index = scroll.firstRow(); index < scroll.endRow(); index++) {
            int y = (index - scroll.firstRow()) * ROW_HEIGHT + 2;
            TradeRow row = rows.get(index);
            row.costA().setPosition(3, y);
            row.costB().ifPresent(slot -> slot.setPosition(23, y));
            row.result().setPosition(79, y);
        }
    }

    record TradeRow(IRecipeSlotDrawable costA, Optional<IRecipeSlotDrawable> costB, IRecipeSlotDrawable result) {
        List<IRecipeSlotDrawable> slots() {
            return costB.map(second -> List.of(costA, second, result)).orElseGet(() -> List.of(costA, result));
        }
    }
}
