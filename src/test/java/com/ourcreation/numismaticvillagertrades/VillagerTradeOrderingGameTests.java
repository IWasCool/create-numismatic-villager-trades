package com.ourcreation.numismaticvillagertrades;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CreateNumismaticVillagerTrades.MODID)
@PrefixGameTestTemplate(false)
public class VillagerTradeOrderingGameTests {
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void existingVillagersMatchJeiAfterLoadingAndOpeningTheirTrades(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        for (var page : NumismaticVillagerTrades.buildProfessionTrades(level.registryAccess())) {
            var profession = BuiltInRegistries.VILLAGER_PROFESSION.get(page.professionId());
            var villager = new Villager(EntityType.VILLAGER, level);
            villager.setVillagerData(new VillagerData(VillagerType.PLAINS, profession, 1));
            var expected = NumismaticVillagerTrades.buildOffers(profession, level.registryAccess(), RandomSource.create(0));
            for (boolean loading : List.of(true, false)) {
                var savedBackwards = new MerchantOffers();
                savedBackwards.addAll(expected.reversed());
                villager.overrideOffers(savedBackwards);
                if (loading) {
                    NumismaticVillagerTrades.onEntityJoinLevel(new EntityJoinLevelEvent(villager, level, true));
                } else {
                    NumismaticVillagerTrades.onPlayerInteract(
                            new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, villager));
                }
                var actual = villager.getOffers();
                helper.assertTrue(actual.size() == page.trades().size(), "Every JEI offer must appear on " + page.professionId());
                for (int index = 0; index < actual.size(); index++) {
                    var display = page.trades().get(index);
                    helper.assertTrue(sameStack(display.costA(), actual.get(index).getBaseCostA())
                                    && sameStack(display.costB(), actual.get(index).getCostB())
                                    && sameStack(display.result(), actual.get(index).getResult()),
                            "Villager order must match JEI after " + (loading ? "loading" : "opening trades")
                                    + ": " + page.professionId() + " offer " + index);
                }
            }
        }
        helper.succeed();
    }

    private static boolean sameStack(ItemStack first, ItemStack second) {
        return ItemStack.isSameItemSameComponents(first, second) && first.getCount() == second.getCount();
    }
}
