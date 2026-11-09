package com.ourcreation.numismaticvillagertrades.client.viewer;

import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades.TradePage;
import com.ourcreation.numismaticvillagertrades.compat.VillagerPreviewFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Shared presentation inside recipe viewers; this class opens no screens or keybinds. */
public final class TradeViewerPresentation {
    public static final String TITLE_KEY = "gui.numismatic_villager_trades.viewer.villager_trades";
    public static final String JOB_SITE_KEY = "gui.numismatic_villager_trades.trades.job_site";
    public static final int WIDTH = 180;
    public static final int HEIGHT = 154;
    public static final int PAGE_SIZE = 6;

    private TradeViewerPresentation() {
    }

    public static Component professionName(ResourceLocation id) {
        String name = "minecraft".equals(id.getNamespace()) ? id.getPath() : id.getNamespace() + "." + id.getPath();
        return Component.translatable("entity.minecraft.villager." + name);
    }

    public static List<ItemStack> jobSites(ResourceLocation id) {
        return BuiltInRegistries.VILLAGER_PROFESSION.getOptional(id)
                .map(profession -> BuiltInRegistries.POINT_OF_INTEREST_TYPE.holders()
                        .filter(profession.heldJobSite())
                        .flatMap(holder -> holder.value().matchingStates().stream())
                        .map(state -> state.getBlock().asItem()).distinct().map(ItemStack::new)
                        .filter(stack -> !stack.isEmpty()).toList())
                .orElse(List.of());
    }

    public static ItemStack iconStack() {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse("numismatics:crown"))
                .map(ItemStack::new).orElseGet(() -> new ItemStack(Items.EMERALD));
    }

    public static Villager createPortrait(ResourceLocation profession) {
        var level = Minecraft.getInstance().level;
        return level == null ? null : VillagerPreviewFactory.create(level, profession);
    }

    public static void drawPortrait(GuiGraphics graphics, Villager villager, double mouseX, double mouseY) {
        if (villager == null) return;
        float yaw = (float) Math.atan((33 - mouseX) / 40);
        float pitch = (float) Math.atan((68 - mouseY) / 40);
        villager.yBodyRot = 180 + yaw * 20;
        villager.setYRot(180 + yaw * 40);
        villager.setXRot(-pitch * 20);
        villager.yHeadRot = villager.getYRot();
        villager.yHeadRotO = villager.yHeadRot;
        Quaternionf camera = new Quaternionf().rotateX(pitch * (float) (Math.PI / 9));
        InventoryScreen.renderEntityInInventory(graphics, 33, 68, 38,
                new Vector3f(0, villager.getBbHeight() / 2 + 0.0625F, 0),
                new Quaternionf().rotateZ((float) Math.PI).mul(camera), camera, villager);
    }

    public static void drawPage(GuiGraphics graphics, TradePage page, Villager portrait, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        String title = font.plainSubstrByWidth(professionName(page.professionId()).getString(), WIDTH - 8);
        graphics.drawString(font, title, (WIDTH - font.width(title)) / 2, 4, 0xFF404040, false);
        graphics.fill(67, 20, 68, 140, 0x40555555);
        for (int row = 0; row < page.trades().size(); row++) {
            if ((page.firstRow() + row) % 2 == 0) graphics.fill(72, 20 + row * 20, 178, 40 + row * 20, 0x12404040);
        }
        Component range = Component.translatable("gui.numismatic_villager_trades.trades.range",
                page.firstRow() + 1, page.firstRow() + page.trades().size(), page.totalRows());
        graphics.drawString(font, range, 72 + (106 - font.width(range)) / 2, 145, 0xFF606060, false);
        drawPortrait(graphics, portrait, mouseX, mouseY);
    }
}
