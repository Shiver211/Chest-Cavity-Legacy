package com.shiver.chestcavity.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.organs.OrganManager;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.chest.types.FallbackChestCavityType;
import com.shiver.chestcavity.chest.types.GeneratedChestCavityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class DataLoaders {

    public static final String DATA_ROOT = "chestcavity_data";
    public static final String ASSET_DATA_PATH = "assets/" + Tags.MOD_ID + "/" + DATA_ROOT;
    public static final String CONFIG_DATA_PATH = "config/" + Tags.MOD_ID + "/data";
    public static final ResourceLocation FALLBACK_ID = new ResourceLocation(Tags.MOD_ID, "fallback");

    private static final ChestCavityType FALLBACK_TYPE = new FallbackChestCavityType();
    private static final Map<ResourceLocation, ChestCavityType> CHEST_CAVITY_TYPES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> ENTITY_ASSIGNMENTS = new LinkedHashMap<>();
    private static final ResourceLocation PLAYER_ENTITY_ID = new ResourceLocation("minecraft", "player");

    private DataLoaders() {
    }

    public static void reload(File gameDir) {
        CHEST_CAVITY_TYPES.clear();
        ENTITY_ASSIGNMENTS.clear();
        OrganManager.clear();
        CHEST_CAVITY_TYPES.put(FALLBACK_ID, FALLBACK_TYPE);

        File configDataDir = gameDir == null ? new File(CONFIG_DATA_PATH) : new File(gameDir, CONFIG_DATA_PATH);
        Set<String> scannedDirectories = new HashSet<>();
        for (File assetDir : getAssetDataDirectories(gameDir)) {
            loadDirectory(assetDir, "asset data", scannedDirectories);
        }
        loadClasspathAssets(scannedDirectories);
        loadDirectory(configDataDir, "config data", scannedDirectories);

        ChestCavityLegacy.LOGGER.info(
                "Loaded chest cavity data. assetPath={}, configPath={}, organs={}, types={}, entityAssignments={}",
                ASSET_DATA_PATH,
                configDataDir == null ? CONFIG_DATA_PATH : configDataDir.getPath(),
                OrganData.getRegistry().size(),
                Math.max(0, CHEST_CAVITY_TYPES.size() - 1),
                ENTITY_ASSIGNMENTS.size());
    }

    public static void registerType(ResourceLocation id, ChestCavityType type) {
        if (id != null && type != null) {
            CHEST_CAVITY_TYPES.put(id, type);
        }
    }

    public static void registerEntityAssignment(ResourceLocation entityId, ResourceLocation typeId) {
        if (entityId != null && typeId != null) {
            ENTITY_ASSIGNMENTS.put(entityId, typeId);
        }
    }

    public static ChestCavityType getType(ResourceLocation id) {
        ChestCavityType type = CHEST_CAVITY_TYPES.get(id);
        return type == null ? FALLBACK_TYPE : type;
    }

    public static ChestCavityType getFallbackType() {
        return FALLBACK_TYPE;
    }

    public static ResourceLocation getAssignedTypeId(ResourceLocation entityId) {
        return ENTITY_ASSIGNMENTS.get(entityId);
    }

    public static Map<ResourceLocation, ChestCavityType> getTypes() {
        return Collections.unmodifiableMap(CHEST_CAVITY_TYPES);
    }

    public static Map<ResourceLocation, ResourceLocation> getEntityAssignments() {
        return Collections.unmodifiableMap(ENTITY_ASSIGNMENTS);
    }

    private static List<File> getAssetDataDirectories(File gameDir) {
        List<File> directories = new ArrayList<>();
        directories.add(new File("build/resources/main/" + ASSET_DATA_PATH));
        directories.add(new File("src/main/resources/" + ASSET_DATA_PATH));
        directories.add(new File(ASSET_DATA_PATH));
        if (gameDir != null) {
            directories.add(new File(gameDir, ASSET_DATA_PATH));
        }
        return directories;
    }

    private static void loadClasspathAssets(Set<String> scannedDirectories) {
        URL resource = DataLoaders.class.getClassLoader().getResource(ASSET_DATA_PATH);
        if (resource == null) {
            return;
        }
        if ("file".equals(resource.getProtocol())) {
            try {
                loadDirectory(new File(resource.toURI()), "classpath asset data", scannedDirectories);
            } catch (URISyntaxException e) {
                ChestCavityLegacy.LOGGER.warn("Unable to scan classpath chest cavity data at {}", resource, e);
            }
            return;
        }
        if ("jar".equals(resource.getProtocol())) {
            loadJarAssets(resource);
        }
    }

    private static void loadJarAssets(URL resource) {
        try {
            JarURLConnection connection = (JarURLConnection) resource.openConnection();
            String rootEntryName = connection.getEntryName();
            if (rootEntryName == null) {
                return;
            }

            JarFile jarFile = connection.getJarFile();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.startsWith(rootEntryName + "/") || !name.endsWith(".json")) {
                    continue;
                }

                String relativePath = name.substring(rootEntryName.length() + 1);
                try (Reader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry), StandardCharsets.UTF_8))) {
                    loadJson(relativePath, reader);
                }
            }
        } catch (IOException e) {
            ChestCavityLegacy.LOGGER.warn("Unable to scan jar chest cavity data at {}", resource, e);
        }
    }

    private static void loadDirectory(File root, String sourceName, Set<String> scannedDirectories) {
        if (root == null || !root.isDirectory()) {
            return;
        }

        try {
            String canonicalPath = root.getCanonicalPath();
            if (!scannedDirectories.add(canonicalPath)) {
                return;
            }
        } catch (IOException e) {
            ChestCavityLegacy.LOGGER.warn("Unable to resolve chest cavity data directory {}", root.getPath(), e);
            return;
        }

        Path rootPath = root.toPath();
        ChestCavityLegacy.LOGGER.info("Loading chest cavity {} from {}", sourceName, root.getPath());
        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .forEach(path -> loadJsonFile(rootPath, path));
        } catch (IOException e) {
            ChestCavityLegacy.LOGGER.warn("Unable to scan chest cavity data directory {}", root.getPath(), e);
        }
    }

    private static void loadJsonFile(Path rootPath, Path jsonPath) {
        String relativePath = rootPath.relativize(jsonPath).toString().replace(File.separatorChar, '/');
        try (Reader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(jsonPath), StandardCharsets.UTF_8))) {
            loadJson(relativePath, reader);
        } catch (Exception e) {
            ResourceLocation id = new ResourceLocation(Tags.MOD_ID, relativePath);
            ChestCavityLegacy.LOGGER.warn("Unable to load chest cavity data {}", id, e);
        }
    }

    private static void loadJson(String relativePath, Reader reader) {
        ResourceLocation id = new ResourceLocation(Tags.MOD_ID, relativePath);
        JsonElement root = new JsonParser().parse(reader);
        if (root == null || !root.isJsonObject()) {
            ChestCavityLegacy.LOGGER.warn("Skipping chest cavity data {} because it is not a JSON object.", id);
            return;
        }
        JsonObject json = root.getAsJsonObject();
        if (relativePath.startsWith("organs/")) {
            OrganManager.load(id, json);
        } else if (relativePath.startsWith("types/")) {
            loadType(id, json);
        } else if (relativePath.startsWith("entity_assignment/")) {
            loadEntityAssignment(id, json);
        }
    }

    private static void loadType(ResourceLocation id, JsonObject json) {
        GeneratedChestCavityType type = new GeneratedChestCavityType();
        List<Integer> forbiddenSlots = readForbiddenSlots(id, json.get("forbiddenSlots"));
        type.setForbiddenSlots(forbiddenSlots);
        if (json.has("defaultChestCavity")) {
            type.setDefaultChestCavity(readDefaultChestCavity(id, json.get("defaultChestCavity"), forbiddenSlots));
        }
        if (json.has("baseOrganScores")) {
            type.setBaseOrganScores(readOrganScores(id, json.get("baseOrganScores")));
        }
        if (json.has("exceptionalOrgans")) {
            type.setExceptionalOrgans(readExceptionalOrgans(id, json.get("exceptionalOrgans")));
        }
        if (json.has("bossChestCavity")) {
            type.setBossChestCavity(json.get("bossChestCavity").getAsBoolean());
        }
        if (json.has("playerChestCavity")) {
            type.setPlayerChestCavity(json.get("playerChestCavity").getAsBoolean());
        }
        if (json.has("dropRateMultiplier")) {
            type.setDropRateMultiplier(json.get("dropRateMultiplier").getAsFloat());
        }
        registerType(id, type);
    }

    private static void loadEntityAssignment(ResourceLocation id, JsonObject json) {
        if (!json.has("chestcavity") || !json.has("entities")) {
            ChestCavityLegacy.LOGGER.warn("Skipping entity assignment {} because chestcavity or entities is missing.", id);
            return;
        }

        ResourceLocation typeId = new ResourceLocation(json.get("chestcavity").getAsString());
        JsonElement entitiesElement = json.get("entities");
        if (!entitiesElement.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping entity assignment {} because entities is not an array.", id);
            return;
        }

        for (JsonElement entityElement : entitiesElement.getAsJsonArray()) {
            try {
                ResourceLocation entityId = new ResourceLocation(entityElement.getAsString());
                if (isEntityPresent(entityId)) {
                    registerEntityAssignment(entityId, typeId);
                } else {
                    ChestCavityLegacy.LOGGER.info("Skipping entity assignment {} -> {} because the entity is not registered in 1.12.2.", entityId, typeId);
                }
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Skipping invalid entity entry in {}.", id, e);
            }
        }
    }

    private static ChestCavityInventory readDefaultChestCavity(ResourceLocation id, JsonElement element, List<Integer> forbiddenSlots) {
        ChestCavityInventory inventory = new ChestCavityInventory();
        if (element == null || !element.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping defaultChestCavity in {} because it is not an array.", id);
            return inventory;
        }

        int index = 0;
        for (JsonElement entry : element.getAsJsonArray()) {
            index++;
            try {
                if (!entry.isJsonObject()) {
                    ChestCavityLegacy.LOGGER.warn("Skipping defaultChestCavity entry {} in {} because it is not an object.", index, id);
                    continue;
                }
                JsonObject object = entry.getAsJsonObject();
                if (!object.has("item") || !object.has("position")) {
                    ChestCavityLegacy.LOGGER.warn("Skipping defaultChestCavity entry {} in {} because item or position is missing.", index, id);
                    continue;
                }

                ResourceLocation itemId = new ResourceLocation(object.get("item").getAsString());
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item == null) {
                    ChestCavityLegacy.LOGGER.info("Skipping defaultChestCavity entry {} in {} because item {} is not registered in 1.12.2.", index, id, itemId);
                    continue;
                }

                int position = object.get("position").getAsInt();
                if (position < 0 || position >= inventory.size()) {
                    ChestCavityLegacy.LOGGER.warn("Skipping defaultChestCavity entry {} in {} because position {} is out of bounds.", index, id, position);
                    continue;
                }
                if (forbiddenSlots.contains(position)) {
                    ChestCavityLegacy.LOGGER.warn("Skipping defaultChestCavity entry {} in {} because position {} is forbidden.", index, id, position);
                    continue;
                }

                int count = object.has("count") ? object.get("count").getAsInt() : item.getItemStackLimit();
                inventory.setStack(position, new ItemStack(item, count));
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to parse defaultChestCavity entry {} in {}.", index, id, e);
            }
        }
        return inventory;
    }

    private static Map<String, Float> readOrganScores(ResourceLocation id, JsonElement element) {
        Map<String, Float> scores = new LinkedHashMap<>();
        if (element == null || !element.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping organ scores in {} because they are not an array.", id);
            return scores;
        }

        for (JsonElement entry : element.getAsJsonArray()) {
            try {
                if (!entry.isJsonObject()) {
                    continue;
                }
                JsonObject object = entry.getAsJsonObject();
                if (!object.has("id") || !object.has("value")) {
                    ChestCavityLegacy.LOGGER.warn("Skipping organ score entry in {} because id or value is missing.", id);
                    continue;
                }
                scores.put(object.get("id").getAsString(), object.get("value").getAsFloat());
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to parse organ score entry in {}.", id, e);
            }
        }
        return scores;
    }

    private static List<GeneratedChestCavityType.ExceptionalOrgan> readExceptionalOrgans(ResourceLocation id, JsonElement element) {
        List<GeneratedChestCavityType.ExceptionalOrgan> organs = new ArrayList<>();
        if (element == null || !element.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping exceptional organs in {} because they are not an array.", id);
            return organs;
        }

        int index = 0;
        for (JsonElement entry : element.getAsJsonArray()) {
            index++;
            try {
                if (!entry.isJsonObject()) {
                    continue;
                }
                JsonObject object = entry.getAsJsonObject();
                if (!object.has("ingredient") || !object.has("value")) {
                    ChestCavityLegacy.LOGGER.warn("Skipping exceptional organ entry {} in {} because ingredient or value is missing.", index, id);
                    continue;
                }

                JsonObject ingredient = object.getAsJsonObject("ingredient");
                Item item = null;
                String oreName = null;
                if (ingredient.has("item")) {
                    ResourceLocation itemId = new ResourceLocation(ingredient.get("item").getAsString());
                    item = ForgeRegistries.ITEMS.getValue(itemId);
                    if (item == null) {
                        ChestCavityLegacy.LOGGER.info("Skipping exceptional organ entry {} in {} because item {} is not registered in 1.12.2.", index, id, itemId);
                        continue;
                    }
                } else if (ingredient.has("ore")) {
                    oreName = ingredient.get("ore").getAsString();
                } else if (ingredient.has("tag")) {
                    oreName = mapTagToOreName(ingredient.get("tag").getAsString());
                }
                if (item == null && (oreName == null || oreName.isEmpty())) {
                    ChestCavityLegacy.LOGGER.warn("Skipping exceptional organ entry {} in {} because ingredient has no supported item, ore, or tag.", index, id);
                    continue;
                }
                organs.add(new GeneratedChestCavityType.ExceptionalOrgan(item, oreName, readOrganScores(id, object.get("value"))));
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to parse exceptional organ entry {} in {}.", index, id, e);
            }
        }
        return organs;
    }

    private static String mapTagToOreName(String tag) {
        if ("minecraft:logs".equals(tag)) {
            return "logWood";
        }
        if ("minecraft:leaves".equals(tag)) {
            return "treeLeaves";
        }
        return null;
    }

    private static List<Integer> readForbiddenSlots(ResourceLocation id, JsonElement element) {
        List<Integer> slots = new ArrayList<>();
        if (element == null) {
            return slots;
        }
        if (!element.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping forbiddenSlots in {} because it is not an array.", id);
            return slots;
        }

        for (JsonElement entry : element.getAsJsonArray()) {
            try {
                slots.add(entry.getAsInt());
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to parse forbidden slot in {}.", id, e);
            }
        }
        return slots;
    }

    private static boolean isEntityPresent(ResourceLocation entityId) {
        return PLAYER_ENTITY_ID.equals(entityId) || ForgeRegistries.ENTITIES.getValue(entityId) != null;
    }
}
