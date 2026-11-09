package com.ourcreation.numismaticvillagertrades.viewertest;

import static com.ourcreation.numismaticvillagertrades.viewertest.ViewerTestClient.require;

import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.client.emi.NumismaticVillagerTradesEmiPlugin;
import com.ourcreation.numismaticvillagertrades.client.emi.VillagerTradeEmiRecipe;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

final class EmiViewerCheck {
    static ViewerTestClient.Result checkAndOpen() {
        var client = Minecraft.getInstance();
        var manager = EmiApi.getRecipeManager();
        var categories = manager.getCategories().stream()
                .filter(category -> category.getId().equals(NumismaticVillagerTradesEmiPlugin.CATEGORY_ID)).toList();
        if (categories.isEmpty()) return null;
        require(categories.size() == 1, "Duplicate native/bridged EMI trade categories");
        var category = categories.getFirst();
        var recipes = manager.getRecipes(category);
        if (recipes.isEmpty()) return null;
        var pages = NumismaticVillagerTrades.buildTradePages(client.level.registryAccess(), 6);
        require(recipes.size() == pages.size(), "EMI registered " + recipes.size() + " pages instead of " + pages.size());
        int offers = 0;
        for (var page : pages) {
            var recipe = manager.getRecipe(page.id());
            require(recipe instanceof VillagerTradeEmiRecipe, "Native EMI recipe missing: " + page.id());
            require(!recipe.supportsRecipeTree(), "Alternative merchant offers entered the crafting tree");
            require(recipe.getOutputs().size() == page.trades().size(), "EMI lost output entries");
            for (int row = 0; row < page.trades().size(); row++) {
                var output = page.trades().get(row).result();
                var shown = recipe.getOutputs().get(row).getItemStack();
                require(ItemStack.isSameItemSameComponents(output, shown) && output.getCount() == shown.getCount(),
                        "EMI changed a result or enchantment");
                require(manager.getRecipesByOutput(EmiStack.of(output)).contains(recipe), "EMI output is not searchable");
                require(manager.getRecipesByInput(EmiStack.of(page.trades().get(row).costA())).contains(recipe), "EMI coin usage is not searchable");
                offers++;
            }
        }
        var miner = recipes.stream().filter(recipe -> recipe.getId().getPath().contains("/miner/0000")).findFirst().orElseThrow();
        EmiApi.displayRecipe(miner);
        return new ViewerTestClient.Result("emi", recipes.size(), offers, client.screen.getClass().getName());
    }
}
