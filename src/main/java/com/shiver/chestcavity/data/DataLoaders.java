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

/**
 * 负责从资源包、配置目录和运行时覆盖中加载胸腔相关数据。
 */
public final class DataLoaders {

    public static final String DATA_ROOT = "chestcavity_data";
    public static final String ASSET_DATA_PATH = "assets/" + Tags.MOD_ID + "/" + DATA_ROOT;
    public static final String CONFIG_DATA_PATH = "config/" + Tags.MOD_ID + "/data";
    public static final String FALLBACK_ID = "fallback";

    private static final ChestCavityType FALLBACK_TYPE = new FallbackChestCavityType();
    private static final Map<String, ChestCavityType> CHEST_CAVITY_TYPES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, String> ENTITY_ASSIGNMENTS = new LinkedHashMap<>();
    private static final List<Runnable> RUNTIME_OVERRIDES = new ArrayList<>();
    private static boolean replayingRuntimeOverrides;
    private static final ResourceLocation PLAYER_ENTITY_ID = new ResourceLocation("minecraft", "player");
    private static int dataVersion;

    /**
     * 工具类，不允许外部实例化。
     */
    private DataLoaders() {
    }

    /**
     * 重新加载全部胸腔数据，包括器官、类型和实体分配。
     *
     * @param gameDir 游戏根目录。
     */
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
        replayRuntimeOverrides();
        dataVersion++;

        ChestCavityLegacy.LOGGER.info(
                "Loaded chest cavity data. assetPath={}, configPath={}, organs={}, types={}, entityAssignments={}",
                ASSET_DATA_PATH,
                configDataDir == null ? CONFIG_DATA_PATH : configDataDir.getPath(),
                OrganData.getRegistry().size(),
                Math.max(0, CHEST_CAVITY_TYPES.size() - 1),
                ENTITY_ASSIGNMENTS.size());
    }

    /**
     * 应用一条运行时覆盖逻辑，并在重载后自动重放。
     *
     * @param override 运行时覆盖逻辑。
     */
    public static void applyRuntimeOverride(Runnable override) {
        if (override == null) {
            return;
        }
        if (!replayingRuntimeOverrides) {
            RUNTIME_OVERRIDES.add(override);
        }
        override.run();
        if (!replayingRuntimeOverrides) {
            dataVersion++;
        }
    }

    /**
     * 在基础数据加载完成后重放全部运行时覆盖。
     */
    private static void replayRuntimeOverrides() {
        if (RUNTIME_OVERRIDES.isEmpty()) {
            return;
        }

        replayingRuntimeOverrides = true;
        try {
            for (Runnable override : RUNTIME_OVERRIDES) {
                override.run();
            }
        } finally {
            replayingRuntimeOverrides = false;
        }
    }

    /**
     * 注册一个胸腔类型。
     *
     * @param id 类型标识。
     * @param type 胸腔类型对象。
     */
    public static void registerType(String id, ChestCavityType type) {
        if (id != null && type != null) {
            CHEST_CAVITY_TYPES.put(id, type);
            dataVersion++;
        }
    }

    /**
     * 注销一个胸腔类型。
     *
     * @param id 类型标识。
     */
    public static void unregisterType(String id) {
        if (id != null && !FALLBACK_ID.equals(id)) {
            CHEST_CAVITY_TYPES.remove(id);
            dataVersion++;
        }
    }

    /**
     * 注册一个实体到胸腔类型的分配关系。
     *
     * @param entityId 实体注册名。
     * @param typeId 胸腔类型标识。
     */
    public static void registerEntityAssignment(ResourceLocation entityId, String typeId) {
        if (entityId != null && typeId != null) {
            ENTITY_ASSIGNMENTS.put(entityId, typeId);
            dataVersion++;
        }
    }

    /**
     * 注销一个实体到胸腔类型的分配关系。
     *
     * @param entityId 实体注册名。
     */
    public static void unregisterEntityAssignment(ResourceLocation entityId) {
        if (entityId != null) {
            ENTITY_ASSIGNMENTS.remove(entityId);
            dataVersion++;
        }
    }

    /**
     * 返回当前数据版本号。
     *
     * @return 数据版本号。
     */
    public static int getDataVersion() {
        return dataVersion;
    }

    /**
     * 按类型标识返回胸腔类型；找不到时回退到保底类型。
     *
     * @param id 类型标识。
     * @return 对应的胸腔类型。
     */
    public static ChestCavityType getType(String id) {
        ChestCavityType type = CHEST_CAVITY_TYPES.get(id);
        return type == null ? FALLBACK_TYPE : type;
    }

    /**
     * 返回保底胸腔类型。
     *
     * @return 保底胸腔类型。
     */
    public static ChestCavityType getFallbackType() {
        return FALLBACK_TYPE;
    }

    /**
     * 返回指定实体被分配到的胸腔类型标识。
     *
     * @param entityId 实体注册名。
     * @return 胸腔类型标识。
     */
    public static String getAssignedTypeId(ResourceLocation entityId) {
        return ENTITY_ASSIGNMENTS.get(entityId);
    }

    /**
     * 返回全部胸腔类型的只读视图。
     *
     * @return 胸腔类型映射。
     */
    public static Map<String, ChestCavityType> getTypes() {
        return Collections.unmodifiableMap(CHEST_CAVITY_TYPES);
    }

    /**
     * 返回全部实体分配关系的只读视图。
     *
     * @return 实体分配映射。
     */
    public static Map<ResourceLocation, String> getEntityAssignments() {
        return Collections.unmodifiableMap(ENTITY_ASSIGNMENTS);
    }

    /**
     * 收集所有可能存在资源数据的目录位置。
     *
     * @param gameDir 游戏根目录。
     * @return 可能的数据目录列表。
     */
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

    /**
     * 从 classpath 中补充加载打包后的资源数据。
     *
     * @param scannedDirectories 已扫描目录集合，用于去重。
     */
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

    /**
     * 从 jar 内部读取打包后的胸腔数据资源。
     *
     * @param resource 指向数据根目录的 jar 资源 URL。
     */
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

    /**
     * 扫描一个目录并加载其中的全部 JSON 数据。
     *
     * @param root 要扫描的目录。
     * @param sourceName 日志中使用的数据来源名称。
     * @param scannedDirectories 已扫描目录集合，用于去重。
     */
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

    /**
     * 加载单个 JSON 文件。
     *
     * @param rootPath 根目录路径。
     * @param jsonPath JSON 文件路径。
     */
    private static void loadJsonFile(Path rootPath, Path jsonPath) {
        String relativePath = rootPath.relativize(jsonPath).toString().replace(File.separatorChar, '/');
        try (Reader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(jsonPath), StandardCharsets.UTF_8))) {
            loadJson(relativePath, reader);
        } catch (Exception e) {
            ResourceLocation id = new ResourceLocation(Tags.MOD_ID, relativePath);
            ChestCavityLegacy.LOGGER.warn("Unable to load chest cavity data {}", id, e);
        }
    }

    /**
     * 根据相对路径分发一份 JSON 数据到对应加载器。
     *
     * @param relativePath 相对数据路径。
     * @param reader JSON 读取器。
     */
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
            loadType(typeIdFromPath(relativePath), id, json);
        } else if (relativePath.startsWith("entity_assignment/")) {
            loadEntityAssignment(id, json);
        }
    }

    /**
     * 从类型数据路径中提取胸腔类型标识。
     *
     * @param relativePath 相对数据路径。
     * @return 类型标识。
     */
    private static String typeIdFromPath(String relativePath) {
        String fileName = relativePath.substring(relativePath.lastIndexOf('/') + 1);
        return fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
    }

    /**
     * 从 JSON 中读取一个胸腔类型定义。
     *
     * @param typeId 类型标识。
     * @param id 数据文件资源标识。
     * @param json 解析后的 JSON 对象。
     */
    private static void loadType(String typeId, ResourceLocation id, JsonObject json) {
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
        registerType(typeId, type);
    }

    /**
     * 从 JSON 中读取实体到胸腔类型的分配定义。
     *
     * @param id 数据文件资源标识。
     * @param json 解析后的 JSON 对象。
     */
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
                    registerEntityAssignment(entityId, typeId);
                } else {
                    ChestCavityLegacy.LOGGER.info("Skipping entity assignment {} -> {} because the entity is not registered in 1.12.2.", entityId, typeId);
                }
            } catch (Exception e) {
                ChestCavityLegacy.LOGGER.warn("Skipping invalid entity entry in {}.", id, e);
            }
        }
    }

    /**
     * 从 JSON 中读取默认胸腔布局。
     *
     * @param id 数据文件资源标识。
     * @param element 默认布局对应的 JSON 元素。
     * @param forbiddenSlots 禁用槽位列表。
     * @return 解析出的默认胸腔布局。
     */
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

    /**
     * 从 JSON 数组中读取器官分数字典。
     *
     * @param id 数据文件资源标识。
     * @param element 记录器官分数的 JSON 元素。
     * @return 器官分数字典。
     */
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

    /**
     * 从 JSON 中读取特殊器官匹配规则列表。
     *
     * @param id 数据文件资源标识。
     * @param element 记录特殊器官的 JSON 元素。
     * @return 特殊器官规则列表。
     */
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

    /**
     * 把部分标签名称映射成 1.12.2 可用的矿辞名称。
     *
     * @param tag 标签名称。
     * @return 对应的矿辞名称；如果不支持则返回 `null`。
     */
    private static String mapTagToOreName(String tag) {
        if ("minecraft:logs".equals(tag)) {
            return "logWood";
        }
        if ("minecraft:leaves".equals(tag)) {
            return "treeLeaves";
        }
        return null;
    }

    /**
     * 从 JSON 中读取禁用槽位列表。
     *
     * @param id 数据文件资源标识。
     * @param element 记录禁用槽位的 JSON 元素。
     * @return 禁用槽位列表。
     */
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

    /**
     * 判断一个实体在当前 1.12.2 环境中是否已注册。
     *
     * @param entityId 实体注册名。
     * @return `true` 表示该实体存在。
     */
    public static boolean isEntityPresent(ResourceLocation entityId) {
        return PLAYER_ENTITY_ID.equals(entityId) || ForgeRegistries.ENTITIES.getValue(entityId) != null;
    }
}
