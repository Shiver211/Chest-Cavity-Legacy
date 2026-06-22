package com.shiver.chestcavity.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.item.ChestOpener;

import net.minecraft.creativetab.CreativeTabs;
import com.shiver.chestcavity.registry.CCTabs;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 集中定义并注册模组中的全部物品。
 */
public final class CCItems {

    private static final List<Item> ITEMS = new ArrayList<Item>();
    private static final Map<String, Item> ITEMS_BY_NAME = new HashMap<String, Item>();

    public static final Item CHEST_OPENER = register("chest_opener", new ChestOpener(), CCTabs.MAIN);

    private static final Item WOODEN_CLEAVER = register("wooden_cleaver", cleaver(Item.ToolMaterial.WOOD), CCTabs.MAIN);
    private static final Item STONE_CLEAVER = register("stone_cleaver", cleaver(Item.ToolMaterial.STONE), CCTabs.MAIN);
    private static final Item GOLD_CLEAVER = register("gold_cleaver", cleaver(Item.ToolMaterial.GOLD), CCTabs.MAIN);
    public static final Item IRON_CLEAVER = register("iron_cleaver", cleaver(Item.ToolMaterial.IRON), CCTabs.MAIN);
    private static final Item DIAMOND_CLEAVER = register("diamond_cleaver", cleaver(Item.ToolMaterial.DIAMOND), CCTabs.MAIN);

    public static final Item HUMAN_APPENDIX = register("appendix", humanOrgan(), CCTabs.MAIN);
    public static final Item HUMAN_HEART = register("heart", humanOrgan(), CCTabs.MAIN);
    public static final Item HUMAN_INTESTINE = register("intestine", humanOrgan(), CCTabs.MAIN);
    public static final Item HUMAN_KIDNEY = register("kidney", humanOrgan(), CCTabs.MAIN);
    public static final Item HUMAN_LIVER = register("liver", humanOrgan(), CCTabs.MAIN);
    public static final Item HUMAN_LUNG = register("lung", humanOrgan(), CCTabs.MAIN);
    public static final Item HUMAN_MUSCLE = register("muscle", food(2, 0.4F, 16), CCTabs.MAIN);
    public static final Item HUMAN_RIB = register("rib", basicItem(4), CCTabs.MAIN);
    public static final Item HUMAN_SPINE = register("spine", basicItem(1), CCTabs.MAIN);
    public static final Item HUMAN_SPLEEN = register("spleen", humanOrgan(), CCTabs.MAIN);
    public static final Item HUMAN_STOMACH = register("stomach", humanOrgan(), CCTabs.MAIN);

    public static final Item ROTTEN_APPENDIX = register("rotten_appendix", rottenOrgan(), CCTabs.MAIN);
    public static final Item ROTTEN_HEART = register("rotten_heart", rottenOrgan(), CCTabs.MAIN);
    public static final Item ROTTEN_INTESTINE = register("rotten_intestine", rottenOrgan(), CCTabs.MAIN);
    public static final Item ROTTEN_KIDNEY = register("rotten_kidney", rottenOrgan(), CCTabs.MAIN);
    public static final Item ROTTEN_LIVER = register("rotten_liver", rottenOrgan(), CCTabs.MAIN);
    public static final Item ROTTEN_LUNG = register("rotten_lung", rottenOrgan(), CCTabs.MAIN);
    public static final Item ROTTEN_MUSCLE = register("rotten_muscle", rottenFood(1, 16), CCTabs.MAIN);
    public static final Item ROTTEN_RIB = register("rotten_rib", basicItem(4), CCTabs.MAIN);
    public static final Item ROTTEN_SPINE = register("rotten_spine", basicItem(1), CCTabs.MAIN);
    public static final Item ROTTEN_SPLEEN = register("rotten_spleen", rottenOrgan(), CCTabs.MAIN);
    public static final Item ROTTEN_STOMACH = register("rotten_stomach", rottenOrgan(), CCTabs.MAIN);

    private static final Item WITHERED_RIB = register("withered_rib", basicItem(4), CCTabs.MAIN);
    private static final Item WITHERED_SPINE = register("withered_spine", basicItem(1), CCTabs.MAIN);
    private static final Item WRITHING_SOULSAND = register("writhing_soulsand", basicItem(16), CCTabs.MAIN);

    public static final Item ANIMAL_APPENDIX = register("animal_appendix", animalOrgan(), CCTabs.MAIN);
    public static final Item ANIMAL_HEART = register("animal_heart", animalOrgan(), CCTabs.MAIN);
    public static final Item ANIMAL_INTESTINE = register("animal_intestine", animalOrgan(), CCTabs.MAIN);
    public static final Item ANIMAL_KIDNEY = register("animal_kidney", animalOrgan(), CCTabs.MAIN);
    public static final Item ANIMAL_LIVER = register("animal_liver", animalOrgan(), CCTabs.MAIN);
    public static final Item ANIMAL_LUNG = register("animal_lung", animalOrgan(), CCTabs.MAIN);
    public static final Item ANIMAL_MUSCLE = register("animal_muscle", food(1, 0.4F, 16), CCTabs.MAIN);
    public static final Item ANIMAL_RIB = register("animal_rib", basicItem(4), CCTabs.MAIN);
    public static final Item ANIMAL_SPINE = register("animal_spine", basicItem(1), CCTabs.MAIN);
    public static final Item ANIMAL_SPLEEN = register("animal_spleen", animalOrgan(), CCTabs.MAIN);
    public static final Item ANIMAL_STOMACH = register("animal_stomach", animalOrgan(), CCTabs.MAIN);

    private static final Item[] ANIMAL_MUSCLES = registerFoods(1, 0.4F, 16,
            "aquatic_muscle",
            "fish_muscle",
            "brutish_muscle",
            "swift_muscle",
            "springy_muscle");
    private static final Item[] ANIMAL_ORGANS = registerFoods(2, 0.6F, 1,
            "gills",
            "llama_lung",
            "carnivore_stomach",
            "carnivore_intestine",
            "herbivore_rumen",
            "herbivore_stomach",
            "herbivore_intestine");
    private static final Item[] FIREPROOF_ORGANS = registerFoods(2, 0.6F, 1,
            "fireproof_appendix",
            "fireproof_heart",
            "fireproof_intestine",
            "fireproof_kidney",
            "fireproof_liver",
            "fireproof_lung",
            "fireproof_spleen",
            "fireproof_stomach");
    private static final Item FIREPROOF_MUSCLE = register("fireproof_muscle", food(1, 0.4F, 16), CCTabs.MAIN);
    private static final Item FIREPROOF_RIB = register("fireproof_rib", basicItem(4), CCTabs.MAIN);
    private static final Item FIREPROOF_SPINE = register("fireproof_spine", basicItem(1), CCTabs.MAIN);
    private static final Item HOLLOW_FIREPROOF_RIB = register("hollow_fireproof_rib", basicItem(4), CCTabs.MAIN);

    private static final Item[] SMALL_ANIMAL_ORGANS = registerFoods(1, 0.2F, 1,
            "small_animal_appendix",
            "small_animal_heart",
            "small_animal_intestine",
            "small_animal_kidney",
            "small_animal_liver",
            "small_animal_lung",
            "small_animal_spleen",
            "small_animal_stomach",
            "rabbit_heart",
            "small_gills",
            "small_carnivore_stomach",
            "small_carnivore_intestine",
            "small_herbivore_stomach",
            "small_herbivore_intestine");
    private static final Item[] SMALL_ANIMAL_MUSCLES = registerFoods(1, 0.2F, 16,
            "small_animal_muscle",
            "small_aquatic_muscle",
            "small_fish_muscle",
            "small_springy_muscle");
    private static final Item SMALL_ANIMAL_RIB = register("small_animal_rib", basicItem(4), CCTabs.MAIN);
    private static final Item SMALL_ANIMAL_SPINE = register("small_animal_spine", basicItem(1), CCTabs.MAIN);

    private static final Item[] INSECT_ORGANS = registerToxicFoods(2, 0.6F, 1,
            "insect_heart",
            "insect_intestine",
            "insect_lung",
            "insect_stomach",
            "insect_caeca",
            "silk_gland");
    private static final Item INSECT_MUSCLE = register("insect_muscle", toxicFood(1, 0.4F, 16), CCTabs.MAIN);
    public static final Item VENOM_GLAND = register("venom_gland", venomGland(), CCTabs.MAIN);

    private static final Item[] ENDER_ORGANS = registerFoods(2, 0.6F, 1,
            "ender_appendix",
            "ender_heart",
            "ender_intestine",
            "ender_kidney",
            "ender_liver",
            "ender_lung",
            "ender_spleen",
            "ender_stomach");
    private static final Item ENDER_MUSCLE = register("ender_muscle", food(1, 0.4F, 16), CCTabs.MAIN);
    private static final Item ENDER_RIB = register("ender_rib", basicItem(4), CCTabs.MAIN);
    private static final Item ENDER_SPINE = register("ender_spine", basicItem(1), CCTabs.MAIN);

    private static final Item[] DRAGON_ORGANS = registerFoods(2, 0.6F, 1,
            "dragon_appendix",
            "dragon_heart",
            "dragon_kidney",
            "dragon_liver",
            "dragon_lung",
            "dragon_spleen",
            "mana_reactor");
    private static final Item DRAGON_MUSCLE = register("dragon_muscle", food(1, 0.4F, 16), CCTabs.MAIN);
    private static final Item DRAGON_RIB = register("dragon_rib", basicItem(4), CCTabs.MAIN);
    private static final Item DRAGON_SPINE = register("dragon_spine", basicItem(1), CCTabs.MAIN);

    private static final Item ACTIVE_BLAZE_ROD = register("active_blaze_rod", basicItem(3), CCTabs.MAIN);
    private static final Item BLAZE_SHELL = register("blaze_shell", basicItem(4), CCTabs.MAIN);
    private static final Item BLAZE_CORE = register("blaze_core", basicItem(1), CCTabs.MAIN);

    private static final Item GAS_BLADDER = register("gas_bladder", basicItem(1), CCTabs.MAIN);
    private static final Item VOLATILE_STOMACH = register("volatile_stomach", basicItem(1), CCTabs.MAIN);

    private static final Item GOLEM_CABLE = register("golem_cable", basicItem(1), CCTabs.MAIN);
    private static final Item GOLEM_PLATING = register("golem_plating", basicItem(4), CCTabs.MAIN);
    private static final Item GOLEM_CORE = register("golem_core", basicItem(1), CCTabs.MAIN);
    private static final Item INNER_FURNACE = register("inner_furnace", basicItem(1), CCTabs.MAIN);
    private static final Item PISTON_MUSCLE = register("piston_muscle", basicItem(16), CCTabs.MAIN);
    private static final Item IRON_SCRAP = register("iron_scrap", basicItem(64), CCTabs.MAIN);

    private static final Item CREEPER_APPENDIX = register("creeper_appendix", basicItem(1), CCTabs.MAIN);
    private static final Item SHIFTING_LEAVES = register("shifting_leaves", basicItem(16), CCTabs.MAIN);
    private static final Item SHULKER_SPLEEN = register("shulker_spleen", basicItem(1), CCTabs.MAIN);

    private static final Item SAUSAGE_SKIN = register("sausage_skin", basicItem(64), CCTabs.MAIN);
    private static final Item MINI_SAUSAGE_SKIN = register("mini_sausage_skin", basicItem(64), CCTabs.MAIN);

    public static final Item FURNACE_POWER = register("furnace_power", food(1, 0.6F, 64, false), CCTabs.MAIN);

    /**
     * 工具类，不允许外部实例化。
     */
    private CCItems() {
    }

    /**
     * 把全部物品注册到 Forge 注册表中。
     *
     * @param registry 物品注册表。
     */
    public static void register(IForgeRegistry<Item> registry) {
        registry.registerAll(ITEMS.toArray(new Item[ITEMS.size()]));
    }

    /**
     * 返回全部已注册物品的只读列表。
     *
     * @return 物品列表。
     */
    public static List<Item> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    /**
     * 把模组物品批量注册到矿辞中，供配方和掉落逻辑使用。
     */
    public static void registerOreDictionary() {
        registerOre("chestcavity:butchering_tool",
                "wooden_cleaver",
                "stone_cleaver",
                "gold_cleaver",
                "iron_cleaver",
                "diamond_cleaver");

        registerOre("chestcavity:salvageable_human_organ_meat",
                "appendix",
                "heart",
                "kidney",
                "liver",
                "lung",
                "stomach",
                "spleen");
        registerOre("chestcavity:salvageable_animal_organ_meat",
                "animal_appendix",
                "animal_heart",
                "animal_kidney",
                "animal_liver",
                "animal_lung",
                "animal_stomach",
                "animal_spleen",
                "carnivore_stomach",
                "fireproof_appendix",
                "fireproof_heart",
                "fireproof_kidney",
                "fireproof_liver",
                "fireproof_lung",
                "fireproof_stomach",
                "fireproof_spleen",
                "gas_bladder",
                "gills",
                "herbivore_stomach",
                "herbivore_rumen",
                "llama_lung",
                "shulker_spleen",
                "volatile_stomach");
        registerOre("chestcavity:salvageable_small_animal_organ_meat",
                "small_animal_appendix",
                "small_animal_heart",
                "small_animal_kidney",
                "small_animal_liver",
                "small_animal_lung",
                "small_animal_stomach",
                "small_animal_spleen",
                "rabbit_heart",
                "small_gills",
                "small_carnivore_stomach",
                "small_herbivore_stomach");
        registerOre("chestcavity:salvageable_toxic_organ_meat",
                "insect_heart",
                "insect_intestine",
                "insect_lung",
                "insect_stomach",
                "insect_caeca",
                "silk_gland",
                "venom_gland");
        registerOre("chestcavity:salvageable_alien_organ_meat",
                "ender_appendix",
                "ender_heart",
                "ender_kidney",
                "ender_liver",
                "ender_lung",
                "ender_stomach",
                "ender_spleen");
        registerOre("chestcavity:salvageable_dragon_organ_meat",
                "dragon_appendix",
                "dragon_heart",
                "dragon_kidney",
                "dragon_liver",
                "dragon_lung",
                "dragon_spleen",
                "mana_reactor");
        registerOre("chestcavity:salvageable_butchered_meat",
                "aquatic_muscle",
                "animal_muscle",
                "fireproof_muscle",
                "fish_muscle",
                "brutish_muscle",
                "swift_muscle",
                "springy_muscle");
        registerOre("chestcavity:salvageable_small_butchered_meat",
                "small_aquatic_muscle",
                "small_animal_muscle",
                "small_fish_muscle",
                "small_springy_muscle");
        registerOre("chestcavity:salvageable_toxic_meat", "insect_muscle");
        registerOre("chestcavity:salvageable_alien_meat", "ender_muscle");
        registerOre("chestcavity:salvageable_dragon_meat", "dragon_muscle");
        registerOre("chestcavity:salvageable_man_meat", "muscle");
        registerOre("chestcavity:salvageable_rotten_flesh",
                "rotten_appendix",
                "rotten_heart",
                "rotten_intestine",
                "rotten_kidney",
                "rotten_liver",
                "rotten_lung",
                "rotten_stomach",
                "rotten_spleen");
        registerOre("chestcavity:salvageable_bone_meal",
                "rib",
                "animal_rib",
                "fireproof_rib",
                "hollow_fireproof_rib",
                "small_animal_rib",
                "rotten_rib",
                "withered_rib",
                "ender_rib",
                "dragon_rib",
                "spine",
                "animal_spine",
                "fireproof_spine",
                "small_animal_spine",
                "rotten_spine",
                "withered_spine",
                "ender_spine",
                "dragon_spine");
        registerOre("chestcavity:salvageable_blaze_powder",
                "active_blaze_rod",
                "blaze_shell",
                "blaze_core");
        registerOre("chestcavity:salvageable_iron_scrap",
                "golem_core",
                "golem_cable",
                "golem_plating",
                "inner_furnace");
        registerOre("chestcavity:salvageable_gunpowder", "creeper_appendix");
        registerOre("chestcavity:salvageable_sausage_skin",
                "animal_intestine",
                "herbivore_intestine",
                "carnivore_intestine",
                "fireproof_intestine",
                "insect_intestine",
                "intestine",
                "ender_intestine");
        registerOre("chestcavity:salvageable_mini_sausage_skin",
                "small_animal_intestine",
                "small_herbivore_intestine",
                "small_carnivore_intestine");
        registerOre("chestcavity:small_animal_organs",
                "small_animal_appendix",
                "small_animal_heart",
                "small_animal_intestine",
                "small_animal_kidney",
                "small_animal_liver",
                "small_animal_lung",
                "small_animal_muscle",
                "small_animal_stomach",
                "small_animal_spleen",
                "small_animal_rib",
                "small_animal_spine",
                "rabbit_heart");

        registerSalvageableUnion();
    }

    /**
     * 注册总的“可拆解器官”矿辞集合。
     */
    private static void registerSalvageableUnion() {
        registerOre("chestcavity:salvageable",
                "appendix",
                "heart",
                "kidney",
                "liver",
                "lung",
                "stomach",
                "spleen",
                "animal_appendix",
                "animal_heart",
                "animal_kidney",
                "animal_liver",
                "animal_lung",
                "animal_stomach",
                "animal_spleen",
                "carnivore_stomach",
                "fireproof_appendix",
                "fireproof_heart",
                "fireproof_kidney",
                "fireproof_liver",
                "fireproof_lung",
                "fireproof_stomach",
                "fireproof_spleen",
                "gas_bladder",
                "gills",
                "herbivore_stomach",
                "herbivore_rumen",
                "llama_lung",
                "shulker_spleen",
                "volatile_stomach",
                "small_animal_appendix",
                "small_animal_heart",
                "small_animal_kidney",
                "small_animal_liver",
                "small_animal_lung",
                "small_animal_stomach",
                "small_animal_spleen",
                "rabbit_heart",
                "small_gills",
                "small_carnivore_stomach",
                "small_herbivore_stomach",
                "insect_heart",
                "insect_intestine",
                "insect_lung",
                "insect_stomach",
                "insect_caeca",
                "silk_gland",
                "venom_gland",
                "ender_appendix",
                "ender_heart",
                "ender_kidney",
                "ender_liver",
                "ender_lung",
                "ender_stomach",
                "ender_spleen",
                "dragon_appendix",
                "dragon_heart",
                "dragon_kidney",
                "dragon_liver",
                "dragon_lung",
                "dragon_spleen",
                "mana_reactor",
                "aquatic_muscle",
                "animal_muscle",
                "fireproof_muscle",
                "fish_muscle",
                "brutish_muscle",
                "swift_muscle",
                "springy_muscle",
                "small_aquatic_muscle",
                "small_animal_muscle",
                "small_fish_muscle",
                "small_springy_muscle",
                "insect_muscle",
                "ender_muscle",
                "dragon_muscle",
                "muscle",
                "rotten_appendix",
                "rotten_heart",
                "rotten_intestine",
                "rotten_kidney",
                "rotten_liver",
                "rotten_lung",
                "rotten_stomach",
                "rotten_spleen",
                "rib",
                "animal_rib",
                "fireproof_rib",
                "hollow_fireproof_rib",
                "small_animal_rib",
                "rotten_rib",
                "withered_rib",
                "ender_rib",
                "dragon_rib",
                "spine",
                "animal_spine",
                "fireproof_spine",
                "small_animal_spine",
                "rotten_spine",
                "withered_spine",
                "ender_spine",
                "dragon_spine",
                "active_blaze_rod",
                "blaze_shell",
                "blaze_core",
                "golem_core",
                "golem_cable",
                "golem_plating",
                "inner_furnace",
                "creeper_appendix",
                "animal_intestine",
                "herbivore_intestine",
                "carnivore_intestine",
                "fireproof_intestine",
                "intestine",
                "ender_intestine",
                "small_animal_intestine",
                "small_herbivore_intestine",
                "small_carnivore_intestine",
                "piston_muscle",
                "writhing_soulsand");
    }

    /**
     * 创建一个标准动物器官食物物品。
     *
     * @return 动物器官物品。
     */
    private static Item animalOrgan() {
        return food(2, 0.6F, 1);
    }

    /**
     * 创建一个仅设置堆叠上限的基础物品。
     *
     * @param maxStackSize 最大堆叠数量。
     * @return 基础物品。
     */
    private static Item basicItem(int maxStackSize) {
        Item item = new Item();
        item.setMaxStackSize(maxStackSize);
        return item;
    }

    /**
     * 创建一把指定材质的切割刀。
     *
     * @param material 工具材质。
     * @return 切割刀物品。
     */
    private static ItemSword cleaver(Item.ToolMaterial material) {
        return new ItemSword(material);
    }

    /**
     * 创建一个默认允许狼食用的食物物品。
     *
     * @param amount 回复饥饿值。
     * @param saturation 饱和度。
     * @param maxStackSize 最大堆叠数量。
     * @return 食物物品。
     */
    private static ItemFood food(int amount, float saturation, int maxStackSize) {
        return food(amount, saturation, maxStackSize, true);
    }

    /**
     * 创建一个可配置是否为狼食物的食物物品。
     *
     * @param amount 回复饥饿值。
     * @param saturation 饱和度。
     * @param maxStackSize 最大堆叠数量。
     * @param isWolfFood 是否允许狼食用。
     * @return 食物物品。
     */
    private static ItemFood food(int amount, float saturation, int maxStackSize, boolean isWolfFood) {
        ItemFood item = new ItemFood(amount, saturation, isWolfFood);
        item.setMaxStackSize(maxStackSize);
        return item;
    }

    /**
     * 创建一个标准人类器官食物物品。
     *
     * @return 人类器官物品。
     */
    private static Item humanOrgan() {
        return food(3, 0.6F, 1);
    }

    /**
     * 批量注册一组普通食物物品。
     *
     * @param amount 回复饥饿值。
     * @param saturation 饱和度。
     * @param maxStackSize 最大堆叠数量。
     * @param names 物品注册名列表。
     * @return 注册后的物品数组。
     */
    private static Item[] registerFoods(int amount, float saturation, int maxStackSize, String... names) {
        Item[] items = new Item[names.length];
        for (int i = 0; i < names.length; i++) {
            items[i] = register(names[i], food(amount, saturation, maxStackSize), CCTabs.MAIN);
        }
        return items;
    }

    /**
     * 批量注册一组带中毒效果的食物物品。
     *
     * @param amount 回复饥饿值。
     * @param saturation 饱和度。
     * @param maxStackSize 最大堆叠数量。
     * @param names 物品注册名列表。
     * @return 注册后的物品数组。
     */
    private static Item[] registerToxicFoods(int amount, float saturation, int maxStackSize, String... names) {
        Item[] items = new Item[names.length];
        for (int i = 0; i < names.length; i++) {
            items[i] = register(names[i], toxicFood(amount, saturation, maxStackSize), CCTabs.MAIN);
        }
        return items;
    }

    /**
     * 创建一个带饥饿副作用的腐烂食物。
     *
     * @param amount 回复饥饿值。
     * @param maxStackSize 最大堆叠数量。
     * @return 腐烂食物物品。
     */
    private static ItemFood rottenFood(int amount, int maxStackSize) {
        ItemFood item = food(amount, 0.1F, maxStackSize);
        item.setPotionEffect(new PotionEffect(MobEffects.HUNGER, 600, 0), 0.8F);
        return item;
    }

    /**
     * 创建一个标准腐烂器官物品。
     *
     * @return 腐烂器官物品。
     */
    private static Item rottenOrgan() {
        return rottenFood(4, 1);
    }

    /**
     * 创建一个必定带中毒效果的食物。
     *
     * @param amount 回复饥饿值。
     * @param saturation 饱和度。
     * @param maxStackSize 最大堆叠数量。
     * @return 有毒食物物品。
     */
    private static ItemFood toxicFood(int amount, float saturation, int maxStackSize) {
        ItemFood item = food(amount, saturation, maxStackSize);
        item.setPotionEffect(new PotionEffect(MobEffects.POISON, 80, 0), 1.0F);
        return item;
    }

    /**
     * 创建一个毒腺食物物品。
     *
     * @return 毒腺物品。
     */
    private static ItemFood venomGland() {
        ItemFood item = new ItemFood(2, 0.6F, true);
        item.setMaxStackSize(1);
        item.setPotionEffect(new PotionEffect(MobEffects.POISON, 80), 1.0F);
        return item;
    }

    /**
     * 写入物品注册信息并加入待注册列表。
     *
     * @param name 注册名。
     * @param item 物品对象。
     * @param tab 创造标签页。
     * @return 原物品对象，便于常量初始化。
     */
    private static Item register(String name, Item item, CreativeTabs tab) {
        if (ITEMS_BY_NAME.containsKey(name)) {
            throw new IllegalStateException("Duplicate item name: " + Tags.MOD_ID + ":" + name);
        }
        item.setRegistryName(Tags.MOD_ID, name);
        item.setTranslationKey(Tags.MOD_ID + "." + name);
        item.setCreativeTab(tab);
        ITEMS.add(item);
        ITEMS_BY_NAME.put(name, item);
        return item;
    }

    /**
     * 把一组已注册物品写入同一个矿辞条目。
     *
     * @param name 矿辞名称。
     * @param itemNames 物品注册名列表。
     */
    private static void registerOre(String name, String... itemNames) {
        for (String itemName : itemNames) {
            Item item = ITEMS_BY_NAME.get(itemName);
            if (item == null) {
                throw new IllegalStateException("Unknown item for ore dictionary: " + itemName);
            }
            OreDictionary.registerOre(name, item);
        }
    }
}
