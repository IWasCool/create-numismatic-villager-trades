package com.ourcreation.numismaticvillagertrades.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ourcreation.numismaticvillagertrades.NumismaticVillagerTrades;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

@Mixin(Villager.class)
public abstract class VillagerTradeMixin extends AbstractVillager {
    protected VillagerTradeMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract VillagerData getVillagerData();

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void numismaticVillagerTrades$updateTrades(CallbackInfo callbackInfo) {
        MerchantOffers offers = NumismaticVillagerTrades.buildOffers(
                getVillagerData().getProfession(),
                registryAccess(),
                getRandom());

        if (!offers.isEmpty()) {
            getOffers().clear();
            getOffers().addAll(offers);
            callbackInfo.cancel();
        }
    }
}
