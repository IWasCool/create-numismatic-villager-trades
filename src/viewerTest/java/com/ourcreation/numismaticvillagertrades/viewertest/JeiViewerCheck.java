package com.ourcreation.numismaticvillagertrades.viewertest;

import static com.ourcreation.numismaticvillagertrades.viewertest.ViewerTestClient.require;

import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades;
import com.ourcreation.numismaticvillagertrades.client.jei.NumismaticVillagerTradesJeiPlugin;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@JeiPlugin
public final class JeiViewerCheck implements IModPlugin {
    private static IJeiRuntime runtime;

    @Override public ResourceLocation getPluginUid() { return ResourceLocation.fromNamespaceAndPath("numismatic_viewer_tests", "jei"); }
    @Override public void onRuntimeAvailable(IJeiRuntime available) { runtime = available; }
    @Override public void onRuntimeUnavailable() { runtime = null; }

    static ViewerTestClient.Result checkAndOpen() {
        if (runtime == null) return null;
        var client = Minecraft.getInstance();
        var registered = runtime.getRecipeManager().createRecipeLookup(NumismaticVillagerTradesJeiPlugin.VILLAGER_TRADES).get().toList();
        if (registered.isEmpty()) return null;
        var expected = NumismaticVillagerTrades.buildProfessionTrades(client.level.registryAccess());
        require(registered.size() == expected.size(), "JEI lost a profession page");
        int offers = registered.stream().mapToInt(page -> page.trades().size()).sum();
        require(offers == expected.stream().mapToInt(page -> page.trades().size()).sum(), "JEI lost trades");
        var focus = runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT,
                VanillaTypes.ITEM_STACK, new ItemStack(Items.RAW_IRON));
        runtime.getRecipesGui().show(java.util.List.of(focus));
        runtime.getRecipesGui().showTypes(java.util.List.of(NumismaticVillagerTradesJeiPlugin.VILLAGER_TRADES));
        return new ViewerTestClient.Result("jei", registered.size(), offers, client.screen.getClass().getName());
    }
}
