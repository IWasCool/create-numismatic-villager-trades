package com.ourcreation.numismaticvillagertrades;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CreateNumismaticVillagerTrades.MODID)
public class CreateNumismaticVillagerTrades {
    public static final String MODID = "numismatic_villager_trades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MODID);

    public static final DeferredHolder<PoiType, PoiType> MINER_POI = POI_TYPES.register("miner",
            () -> new PoiType(
                    ImmutableSet.<net.minecraft.world.level.block.state.BlockState>builder()
                            .addAll(Blocks.ANVIL.getStateDefinition().getPossibleStates())
                            .addAll(Blocks.CHIPPED_ANVIL.getStateDefinition().getPossibleStates())
                            .addAll(Blocks.DAMAGED_ANVIL.getStateDefinition().getPossibleStates())
                            .build(),
                    1,
                    1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> MINER = VILLAGER_PROFESSIONS.register("miner",
            () -> new VillagerProfession(
                    "miner",
                    holder -> holder.is(MINER_POI.getKey()),
                    holder -> holder.is(MINER_POI.getKey()),
                    ImmutableSet.of(),
                    ImmutableSet.of(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL),
                    SoundEvents.VILLAGER_WORK_TOOLSMITH));

    public CreateNumismaticVillagerTrades(IEventBus modEventBus, ModContainer modContainer) {
        POI_TYPES.register(modEventBus);
        VILLAGER_PROFESSIONS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NumismaticVillagerTrades::onVillagerTrades);
        NeoForge.EVENT_BUS.addListener(NumismaticVillagerTrades::onPlayerInteract);
        NeoForge.EVENT_BUS.addListener(NumismaticVillagerTrades::onEntityJoinLevel);
    }
}
