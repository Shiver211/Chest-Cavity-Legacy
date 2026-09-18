package com.shiver.chestcavity.event;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;
import java.util.List;

/**
 * 负责处理胸腔器官掉落、自定义 API 掉落以及战利品表注入。
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ChestCavityDropEvents {

    private static final LootCondition[] NO_CONDITIONS = new LootCondition[0];
    private static final LootFunction[] NO_FUNCTIONS = new LootFunction[0];

    private ChestCavityDropEvents() {
    }

    /**
     * 接管实体死亡掉落，生成胸腔器官与 API 自定义掉落。
     *
     * @param event 生物掉落事件。
     */
    @SubscribeEvent
    public static void livingDrops(LivingDropsEvent event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity == null) {
            return;
        }

        addApiDrops(event);

        if (chestCavity.isOpened()) {
            removeTakenWitherStar(event, chestCavity);
            if (event.getEntityLiving() instanceof EntityPlayer) {
                boolean keepInventory = event.getEntityLiving().world != null
                        && event.getEntityLiving().world.getGameRules().getBoolean("keepInventory");
                if (CCConfig.KEEP_CHEST_CAVITY || keepInventory) {
                    return;
                }
            }
            for (ItemStack stack : ChestCavityHelper.removeUnboundOrgansForDeath(chestCavity)) {
                event.getDrops().add(new EntityItem(event.getEntityLiving().world,
                        event.getEntityLiving().posX,
                        event.getEntityLiving().posY,
                        event.getEntityLiving().posZ,
                        stack));
            }
            return;
        }

        Entity trueSource = event.getSource() == null ? null : event.getSource().getTrueSource();
        EntityLivingBase killer = trueSource instanceof EntityLivingBase ? (EntityLivingBase) trueSource : null;
        List<ItemStack> generatedLoot = ChestCavityHelper.generateUnopenedOrganDrops(
                chestCavity,
                event.getEntityLiving().world.rand,
                event.getLootingLevel(),
                killer);

        for (ItemStack stack : generatedLoot) {
            event.getDrops().add(new EntityItem(event.getEntityLiving().world,
                    event.getEntityLiving().posX,
                    event.getEntityLiving().posY,
                    event.getEntityLiving().posZ,
                    stack));
        }
    }

    /**
     * 向沙漠神殿战利品表注入额外器官掉落。
     *
     * @param event 战利品表加载事件。
     */
    @SubscribeEvent
    public static void lootTableLoad(LootTableLoadEvent event) {
        if (!LootTableList.CHESTS_DESERT_PYRAMID.equals(event.getName())) {
            return;
        }
        addDesertPyramidPool(event, "rotten_rib", CCItems.ROTTEN_RIB, 4, 0.25F,
                new SetCount(NO_CONDITIONS, new RandomValueRange(1, 4)));
        addDesertPyramidPool(event, "rotten_spine", CCItems.ROTTEN_SPINE, 1, 0.3F);
    }

    /**
     * 把通过运行时 API 注册的额外器官掉落加入事件结果中。
     *
     * @param event 生物掉落事件。
     */
    static void addApiDrops(LivingDropsEvent event) {
        ResourceLocation entityId = EntityList.getKey(event.getEntityLiving());
        if (entityId == null) {
            return;
        }
        for (ItemStack stack : ChestCavityApis.DROPS.generateDrops(entityId, event.getEntityLiving(), event.getEntityLiving().world.rand)) {
            event.getDrops().add(new EntityItem(event.getEntityLiving().world,
                    event.getEntityLiving().posX,
                    event.getEntityLiving().posY,
                    event.getEntityLiving().posZ,
                    stack));
        }
    }

    /**
     * 当凋灵体内已经取走下界之星时，移除其原版掉落。
     *
     * @param event 生物掉落事件。
     * @param chestCavity 凋灵胸腔数据。
     */
    static void removeTakenWitherStar(LivingDropsEvent event, IChestCavity chestCavity) {
        if (!(event.getEntityLiving() instanceof EntityWither) || containsOrgan(chestCavity, Items.NETHER_STAR)) {
            return;
        }
        for (Iterator<EntityItem> iterator = event.getDrops().iterator(); iterator.hasNext();) {
            EntityItem drop = iterator.next();
            ItemStack stack = drop.getItem();
            if (!stack.isEmpty() && stack.getItem() == Items.NETHER_STAR) {
                iterator.remove();
            }
        }
    }

    /**
     * 判断胸腔中是否仍然包含指定物品。
     *
     * @param chestCavity 要检查的胸腔数据。
     * @param item 目标物品。
     * @return `true` 表示胸腔中仍有该物品。
     */
    static boolean containsOrgan(IChestCavity chestCavity, net.minecraft.item.Item item) {
        for (ItemStack stack : chestCavity.getOrgans()) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    /**
     * 向沙漠神殿掉落表追加一个带随机概率的独立掉落池。
     *
     * @param event 战利品表加载事件。
     * @param name 掉落池名称后缀。
     * @param item 要掉落的物品。
     * @param attempts 添加次数。
     * @param chance 每次添加的触发概率。
     * @param functions 可选的掉落函数。
     */
    static void addDesertPyramidPool(LootTableLoadEvent event, String name, net.minecraft.item.Item item,
                                     int attempts, float chance, LootFunction... functions) {
        for (int i = 0; i < attempts; i++) {
            LootEntry entry = new LootEntryItem(
                    item,
                    1,
                    0,
                    functions == null ? NO_FUNCTIONS : functions,
                    NO_CONDITIONS,
                    Tags.MOD_ID + "_desert_pyramid_" + name + "_" + i);
            LootPool pool = new LootPool(
                    new LootEntry[] {entry},
                    new LootCondition[] {new RandomChance(chance)},
                    new RandomValueRange(1),
                    new RandomValueRange(0),
                    Tags.MOD_ID + "_desert_pyramid_" + name + "_" + i);
            event.getTable().addPool(pool);
        }
    }
}

