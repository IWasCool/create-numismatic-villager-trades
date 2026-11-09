package com.ourcreation.numismaticvillagertrades.viewertest;

import static com.ourcreation.numismaticvillagertrades.viewertest.ViewerTestClient.require;

import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.client.rei.NumismaticVillagerTradesReiPlugin;
import com.ourcreation.numismaticvillagertrades.client.rei.VillagerTradeReiDisplay;

import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class ReiViewerCheck {
    static ViewerTestClient.Result checkAndOpen() {
        var client = Minecraft.getInstance();
        var displays = DisplayRegistry.getInstance().getAll().get(NumismaticVillagerTradesReiPlugin.CATEGORY);
        if (displays == null || displays.isEmpty()) return null;
        var pages = NumismaticVillagerTrades.buildTradePages(client.level.registryAccess(), 6);
        require(displays.size() == pages.size(), "REI page count differs from the shared trade catalog");
        int offers = 0;
        for (int index = 0; index < pages.size(); index++) {
            var expected = pages.get(index);
            require(displays.get(index) instanceof VillagerTradeReiDisplay, "REI native display missing");
            var display = (VillagerTradeReiDisplay) displays.get(index);
            require(display.page().id().equals(expected.id()), "REI changed profession/price page ordering");
            require(display.getOutputEntries().size() == expected.trades().size(), "REI lost output entries");
            for (int row = 0; row < expected.trades().size(); row++) {
                ItemStack actual = (ItemStack) display.getOutputEntries().get(row).getFirst().getValue();
                ItemStack output = expected.trades().get(row).result();
                require(ItemStack.isSameItemSameComponents(actual, output) && actual.getCount() == output.getCount(),
                        "REI changed a result or enchantment");
                offers++;
            }
        }
        require(ViewSearchBuilder.builder().filterCategory(NumismaticVillagerTradesReiPlugin.CATEGORY)
                .addRecipesFor(EntryStacks.of(Items.RAW_IRON)).open(), "REI could not find the miner by output");
        return new ViewerTestClient.Result("rei", displays.size(), offers, client.screen.getClass().getName());
    }
}
