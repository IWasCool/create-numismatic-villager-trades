package com.ourcreation.numismaticvillagertrades;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.ourcreation.numismaticvillagertrades.client.VillagerTradeBrowser;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CreateNumismaticVillagerTrades.MODID, dist = Dist.CLIENT)
public class CreateNumismaticVillagerTradesClient {
    private final KeyMapping browseTrades = new KeyMapping("key.numismatic_villager_trades.browse_trades",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.numismatic_villager_trades");

    public CreateNumismaticVillagerTradesClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::registerKeys);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(browseTrades);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        while (browseTrades.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && minecraft.screen == null) {
                VillagerTradeBrowser.open(null);
            }
        }
    }
}
