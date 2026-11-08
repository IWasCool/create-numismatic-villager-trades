package com.ourcreation.numismaticvillagertrades.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Amendments can replace merchant offer buttons with its loom/stonecutter style.
 * Render these buttons with Minecraft's normal button renderer instead.
 */
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.MerchantScreen$TradeOfferButton", priority = 900)
public abstract class MerchantTradeButtonMixin extends Button {
    protected MerchantTradeButtonMixin(int x, int y, int width, int height, Component message, OnPress onPress,
            CreateNarration narration) {
        super(x, y, width, height, message, onPress, narration);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true, require = 0)
    private void numismaticVillagerTrades$renderVanillaTradeButton(GuiGraphics guiGraphics, int mouseX, int mouseY,
            float partialTick, CallbackInfo callbackInfo) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        callbackInfo.cancel();
    }
}
