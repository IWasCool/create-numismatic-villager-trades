package com.ourcreation.numismaticvillagertrades.viewertest;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Controlled client test mod, excluded from the published mod JAR. */
@Mod(value = "numismatic_viewer_tests", dist = Dist.CLIENT)
public final class ViewerTestClient {
    private final String profile = System.getProperty("numismatic.viewerTest.profile");
    private final long started = System.nanoTime();
    private int stage;
    private int screenTicks;
    private Result result;

    public ViewerTestClient() {
        NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private void tick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        require(System.nanoTime() - started < 180_000_000_000L, "Viewer client test timed out: " + profile + " stage " + stage);
        if (client.level == null) return;
        require(Arrays.stream(client.options.keyMappings).noneMatch(key -> key.getName().contains("numismatic_villager_trades")),
                "The removed standalone trade-browser keybind is still registered");
        if (stage == 0) {
            result = switch (profile) {
                case "emi", "jei-emi" -> EmiViewerCheck.checkAndOpen();
                case "rei" -> ReiViewerCheck.checkAndOpen();
                case "jei" -> JeiViewerCheck.checkAndOpen();
                case "none" -> new Result("none", 0, 0, "No standalone viewer/keybind");
                default -> throw new IllegalStateException("Unknown viewer test profile: " + profile);
            };
            if (result == null) return;
            stage = 1;
        } else if (stage == 1 && ++screenTicks >= 40) {
            if (!profile.equals("none")) {
                require(client.screen != null, "Native recipe viewer did not open");
                require(!client.screen.getClass().getName().startsWith("com.ourcreation"), "A standalone mod browser opened");
            }
            Screenshot.grab(client.gameDirectory, "viewer-" + profile + ".png", client.getMainRenderTarget(), message -> {});
            stage = 2;
            screenTicks = 0;
        } else if (stage == 2 && ++screenTicks >= 40) {
            stage = 3;
            client.stop();
        }
    }

    static void require(boolean valid, String message) {
        if (!valid) throw new IllegalStateException(message);
    }

    public record Result(String viewer, int registeredPages, int checkedOffers, String screen) {
    }
}
