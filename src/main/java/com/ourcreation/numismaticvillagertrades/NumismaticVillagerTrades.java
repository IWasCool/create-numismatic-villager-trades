package com.ourcreation.numismaticvillagertrades;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public final class NumismaticVillagerTrades {
    private static final int XP = 0;
    private static final float PRICE_MULTIPLIER = 0.0F;

    private static final Map<String, Integer> COIN_VALUES = Map.of(
            "numismatics:spur", 1, "numismatics:bevel", 8, "numismatics:sprocket", 16,
            "numismatics:cog", 64, "numismatics:crown", 512, "numismatics:sun", 4096);

    private static final Map<String, Supplier<VillagerProfession>> PROFESSIONS = Map.ofEntries(
            Map.entry("armorer", () -> VillagerProfession.ARMORER),
            Map.entry("butcher", () -> VillagerProfession.BUTCHER),
            Map.entry("cartographer", () -> VillagerProfession.CARTOGRAPHER),
            Map.entry("cleric", () -> VillagerProfession.CLERIC),
            Map.entry("farmer", () -> VillagerProfession.FARMER),
            Map.entry("fisherman", () -> VillagerProfession.FISHERMAN),
            Map.entry("fletcher", () -> VillagerProfession.FLETCHER),
            Map.entry("leatherworker", () -> VillagerProfession.LEATHERWORKER),
            Map.entry("librarian", () -> VillagerProfession.LIBRARIAN),
            Map.entry("mason", () -> VillagerProfession.MASON),
            Map.entry("shepherd", () -> VillagerProfession.SHEPHERD),
            Map.entry("toolsmith", () -> VillagerProfession.TOOLSMITH),
            Map.entry("weaponsmith", () -> VillagerProfession.WEAPONSMITH),
            Map.entry("miner", CreateNumismaticVillagerTrades.MINER::get));

    private static Map<VillagerProfession, List<TradeGroup>> trades;

    private NumismaticVillagerTrades() {
    }

    public static void onVillagerTrades(VillagerTradesEvent event) {
        MerchantOffers offers = buildOffers(event.getType(), event.getRegistryAccess(), RandomSource.create(0L));
        if (offers.isEmpty()) {
            return;
        }

        // Trade viewers read this table instead of the offers on a particular villager.
        // Publish the same marketplace offers, including the miner, at the novice level.
        event.getTrades().values().forEach(List::clear);
        offers.forEach(offer -> event.getTrades().get(1).add((entity, random) -> offer.copy()));
    }

    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getTarget() instanceof Villager villager) || villager.isBaby()) {
            return;
        }

        List<TradeGroup> groups = trades().get(villager.getVillagerData().getProfession());
        if (!Config.ENABLE_COIN_TRADES.getAsBoolean() || groups == null || groups.isEmpty()) {
            return;
        }

        MerchantOffers offers = buildOffers(groups, villager.level().registryAccess(), villagerTradeRandom(villager));
        if (!offers.isEmpty()) {
            villager.overrideOffers(offers);
        }
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Villager villager) || villager.isBaby()) {
            return;
        }

        MerchantOffers offers = buildOffers(villager.getVillagerData().getProfession(), event.getLevel().registryAccess(), villagerTradeRandom(villager));
        if (!offers.isEmpty()) {
            villager.overrideOffers(offers);
        }
    }

    private static RandomSource villagerTradeRandom(Villager villager) {
        long seed = villager.getUUID().getMostSignificantBits() ^ villager.getUUID().getLeastSignificantBits();
        return RandomSource.create(seed);
    }

    private static MerchantOffers buildOffers(List<TradeGroup> groups, RegistryAccess registryAccess, RandomSource random) {
        MerchantOffers offers = new MerchantOffers();

        for (TradeGroup group : groups) {
            if (group.totalExpRequired() != 0) {
                continue;
            }

            int selectedSpecs = Math.min(group.numToSelect(), group.trades().size());
            for (int i = 0; i < selectedSpecs; i++) {
                for (MerchantOffer offer : createOffers(group.trades().get(i), registryAccess, random)) {
                    offers.add(offer);
                }
            }
        }

        // Use one stable price order for villagers, the browser, and recipe viewers.
        offers.sort(Comparator.comparingInt(offer -> tradePrice(offer.getBaseCostA())));
        return offers;
    }

    public static MerchantOffers buildOffers(VillagerProfession profession, RegistryAccess registryAccess, RandomSource random) {
        if (!Config.ENABLE_COIN_TRADES.getAsBoolean()) {
            return new MerchantOffers();
        }

        List<TradeGroup> groups = trades().get(profession);
        if (groups == null || groups.isEmpty()) {
            return new MerchantOffers();
        }

        return buildOffers(groups, registryAccess, random);
    }

    public static List<TradeDisplay> buildTradeDisplays(RegistryAccess registryAccess) {
        if (!Config.ENABLE_COIN_TRADES.getAsBoolean()) {
            return List.of();
        }

        List<TradeDisplay> displays = new ArrayList<>();
        trades().entrySet().stream()
                .sorted(java.util.Comparator.comparing(entry -> BuiltInRegistries.VILLAGER_PROFESSION.getKey(entry.getKey())))
                .forEach(entry -> {
                    ResourceLocation professionId = BuiltInRegistries.VILLAGER_PROFESSION.getKey(entry.getKey());
                    for (MerchantOffer offer : buildOffers(entry.getValue(), registryAccess, RandomSource.create(0L))) {
                        displays.add(new TradeDisplay(professionId, offer.getBaseCostA(), offer.getCostB(), offer.getResult()));
                    }
                });
        return List.copyOf(displays);
    }

    public static List<ProfessionTrades> buildProfessionTrades(RegistryAccess registryAccess) {
        Map<ResourceLocation, List<TradeDisplay>> byProfession = new LinkedHashMap<>();
        for (TradeDisplay display : buildTradeDisplays(registryAccess)) {
            byProfession.computeIfAbsent(display.professionId(), ignored -> new ArrayList<>()).add(display);
        }
        return byProfession.entrySet().stream()
                .map(entry -> new ProfessionTrades(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static int tradePrice(ItemStack cost) {
        String coin = BuiltInRegistries.ITEM.getKey(cost.getItem()).toString();
        return COIN_VALUES.get(coin) * cost.getCount();
    }

    private static List<MerchantOffer> createOffers(TradeSpec spec, RegistryAccess registryAccess, RandomSource random) {
        ItemCost costA = spec.costA().toItemCost();
        if (costA == null) {
            return List.of();
        }

        Optional<ItemCost> costB = Optional.empty();
        if (spec.costB() != null) {
            ItemCost itemCostB = spec.costB().toItemCost();
            if (itemCostB == null) {
                return List.of();
            }
            costB = Optional.of(itemCostB);
        }

        List<ItemStack> results = createResults(spec.result(), registryAccess, random);
        List<MerchantOffer> offers = new ArrayList<>();
        for (ItemStack result : results) {
            if (!result.isEmpty()) {
                offers.add(new MerchantOffer(costA, costB, result, spec.maxUses(), XP, PRICE_MULTIPLIER));
            }
        }
        return offers;
    }

    private static List<ItemStack> createResults(ResultSpec result, RegistryAccess registryAccess, RandomSource random) {
        if (result.enchantment() != null) {
            return List.of(createEnchantedBook(result.enchantment(), registryAccess));
        }

        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(result.id());
        return item.map(value -> List.of(new ItemStack(value, result.count()))).orElse(List.of());
    }

    private static ItemStack createEnchantedBook(EnchantmentSpec enchantment, RegistryAccess registryAccess) {
        if (registryAccess == null) {
            return new ItemStack(Items.ENCHANTED_BOOK);
        }

        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, enchantment.id());
        Holder<Enchantment> holder = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
        if (holder == null) {
            return new ItemStack(Items.BOOK);
        }

        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, enchantment.level()));
    }

    private static Map<VillagerProfession, List<TradeGroup>> trades() {
        if (trades == null) {
            trades = loadTrades();
        }
        return trades;
    }

    private static Map<VillagerProfession, List<TradeGroup>> loadTrades() {
        var builder = new java.util.HashMap<VillagerProfession, List<TradeGroup>>();

        PROFESSIONS.forEach((name, professionSupplier) -> {
            String path = "data/" + CreateNumismaticVillagerTrades.MODID + "/trades/" + name + ".json";
            try (InputStream stream = NumismaticVillagerTrades.class.getClassLoader().getResourceAsStream(path)) {
                if (stream == null) {
                    CreateNumismaticVillagerTrades.LOGGER.warn("Missing villager trade data resource: {}", path);
                    return;
                }

                JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                builder.put(professionSupplier.get(), parseTradeGroups(root));
            } catch (RuntimeException exception) {
                CreateNumismaticVillagerTrades.LOGGER.error("Failed to parse villager trade data for {}", name, exception);
            } catch (Exception exception) {
                CreateNumismaticVillagerTrades.LOGGER.error("Failed to load villager trade data for {}", name, exception);
            }
        });

        int count = builder.values().stream()
                .flatMap(List::stream)
                .mapToInt(group -> group.trades().size())
                .sum();
        CreateNumismaticVillagerTrades.LOGGER.info("Loaded {} numismatic villager trade specs for {} professions.", count, builder.size());
        return Map.copyOf(builder);
    }

    private static List<TradeGroup> parseTradeGroups(JsonObject root) {
        List<TradeGroup> groups = new ArrayList<>();

        for (JsonElement tierElement : root.getAsJsonArray("tiers")) {
            JsonObject tier = tierElement.getAsJsonObject();
            int totalExpRequired = tier.get("total_exp_required").getAsInt();
            JsonArray groupArray = tier.getAsJsonArray("groups");
            for (JsonElement groupElement : groupArray) {
                JsonObject group = groupElement.getAsJsonObject();
                List<TradeSpec> specs = new ArrayList<>();
                for (JsonElement tradeElement : group.getAsJsonArray("trades")) {
                    JsonObject trade = tradeElement.getAsJsonObject();
                    CostSpec costA = parseCost(trade.getAsJsonObject("cost_a"));
                    CostSpec costB = trade.has("cost_b") ? parseCost(trade.getAsJsonObject("cost_b")) : null;
                    ResultSpec result = parseResult(trade.getAsJsonObject("result"));
                    int maxUses = trade.has("max_uses") ? trade.get("max_uses").getAsInt() : Config.MAX_USES.getAsInt();
                    specs.add(new TradeSpec(costA, costB, result, maxUses));
                }

                groups.add(new TradeGroup(totalExpRequired, group.get("num_to_select").getAsInt(), List.copyOf(specs)));
            }
        }

        return List.copyOf(groups);
    }

    private static CostSpec parseCost(JsonObject cost) {
        ResourceLocation itemId = ResourceLocation.parse(cost.get("name").getAsString());
        return new CostSpec(itemId, parseCount(cost));
    }

    private static ResultSpec parseResult(JsonObject result) {
        ResourceLocation id = ResourceLocation.parse(result.get("name").getAsString());
        EnchantmentSpec enchantment = null;
        if (result.has("functions")) {
            for (JsonElement functionElement : result.getAsJsonArray("functions")) {
                JsonObject function = functionElement.getAsJsonObject();
                if ("villagerconfig:enchant_randomly".equals(function.get("function").getAsString())) {
                    JsonArray includes = function.getAsJsonArray("include");
                    enchantment = new EnchantmentSpec(
                            ResourceLocation.parse(includes.get(0).getAsString()),
                            function.get("min_level").getAsInt());
                }
            }
        }

        return new ResultSpec(id, parseCount(result), enchantment);
    }

    private static int parseCount(JsonObject entry) {
        if (!entry.has("functions")) {
            return 1;
        }

        for (JsonElement functionElement : entry.getAsJsonArray("functions")) {
            JsonObject function = functionElement.getAsJsonObject();
            if ("minecraft:set_count".equals(function.get("function").getAsString())) {
                return function.get("count").getAsInt();
            }
        }

        return 1;
    }

    private record TradeGroup(int totalExpRequired, int numToSelect, List<TradeSpec> trades) {
    }

    private record CostSpec(ResourceLocation itemId, int count) {
        ItemCost toItemCost() {
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
            return item.map(value -> new ItemCost(value, count)).orElse(null);
        }
    }

    private record TradeSpec(CostSpec costA, CostSpec costB, ResultSpec result, int maxUses) {
    }

    private record ResultSpec(ResourceLocation id, int count, EnchantmentSpec enchantment) {
    }

    private record EnchantmentSpec(ResourceLocation id, int level) {
    }

    public record TradeDisplay(ResourceLocation professionId, ItemStack costA, ItemStack costB, ItemStack result) {
        public TradeDisplay {
            costA = costA.copy();
            costB = costB.copy();
            result = result.copy();
        }
    }

    public record ProfessionTrades(ResourceLocation professionId, List<TradeDisplay> trades) {
        public ProfessionTrades {
            trades = List.copyOf(trades);
        }
    }
}
