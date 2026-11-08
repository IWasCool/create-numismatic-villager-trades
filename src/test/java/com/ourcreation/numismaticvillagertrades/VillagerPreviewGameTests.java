package com.ourcreation.numismaticvillagertrades;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.ourcreation.numismaticvillagertrades.compat.VillagerPreviewFactory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Run with: gradlew runGameTestServer -PtestVillagerPreviews=true
 * Add -PtestVillagersReborn=true to verify the installed NPC mod and both skin themes.
 * Both runs use isolated worlds under build/.
 */
@GameTestHolder(CreateNumismaticVillagerTrades.MODID)
@PrefixGameTestTemplate(false)
public class VillagerPreviewGameTests {
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void professionPreviewsUseAvailableNpcModelsAndValidThemedSkins(GameTestHelper helper) throws Exception {
        MinecraftServer server = helper.getLevel().getServer();
        if (!ModList.get().isLoaded("slimpatch")) {
            verifyPreviews(server, false, "vanilla");
            helper.succeed();
            return;
        }

        // Reflection is confined to the test so the same test class also loads
        // in the normal runtime, where the optional NPC mod is absent.
        Class<?> skinDataType = Class.forName("com.javic.slimpatch.data.WorldSkinData");
        Object skinData = skinDataType.getMethod("get", ServerLevel.class).invoke(null, server.overworld());
        String originalTheme = (String) skinDataType.getMethod("getTheme").invoke(skinData);
        try {
            for (String theme : new String[] {"modern", "fantasy"}) {
                skinDataType.getMethod("setTheme", String.class).invoke(skinData, theme);
                verifyPreviews(server, true, theme);
            }
        } finally {
            skinDataType.getMethod("setTheme", String.class).invoke(skinData, originalTheme);
        }
        helper.succeed();
    }

    private static void verifyPreviews(MinecraftServer server, boolean reborn, String theme) throws Exception {
        Set<ResourceLocation> models = new HashSet<>();
        var professions = NumismaticVillagerTrades.buildProfessionTrades(server.registryAccess());
        require(professions.size() == 14, "All fourteen professions must have a preview");
        for (var profession : professions) {
            Villager preview = VillagerPreviewFactory.create(server.overworld(), profession.professionId());
            Villager second = VillagerPreviewFactory.create(server.overworld(), profession.professionId());
            ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(preview.getType());
            models.add(type);
            require(preview.getType() == second.getType(), "Each profession must keep its NPC model");
            require(BuiltInRegistries.VILLAGER_PROFESSION.get(profession.professionId())
                    == preview.getVillagerData().getProfession(), "The preview must use the listed profession");
            require(!preview.isBaby(), "Profession previews must be adults");
            require(preview.isNoAi(), "Preview AI must be disabled");
            require(preview.isSilent(), "Previews must be silent");
            require(server.overworld().getEntity(preview.getUUID()) == null, "Previews must never spawn into the world");

            if (reborn) {
                ResourceLocation skin = texture(preview);
                require(skin.equals(texture(second)), "Reopening a profession should keep its representative skin");
                require("slimpatch".equals(skin.getNamespace()), "Use a skin from Villagers Reborn");
                String gender = type.getPath().startsWith("female") ? "female" : "male";
                String directory = "textures/entity/custom_villager/" + (theme.equals("fantasy") ? "fantasy/" : "") + gender + "/";
                require(skin.getPath().startsWith(directory), "Use the NPC mod's current theme: " + skin);
                try (var stream = preview.getClass().getClassLoader().getResourceAsStream("assets/" + skin.getNamespace() + "/" + skin.getPath())) {
                    require(stream != null, "Preview skin must exist in the installed mod: " + skin);
                    require(Arrays.equals(new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10}, stream.readNBytes(8)),
                            "Preview skin must be a valid PNG: " + skin);
                }
            }
        }
        require(models.equals(reborn
                ? Set.of(ResourceLocation.parse("slimpatch:male_villager"), ResourceLocation.parse("slimpatch:female_villager"))
                : Set.of(ResourceLocation.withDefaultNamespace("villager"))), "Use the registered NPC model types");
    }

    private static ResourceLocation texture(Villager preview) throws ReflectiveOperationException {
        return (ResourceLocation) preview.getClass().getMethod("getSkinTexture").invoke(preview);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
