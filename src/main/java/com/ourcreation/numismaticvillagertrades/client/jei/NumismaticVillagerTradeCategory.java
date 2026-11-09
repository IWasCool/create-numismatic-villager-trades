package com.ourcreation.numismaticvillagertrades.client.jei;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.ourcreation.numismaticvillagertrades.CreateNumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades.ProfessionTrades;
import com.ourcreation.numismaticvillagertrades.client.viewer.TradeViewerPresentation;
import com.ourcreation.numismaticvillagertrades.client.jei.VillagerTradeListWidget.TradeRow;
import com.ourcreation.numismaticvillagertrades.compat.VillagerPreviewFactory;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class NumismaticVillagerTradeCategory implements IRecipeCategory<ProfessionTrades> {
    private static final int WIDTH = 180;
    private static final int HEIGHT = 154;

    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable slot;

    public NumismaticVillagerTradeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(iconStack());
        this.arrow = guiHelper.getRecipeArrow();
        this.slot = guiHelper.getSlotDrawable();
    }

    @Override
    public RecipeType<ProfessionTrades> getRecipeType() {
        return NumismaticVillagerTradesJeiPlugin.VILLAGER_TRADES;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(TradeViewerPresentation.TITLE_KEY);
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public ResourceLocation getRegistryName(ProfessionTrades recipe) {
        return ResourceLocation.fromNamespaceAndPath(CreateNumismaticVillagerTrades.MODID,
                "villager_trades/" + recipe.professionId().getNamespace() + "/" + recipe.professionId().getPath());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ProfessionTrades recipe, IFocusGroup focuses) {
        builder.addInputSlot(25, 122).setBackground(slot, -1, -1)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                        "gui." + CreateNumismaticVillagerTrades.MODID + ".trades.job_site")))
                .addItemStacks(TradeViewerPresentation.jobSites(recipe.professionId()));

        // Register every trade with JEI, including rows that start outside the viewport.
        // The slotted widget owns drawing and hit-testing, so hidden rows stay searchable.
        for (int row = 0; row < recipe.trades().size(); row++) {
            var trade = recipe.trades().get(row);
            builder.addInputSlot().setSlotName(slotName(row, "cost_a"))
                    .setBackground(slot, -1, -1).addItemStack(trade.costA());
            if (!trade.costB().isEmpty()) {
                builder.addInputSlot().setSlotName(slotName(row, "cost_b"))
                        .setBackground(slot, -1, -1).addItemStack(trade.costB());
            }
            builder.addOutputSlot().setSlotName(slotName(row, "result"))
                    .setBackground(slot, -1, -1).addItemStack(trade.result());
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, ProfessionTrades recipe, IFocusGroup focuses) {
        var recipeSlots = builder.getRecipeSlots();
        List<TradeRow> rows = new ArrayList<>();
        for (int row = 0; row < recipe.trades().size(); row++) {
            rows.add(new TradeRow(
                    recipeSlots.findSlotByName(slotName(row, "cost_a")).orElseThrow(),
                    recipeSlots.findSlotByName(slotName(row, "cost_b")),
                    recipeSlots.findSlotByName(slotName(row, "result")).orElseThrow()));
        }
        var tradeList = new VillagerTradeListWidget(rows, arrow, focusedRow(recipe, focuses));
        builder.addSlottedWidget(tradeList, rows.stream().flatMap(row -> row.slots().stream()).toList());
        builder.addGuiEventListener(tradeList);

        var level = Minecraft.getInstance().level;
        if (level != null) {
            var villager = VillagerPreviewFactory.create(level, recipe.professionId());
            builder.addWidget(new VillagerPortrait(villager));
        }
    }

    @Override
    public void draw(ProfessionTrades recipe, IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        String profession = font.plainSubstrByWidth(
                TradeViewerPresentation.professionName(recipe.professionId()).getString(), WIDTH - 8);
        guiGraphics.drawString(font, profession, (WIDTH - font.width(profession)) / 2, 4, 0xFF404040, false);
        guiGraphics.fill(67, 20, 68, 140, 0x40555555);
    }

    private static String slotName(int row, String ingredient) {
        return "trade_" + row + "_" + ingredient;
    }

    private static int focusedRow(ProfessionTrades recipe, IFocusGroup focuses) {
        var itemFocuses = focuses.getFocuses(VanillaTypes.ITEM_STACK).toList();
        for (int row = 0; row < recipe.trades().size(); row++) {
            var trade = recipe.trades().get(row);
            if (itemFocuses.stream().anyMatch(focus -> {
                ItemStack item = focus.getTypedValue().getIngredient();
                return switch (focus.getRole()) {
                    case INPUT -> ItemStack.isSameItemSameComponents(item, trade.costA())
                            || ItemStack.isSameItemSameComponents(item, trade.costB());
                    case OUTPUT -> ItemStack.isSameItemSameComponents(item, trade.result());
                    default -> false;
                };
            })) {
                return row;
            }
        }
        return -1;
    }

    private record VillagerPortrait(Villager villager) implements IRecipeWidget {
        @Override
        public ScreenPosition getPosition() {
            return new ScreenPosition(0, 0);
        }

        @Override
        public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            float yaw = (float) Math.atan((33 - mouseX) / 40);
            float pitch = (float) Math.atan((68 - mouseY) / 40);
            villager.yBodyRot = 180 + yaw * 20;
            villager.setYRot(180 + yaw * 40);
            villager.setXRot(-pitch * 20);
            villager.yHeadRot = villager.getYRot();
            villager.yHeadRotO = villager.yHeadRot;
            Quaternionf camera = new Quaternionf().rotateX(pitch * (float) (Math.PI / 9));
            // The lower-level renderer respects JEI's translated pose without applying
            // the inventory screen helper's absolute screen-coordinate scissor.
            InventoryScreen.renderEntityInInventory(guiGraphics, 33, 68, 38,
                    new Vector3f(0, villager.getBbHeight() / 2 + 0.0625F, 0),
                    new Quaternionf().rotateZ((float) Math.PI).mul(camera), camera, villager);
        }
    }

    private static ItemStack iconStack() {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse("numismatics:crown"))
                .map(ItemStack::new)
                .orElseGet(() -> new ItemStack(Items.EMERALD));
    }
}
