package com.ourcreation.numismaticvillagertrades;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades.TradeDisplay;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;

@ExtendWith(EphemeralTestServerProvider.class)
class VillagerTradeCatalogTest {
    private static final Map<String, Integer> COIN_VALUES = Map.of(
            "spur", 1, "bevel", 8, "sprocket", 16, "cog", 64, "crown", 512, "sun", 4096);

    @Test
    void villagerPricesDescendWithSmallStacksAndExactDenominations(MinecraftServer server) {
        var professions = NumismaticVillagerTrades.buildTradeDisplays(server.registryAccess()).stream()
                .collect(Collectors.groupingBy(TradeDisplay::professionId));
        for (var profession : professions.entrySet()) {
            int previous = Integer.MAX_VALUE;
            for (var trade : profession.getValue()) {
                int price = price(trade);
                assertTrue(price <= previous, "Cheapest villager offers must be at the bottom: " + profession.getKey());
                assertTrue(trade.costA().getCount() <= 7, "Avoid crowded coin stacks: " + trade.result());
                int largest = COIN_VALUES.values().stream().filter(value -> price % value == 0)
                        .mapToInt(Integer::intValue).max().orElseThrow();
                assertEquals(price / largest, trade.costA().getCount(), "Use the largest exact denomination");
                previous = price;
            }
        }
    }

    @Test
    void finishedGearUsesAModestMaterialMarkupAcrossProfessions(MinecraftServer server) {
        var prices = NumismaticVillagerTrades.buildTradeDisplays(server.registryAccess()).stream()
                .collect(Collectors.toMap(trade -> BuiltInRegistries.ITEM.getKey(trade.result().getItem()).toString(),
                        VillagerTradeCatalogTest::price, (first, second) -> first));
        var materials = Map.of("leather", "minecraft:leather", "iron", "minecraft:raw_iron",
                "golden", "minecraft:raw_gold", "diamond", "minecraft:diamond", "netherite", "minecraft:netherite_scrap");
        materials.forEach((material, raw) -> {
            int expected = 2 * prices.get(raw);
            for (String piece : List.of("helmet", "chestplate", "leggings", "boots", "horse_armor",
                    "sword", "shovel", "pickaxe", "hoe", "axe")) {
                String item = "minecraft:" + material + "_" + piece;
                if (prices.containsKey(item)) {
                    assertEquals(expected, prices.get(item), "Finished gear should not scale with ingredient count: " + item);
                }
            }
            String knife = "farmersdelight:" + material + "_knife";
            if (prices.containsKey(knife)) {
                assertEquals(expected, prices.get(knife), "Knives follow the same shop material tier");
            }
        });
        assertTrue(prices.get("minecraft:leather_leggings") < prices.get("minecraft:chainmail_leggings"));
        assertTrue(prices.get("minecraft:chainmail_leggings") < prices.get("minecraft:iron_leggings"));
        assertTrue(prices.get("minecraft:iron_leggings") < prices.get("minecraft:golden_leggings"));
        assertTrue(prices.get("minecraft:golden_leggings") < prices.get("minecraft:diamond_leggings"));
        assertTrue(prices.get("minecraft:diamond_leggings") < prices.get("minecraft:netherite_leggings"));
    }

    private static int price(TradeDisplay trade) {
        return COIN_VALUES.get(BuiltInRegistries.ITEM.getKey(trade.costA().getItem()).getPath()) * trade.costA().getCount();
    }

    @Test
    void browserMatchesEveryActualOfferWithoutJei(MinecraftServer server) {
        assertFalse(ModList.get().isLoaded("jei"), "This server test must run without JEI");
        var displays = NumismaticVillagerTrades.buildTradeDisplays(server.registryAccess());
        var byProfession = displays.stream().collect(Collectors.groupingBy(TradeDisplay::professionId));
        assertEquals(14, byProfession.size());
        assertTrue(byProfession.containsKey(CreateNumismaticVillagerTrades.MINER.getId()));
        byProfession.forEach((professionId, professionDisplays) -> {
            var profession = BuiltInRegistries.VILLAGER_PROFESSION.get(professionId);
            var offers = NumismaticVillagerTrades.buildOffers(profession, server.registryAccess(), RandomSource.create(4));
            assertEquals(offers.size(), professionDisplays.size(), professionId.toString());
            for (int index = 0; index < offers.size(); index++) {
                var offer = offers.get(index);
                var display = professionDisplays.get(index);
                assertStackEquals(offer.getBaseCostA(), display.costA());
                assertStackEquals(offer.getCostB(), display.costB());
                assertStackEquals(offer.getResult(), display.result());
            }
        });
        assertTrue(displays.stream().allMatch(display -> "numismatics".equals(
                BuiltInRegistries.ITEM.getKey(display.costA().getItem()).getNamespace())));
        assertTrue(displays.stream().noneMatch(display -> display.result().isEmpty()));
    }

    @Test
    void jeiProfessionPagesKeepEveryOfferInAscendingPriceOrder(MinecraftServer server) {
        var displays = NumismaticVillagerTrades.buildTradeDisplays(server.registryAccess());
        var pages = NumismaticVillagerTrades.buildProfessionTrades(server.registryAccess());
        assertEquals(14, pages.size());
        assertEquals(pages.size(), pages.stream().map(page -> page.professionId()).distinct().count());
        assertEquals(displays.size(), pages.stream().mapToInt(page -> page.trades().size()).sum());
        for (var page : pages) {
            var expected = displays.stream().filter(display -> display.professionId().equals(page.professionId()))
                    .sorted(Comparator.comparingInt(VillagerTradeCatalogTest::price)).toList();
            assertEquals(expected.size(), page.trades().size(), page.professionId().toString());
            for (int index = 0; index < expected.size(); index++) {
                var actual = page.trades().get(index);
                assertEquals(page.professionId(), actual.professionId());
                assertStackEquals(expected.get(index).costA(), actual.costA());
                assertStackEquals(expected.get(index).costB(), actual.costB());
                assertStackEquals(expected.get(index).result(), actual.result());
            }
        }
        var librarian = pages.stream()
                .filter(page -> page.professionId().equals(ResourceLocation.withDefaultNamespace("librarian")))
                .findFirst().orElseThrow();
        assertTrue(librarian.trades().size() > 6, "Long lists must remain a single profession page");
        assertTrue(librarian.trades().stream().allMatch(trade -> trade.result().is(Items.ENCHANTED_BOOK)));
    }

    @Test
    void tagReloadPublishesCoinTradesForOtherViewers(MinecraftServer server) {
        var displays = NumismaticVillagerTrades.buildTradeDisplays(server.registryAccess());
        for (var professionId : displays.stream().map(TradeDisplay::professionId).distinct().toList()) {
            var profession = BuiltInRegistries.VILLAGER_PROFESSION.get(professionId);
            var published = VillagerTrades.TRADES.get(profession);
            assertNotNull(published, "Missing shared trade table for " + professionId);
            var expected = NumismaticVillagerTrades.buildOffers(profession, server.registryAccess(), RandomSource.create(0));
            assertEquals(expected.size(), published.get(1).length, professionId.toString());
            for (int index = 0; index < expected.size(); index++) {
                var offer = published.get(1)[index].getOffer(null, RandomSource.create(0));
                assertStackEquals(expected.get(index).getBaseCostA(), offer.getBaseCostA());
                assertStackEquals(expected.get(index).getResult(), offer.getResult());
            }
            for (int level = 2; level <= 5; level++) {
                assertEquals(0, published.get(level).length);
            }
        }
    }

    @Test
    void sharedTradeTableMatchesTheCatalogAndCopiesOfferStock(MinecraftServer server) {
        var displays = NumismaticVillagerTrades.buildTradeDisplays(server.registryAccess());
        for (var professionId : displays.stream().map(TradeDisplay::professionId).distinct().toList()) {
            var profession = BuiltInRegistries.VILLAGER_PROFESSION.get(professionId);
            var levels = vanillaLevels();
            var event = new VillagerTradesEvent(levels, profession, server.registryAccess());
            NumismaticVillagerTrades.onVillagerTrades(event);
            // A repeat reload must not accumulate duplicate entries.
            NumismaticVillagerTrades.onVillagerTrades(event);
            var expected = NumismaticVillagerTrades.buildOffers(profession, server.registryAccess(), RandomSource.create(0));
            assertEquals(expected.size(), levels.get(1).size(), professionId.toString());
            for (int level = 2; level <= 5; level++) {
                assertTrue(levels.get(level).isEmpty());
            }
            for (int index = 0; index < expected.size(); index++) {
                var listing = levels.get(1).get(index);
                var first = listing.getOffer(null, RandomSource.create(0));
                var second = listing.getOffer(null, RandomSource.create(0));
                assertNotSame(first, second);
                assertStackEquals(expected.get(index).getBaseCostA(), first.getBaseCostA());
                assertStackEquals(expected.get(index).getCostB(), first.getCostB());
                assertStackEquals(expected.get(index).getResult(), first.getResult());
                first.increaseUses();
                assertEquals(0, second.getUses());
            }
        }
    }

    @Test
    void librarianListingsKeepTheirEnchantmentAndBookCost(MinecraftServer server) {
        var books = NumismaticVillagerTrades.buildTradeDisplays(server.registryAccess()).stream()
                .filter(display -> display.professionId().equals(ResourceLocation.withDefaultNamespace("librarian")))
                .toList();
        assertFalse(books.isEmpty());
        for (var book : books) {
            assertTrue(book.result().is(Items.ENCHANTED_BOOK));
            assertTrue(book.costB().is(Items.BOOK));
            assertEquals(1, book.costB().getCount());
            assertNotNull(book.result().get(DataComponents.STORED_ENCHANTMENTS));
            assertFalse(book.result().get(DataComponents.STORED_ENCHANTMENTS).isEmpty());
        }
        assertTrue(books.stream().anyMatch(book -> book.result().get(DataComponents.STORED_ENCHANTMENTS)
                .entrySet().stream().anyMatch(entry -> entry.getIntValue() > 1)));
    }

    @Test
    void unsupportedProfessionsKeepTheirTradeTables(MinecraftServer server) {
        var levels = vanillaLevels();
        var first = levels.get(1).getFirst();
        NumismaticVillagerTrades.onVillagerTrades(new VillagerTradesEvent(levels, VillagerProfession.NONE, server.registryAccess()));
        assertEquals(5, levels.size());
        for (var listings : levels.values()) {
            assertEquals(1, listings.size());
            assertSame(first, listings.getFirst());
        }
    }

    @Test
    void disabledCoinTradesLeaveTheTableAloneAndHideTheCatalog(MinecraftServer server) {
        boolean previous = Config.ENABLE_COIN_TRADES.getAsBoolean();
        Config.ENABLE_COIN_TRADES.set(false);
        try {
            assertTrue(NumismaticVillagerTrades.buildTradeDisplays(server.registryAccess()).isEmpty());
            assertTrue(NumismaticVillagerTrades.buildProfessionTrades(server.registryAccess()).isEmpty());
            var levels = vanillaLevels();
            var first = levels.get(1).getFirst();
            NumismaticVillagerTrades.onVillagerTrades(new VillagerTradesEvent(levels, VillagerProfession.FARMER, server.registryAccess()));
            assertEquals(1, levels.get(1).size());
            assertSame(first, levels.get(1).getFirst());
        } finally {
            Config.ENABLE_COIN_TRADES.set(previous);
        }
    }

    private static Int2ObjectOpenHashMap<List<VillagerTrades.ItemListing>> vanillaLevels() {
        var levels = new Int2ObjectOpenHashMap<List<VillagerTrades.ItemListing>>();
        VillagerTrades.ItemListing vanilla = (entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD), new ItemStack(Items.APPLE), 10, 1, 0.05F);
        for (int level = 1; level <= 5; level++) {
            levels.put(level, new ArrayList<>(List.of(vanilla)));
        }
        return levels;
    }

    private static void assertStackEquals(ItemStack expected, ItemStack actual) {
        assertTrue(ItemStack.isSameItemSameComponents(expected, actual), () -> expected + " != " + actual);
        assertEquals(expected.getCount(), actual.getCount());
    }
}
