package com.shiver.chestcavity.integration.compat;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.config.CCConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class CrossModCompat {

    private static final LootCondition[] NO_CONDITIONS = new LootCondition[0];
    private static final LootFunction[] NO_FUNCTIONS = new LootFunction[0];

    private static final String BACKROOMS_MODID = "backrooms";
    private static final String REQUIEM_MODID = "requiem";
    private static final String BACKROOMS_NAME = "Backrooms";
    private static final String REQUIEM_NAME = "Requiem";
    private static final ResourceLocation REQUIEM_PLAYER_SHELL = new ResourceLocation(REQUIEM_MODID, "player_shell");

    private static boolean backroomsActive;
    private static boolean requiemActive;
    private static boolean eventHandlersRegistered;

    private CrossModCompat() {
    }

    public static void init() {
        backroomsActive = checkIntegration(BACKROOMS_MODID, BACKROOMS_NAME, CCConfig.BACKROOMS_INTEGRATION);
        requiemActive = checkIntegration(REQUIEM_MODID, REQUIEM_NAME, CCConfig.REQUIEM_INTEGRATION);
        registerEvents();
    }

    public static void registerEvents() {
        if (eventHandlersRegistered || !backroomsActive) {
            return;
        }
        MinecraftForge.EVENT_BUS.register(new BackroomsLootHandler());
        eventHandlersRegistered = true;
    }

    public static boolean isRequiemPlayerShell(EntityLivingBase entity) {
        if (!requiemActive || entity == null) {
            return false;
        }
        ResourceLocation entityId = EntityList.getKey(entity);
        return REQUIEM_PLAYER_SHELL.equals(entityId);
    }

    private static boolean checkIntegration(String modid, String name, boolean enabled) {
        if (!Loader.isModLoaded(modid)) {
            return false;
        }
        ChestCavityLegacy.LOGGER.info("{} detected.", name);
        if (!enabled) {
            ChestCavityLegacy.LOGGER.info("{} integration disabled.", name);
            return false;
        }
        ChestCavityLegacy.LOGGER.info("Integrating with {}.", name);
        return true;
    }

    private static final class BackroomsLootHandler {

        private static final ResourceLocation LEVEL0_CHEST = new ResourceLocation(BACKROOMS_MODID, "chests/level0");
        private static final ResourceLocation LEVEL1_CHEST = new ResourceLocation(BACKROOMS_MODID, "chests/level1");
        private static final ResourceLocation LEVEL3_CHEST = new ResourceLocation(BACKROOMS_MODID, "chests/level3");

        @SubscribeEvent
        public void lootTableLoad(LootTableLoadEvent event) {
            ResourceLocation table = event.getName();
            if (LEVEL0_CHEST.equals(table)) {
                addBackroomsPool(event, "level0", level0Entries());
            } else if (LEVEL1_CHEST.equals(table)) {
                addBackroomsPool(event, "level1", level1Entries());
            } else if (LEVEL3_CHEST.equals(table)) {
                addBackroomsPool(event, "level3", level3Entries());
            }
        }

        private static void addBackroomsPool(LootTableLoadEvent event, String level, List<LootEntry> entries) {
            if (entries.isEmpty()) {
                return;
            }
            LootEntry[] poolEntries = entries.toArray(new LootEntry[entries.size()]);
            int attempts = Math.max(0, CCConfig.BACKROOMS_CHEST_ORGAN_LOOT_ATTEMPTS);
            float chance = Math.max(0.0F, Math.min(1.0F, CCConfig.BACKROOMS_CHEST_ORGAN_LOOT_CHANCE));
            for (int i = 0; i < attempts; i++) {
                LootCondition[] conditions = new LootCondition[] {new RandomChance(chance)};
                LootPool pool = new LootPool(
                        poolEntries,
                        conditions,
                        new RandomValueRange(1),
                        new RandomValueRange(0),
                        Tags.MOD_ID + "_backrooms_" + level + "_organs_" + i);
                event.getTable().addPool(pool);
            }
        }

        private static List<LootEntry> level0Entries() {
            List<LootEntry> entries = new ArrayList<LootEntry>();
            add(entries, "rotten_appendix", 1);
            add(entries, "rotten_heart", 1);
            add(entries, "rotten_intestine", 1);
            add(entries, "rotten_kidney", 1);
            add(entries, "rotten_liver", 1);
            add(entries, "rotten_lung", 1);
            add(entries, "rotten_rib", 1, count(1, 4));
            add(entries, "rotten_spine", 1);
            add(entries, "rotten_spleen", 1);
            add(entries, "rotten_stomach", 1);
            return entries;
        }

        private static List<LootEntry> level1Entries() {
            List<LootEntry> entries = new ArrayList<LootEntry>();
            add(entries, "rotten_appendix", 2);
            add(entries, "rotten_heart", 2);
            add(entries, "rotten_intestine", 2);
            add(entries, "rotten_kidney", 2);
            add(entries, "rotten_liver", 2);
            add(entries, "rotten_lung", 2);
            add(entries, "rotten_rib", 2, count(1, 4));
            add(entries, "rotten_spine", 2);
            add(entries, "rotten_spleen", 2);
            add(entries, "rotten_stomach", 2);
            add(entries, "small_animal_appendix", 1);
            add(entries, "small_animal_heart", 1);
            add(entries, "small_animal_intestine", 1);
            add(entries, "small_animal_kidney", 1);
            add(entries, "small_animal_liver", 1);
            add(entries, "small_animal_lung", 1);
            add(entries, "small_gills", 1);
            add(entries, "small_animal_rib", 1, count(1, 4));
            add(entries, "small_animal_spine", 1);
            add(entries, "small_animal_spleen", 1);
            add(entries, "small_animal_stomach", 1);
            add(entries, "small_animal_muscle", 1, count(1, 4));
            return entries;
        }

        private static List<LootEntry> level3Entries() {
            List<LootEntry> entries = new ArrayList<LootEntry>();
            add(entries, "muscle", 8, count(1, 16));
            add(entries, "appendix", 4);
            add(entries, "kidney", 4);
            add(entries, "liver", 4);
            add(entries, "rib", 4, count(1, 4));
            add(entries, "spleen", 4);
            add(entries, "lung", 2);
            add(entries, "heart", 1);
            add(entries, "intestine", 1);
            add(entries, "spine", 1);
            add(entries, "stomach", 1);
            return entries;
        }

        private static void add(List<LootEntry> entries, String itemName, int weight, LootFunction... functions) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Tags.MOD_ID, itemName));
            if (item == null) {
                ChestCavityLegacy.LOGGER.warn("Skipping Backrooms loot entry for missing item {}:{}.", Tags.MOD_ID, itemName);
                return;
            }
            entries.add(new LootEntryItem(
                    item,
                    weight,
                    0,
                    functions == null ? NO_FUNCTIONS : functions,
                    NO_CONDITIONS,
                    Tags.MOD_ID + "_" + itemName));
        }

        private static LootFunction count(int min, int max) {
            return new SetCount(NO_CONDITIONS, new RandomValueRange(min, max));
        }
    }
}
