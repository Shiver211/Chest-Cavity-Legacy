package com.shiver.chestcavity.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.chest.ChestCavityInventory;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.content.BodyTypeDef;
import com.shiver.chestcavity.content.CompiledContent;
import com.shiver.chestcavity.content.ContentManifest;
import com.shiver.chestcavity.content.ContentRegistry;
import com.shiver.chestcavity.content.ExceptionalOrganDef;
import com.shiver.chestcavity.content.OrganDef;
import com.shiver.chestcavity.layout.ChestLayoutDef;
import com.shiver.chestcavity.layout.LayoutMigrationStrategy;
import com.shiver.chestcavity.layout.SlotRule;
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class DataLoaders {

    public static final String DATA_ROOT = "chestcavity_data";
    public static final String ASSET_DATA_PATH = "assets/" + Tags.MOD_ID + "/" + DATA_ROOT;
    public static final String CONFIG_DATA_PATH = "config/" + Tags.MOD_ID + "/data";
    public static final String FALLBACK_ID = "fallback";

    private static final ResourceLocation PLAYER_ENTITY_ID = new ResourceLocation("minecraft", "player");
    private static ContentManifest loadingManifest;

    private DataLoaders() {
    }

    public static void reload(File gameDir) {
        loadingManifest = ContentRegistry.createReloadManifest();

        File configDataDir = gameDir == null ? new File(CONFIG_DATA_PATH) : new File(gameDir, CONFIG_DATA_PATH);
        Set<String> scannedDirectories = new HashSet<>();
        loadClasspathAssets();
        loadDirectory(configDataDir, "config data", scannedDirectories);

        ContentRegistry.applyScriptManifest(loadingManifest);
        ContentRegistry.publish(loadingManifest);
        CompiledContent compiled = ContentRegistry.getCompiled();
        loadingManifest = null;

        ChestCavityLegacy.LOGGER.info(
                "Loaded chest cavity data. classpathAssetPath={}, configPath={}, organs={}, types={}, entityAssignments={}, layouts={}",
                ASSET_DATA_PATH,
                configDataDir == null ? CONFIG_DATA_PATH : configDataDir.getPath(),
                compiled.getOrgans().size(),
                Math.max(0, compiled.getTypes().size() - 1),
                compiled.getEntityAssignments().size(),
                compiled.getLayouts().size());
    }

    public static ChestCavityType getType(String id) {
        return ContentRegistry.getCompiled().getType(id);
    }

    public static ChestCavityType getFallbackType() {
        return ContentRegistry.getCompiled().getFallbackType();
    }

    public static String getAssignedTypeId(ResourceLocation entityId) {
        return ContentRegistry.getCompiled().getAssignedTypeId(entityId);
    }

    public static Map<String, ChestCavityType> getTypes() {
        return ContentRegistry.getCompiled().getTypes();
    }

    public static Map<ResourceLocation, String> getEntityAssignments() {
        return ContentRegistry.getCompiled().getEntityAssignments();
    }

    private static ContentManifest manifest() {
        if (loadingManifest == null) {
            loadingManifest = ContentRegistry.createReloadManifest();
        }
        return loadingManifest;
    }

    private static void loadClasspathAssets() {
        URL resource = DataLoaders.class.getClassLoader().getResource(ASSET_DATA_PATH);
        if (resource == null) {
            ChestCavityLegacy.LOGGER.warn("Unable to locate bundled chest cavity data at {}", ASSET_DATA_PATH);
            return;
        }
        if ("file".equals(resource.getProtocol())) {
            try {
                loadDirectory(new File(resource.toURI()), "classpath asset data", new HashSet<>());
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
            loadOrgan(id, json);
        } else if (relativePath.startsWith("types/")) {
            loadType(typeIdFromPath(relativePath), id, json);
        } else if (relativePath.startsWith("layouts/")) {
            loadLayout(layoutIdFromPath(relativePath), id, json);
        } else if (relativePath.startsWith("entity_assignment/")) {
            loadEntityAssignment(id, json);
        }
    }

    private static String typeIdFromPath(String relativePath) {
        String fileName = relativePath.substring(relativePath.lastIndexOf('/') + 1);
        return fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
    }

    private static ResourceLocation layoutIdFromPath(String relativePath) {
        String path = relativePath.substring("layouts/".length());
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - 5);
        }
        return new ResourceLocation(Tags.MOD_ID, path);
    }

    private static void loadOrgan(ResourceLocation id, JsonObject json) {
        if (!json.has("itemID") || !json.has("organScores")) {
            ChestCavityLegacy.LOGGER.warn("Skipping organ {} because itemID or organScores is missing.", id);
            return;
        }

        ResourceLocation itemId = new ResourceLocation(json.get("itemID").getAsString());
        if (ForgeRegistries.ITEMS.getValue(itemId) == null) {
            ChestCavityLegacy.LOGGER.info("Skipping organ {} because item {} is not registered in 1.12.2.", id, itemId);
            return;
        }

        OrganData data = new OrganData();
        if (json.has("pseudoOrgan")) {
            data.setPseudoOrgan(json.get("pseudoOrgan").getAsBoolean());
        }
        data.setOrganScores(readOrganScores(id, json.get("organScores")));
        manifest().registerOrgan(new OrganDef(itemId, data));
    }

    private static void loadLayout(ResourceLocation fallbackId, ResourceLocation sourceId, JsonObject json) {
        ResourceLocation layoutId = json.has("id") ? new ResourceLocation(json.get("id").getAsString()) : fallbackId;
        int slotCount = readInt(json, "slotCount", 27);
        int slotsPerRow = readInt(json, "slotsPerRow", Math.max(1, Math.min(9, slotCount)));
        int panelWidth = readInt(json, "panelWidth", 176);
        int panelHeight = readInt(json, "panelHeight", 168);
        int titleX = readInt(json, "titleX", 8);
        int titleY = readInt(json, "titleY", 6);
        int firstSlotX = readInt(json, "firstSlotX", 8);
        int firstSlotY = readInt(json, "firstSlotY", 18);
        int slotSpacingX = readInt(json, "slotSpacingX", 18);
        int slotSpacingY = readInt(json, "slotSpacingY", 18);
        LayoutMigrationStrategy migrationStrategy = LayoutMigrationStrategy.byName(
                json.has("migrationStrategy") ? json.get("migrationStrategy").getAsString() : null);
        Set<Integer> forbiddenSlots = new LinkedHashSet<>(readForbiddenSlots(sourceId, json.get("forbiddenSlots")));
        Map<Integer, SlotRule> slotRules = readSlotRules(sourceId, json.get("slotRules"), forbiddenSlots);
        try {
            manifest().registerLayout(new ChestLayoutDef(layoutId, slotCount, slotsPerRow,
                    panelWidth, panelHeight, titleX, titleY, firstSlotX, firstSlotY, slotSpacingX, slotSpacingY,
                    migrationStrategy, slotRules));
        } catch (IllegalArgumentException e) {
            ChestCavityLegacy.LOGGER.warn("Skipping chest layout {} from {} because it is invalid.", layoutId, sourceId, e);
        }
    }

    private static int readInt(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    private static void loadType(String typeId, ResourceLocation id, JsonObject json) {
        BodyTypeDef type = new BodyTypeDef(typeId);
        List<Integer> forbiddenSlots = readForbiddenSlots(id, json.get("forbiddenSlots"));
        type.setForbiddenSlots(forbiddenSlots);
        if (json.has("layoutId")) {
            type.setLayoutId(new ResourceLocation(json.get("layoutId").getAsString()));
        } else if (json.has("layout")) {
            type.setLayoutId(new ResourceLocation(json.get("layout").getAsString()));
        }
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
        manifest().registerBodyType(type);
    }

    private static void loadEntityAssignment(ResourceLocation id, JsonObject json) {
        if (!json.has("chestcavity") || !json.has("entities")) {
            ChestCavityLegacy.LOGGER.warn("Skipping entity assignment {} because chestcavity or entities is missing.", id);
            return;
        }

        String typeId = json.get("chestcavity").getAsString();
        JsonElement entitiesElement = json.get("entities");
        if (!entitiesElement.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping entity assignment {} because entities is not an array.", id);
            return;
        }

        for (JsonElement entityElement : entitiesElement.getAsJsonArray()) {
            try {
                ResourceLocation entityId = new ResourceLocation(entityElement.getAsString());
                if (isEntityPresent(entityId)) {
                    manifest().registerEntityAssignment(entityId, typeId);
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

    private static List<ExceptionalOrganDef> readExceptionalOrgans(ResourceLocation id, JsonElement element) {
        List<ExceptionalOrganDef> organs = new ArrayList<>();
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
                ResourceLocation itemId = null;
                String oreName = null;
                if (ingredient.has("item")) {
                    itemId = new ResourceLocation(ingredient.get("item").getAsString());
                    if (ForgeRegistries.ITEMS.getValue(itemId) == null) {
                        ChestCavityLegacy.LOGGER.info("Skipping exceptional organ entry {} in {} because item {} is not registered in 1.12.2.", index, id, itemId);
                        continue;
                    }
                } else if (ingredient.has("ore")) {
                    oreName = ingredient.get("ore").getAsString();
                } else if (ingredient.has("tag")) {
                    oreName = mapTagToOreName(ingredient.get("tag").getAsString());
                }
                if (itemId == null && (oreName == null || oreName.isEmpty())) {
                    ChestCavityLegacy.LOGGER.warn("Skipping exceptional organ entry {} in {} because ingredient has no supported item, ore, or tag.", index, id);
                    continue;
                }
                organs.add(new ExceptionalOrganDef(itemId, oreName, readOrganScores(id, object.get("value"))));
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

    private static Map<Integer, SlotRule> readSlotRules(ResourceLocation id, JsonElement element, Set<Integer> forbiddenSlots) {
        Map<Integer, SlotRule> rules = new LinkedHashMap<>();
        if (forbiddenSlots != null) {
            for (Integer slot : forbiddenSlots) {
                if (slot != null) {
                    rules.put(slot, SlotRule.FORBIDDEN);
                }
            }
        }
        if (element == null) {
            return rules;
        }
        if (!element.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping slotRules in {} because it is not an array.", id);
            return rules;
        }

        for (JsonElement entry : element.getAsJsonArray()) {
            try {
                if (!entry.isJsonObject()) {
                    continue;
                }
                JsonObject object = entry.getAsJsonObject();
                if (!object.has("slot")) {
                    ChestCavityLegacy.LOGGER.warn("Skipping slotRule in {} because slot is missing.", id);
                    continue;
                }
                int slot = object.get("slot").getAsInt();
                boolean forbidden = object.has("forbidden") && object.get("forbidden").getAsBoolean();
                Set<ResourceLocation> allowedItems = readResourceLocations(id, object.get("allowedItems"));
                Set<String> allowedScores = readStrings(id, object.get("allowedScores"));
                int minStackSize = readInt(object, "minStackSize", 0);
                int maxStackSize = readInt(object, "maxStackSize", 64);
                rules.put(slot, new SlotRule(forbidden, allowedItems, allowedScores, minStackSize, maxStackSize));
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to parse slotRule in {}.", id, e);
            }
        }
        return rules;
    }

    private static Set<ResourceLocation> readResourceLocations(ResourceLocation id, JsonElement element) {
        Set<ResourceLocation> values = new LinkedHashSet<>();
        if (element == null) {
            return values;
        }
        if (!element.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping resource location list in {} because it is not an array.", id);
            return values;
        }
        for (JsonElement entry : element.getAsJsonArray()) {
            try {
                values.add(new ResourceLocation(entry.getAsString()));
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to parse resource location in {}.", id, e);
            }
        }
        return values;
    }

    private static Set<String> readStrings(ResourceLocation id, JsonElement element) {
        Set<String> values = new LinkedHashSet<>();
        if (element == null) {
            return values;
        }
        if (!element.isJsonArray()) {
            ChestCavityLegacy.LOGGER.warn("Skipping string list in {} because it is not an array.", id);
            return values;
        }
        for (JsonElement entry : element.getAsJsonArray()) {
            try {
                values.add(entry.getAsString());
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Unable to parse string in {}.", id, e);
            }
        }
        return values;
    }

    public static boolean isEntityPresent(ResourceLocation entityId) {
        return PLAYER_ENTITY_ID.equals(entityId) || ForgeRegistries.ENTITIES.getValue(entityId) != null;
    }
}
