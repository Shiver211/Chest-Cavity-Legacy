package com.shiver.chestcavity.event;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityProvider;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.item.ChestOpener;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.potion.FurnacePower;
import com.shiver.chestcavity.registry.CCItems;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.Tags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumActionResult;
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
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

/**
 * 集中处理胸腔系统依赖的 Forge 运行时事件。
 */
@Mod.EventBusSubscriber(modid = "chestcavity")
public final class ForgeEvents {

    private static final LootCondition[] NO_CONDITIONS = new LootCondition[0];
    private static final LootFunction[] NO_FUNCTIONS = new LootFunction[0];
    private static final Field CREEPER_IGNITION_TIME_FIELD = findCreeperIgnitionTimeField();

    /**
     * 工具类，不允许外部实例化。
     */
    private ForgeEvents() {
    }

    /**
     * 为所有活体实体挂接胸腔能力。
     *
     * @param event 能力挂接事件。
     */
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase) {
            event.addCapability(ChestCavityHelper.CAPABILITY_ID, new ChestCavityProvider((EntityLivingBase) event.getObject()));
        }
    }

    /**
     * 在实体每 tick 更新时推进胸腔逻辑与炉火层数。
     *
     * @param event 活体更新事件。
     */
    @SubscribeEvent
    public static void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        FurnacePower.tickFuelLayers(event.getEntityLiving());
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            ChestCavityHelper.tick(event.getEntityLiving(), chestCavity);
            stopOpenedCreeperWithoutCreepy(event.getEntityLiving(), chestCavity);
        }
    }

    /**
     * 在攻击判定前尝试处理投射物闪避。
     *
     * @param event 攻击事件。
     */
    @SubscribeEvent
    public static void livingAttack(LivingAttackEvent event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null && ChestCavityHelper.attemptProjectileDodge(event.getEntityLiving(), chestCavity, event.getSource())) {
            event.setCanceled(true);
        }
    }

    /**
     * 在伤害进入前按器官防御属性修正伤害值。
     *
     * @param event 受伤事件。
     */
    @SubscribeEvent
    public static void livingHurt(LivingHurtEvent event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            event.setAmount(ChestCavityHelper.applyDefense(chestCavity, event.getSource(), event.getAmount()));
        }
    }

    /**
     * 在最终伤害结算阶段处理附加效果与碰撞破坏。
     *
     * @param event 实际受伤事件。
     */
    @SubscribeEvent
    public static void livingDamage(LivingDamageEvent event) {
        float amount = ChestCavityHelper.applyFinalDamageEffects(event.getEntityLiving(), event.getSource(), event.getAmount());
        event.setAmount(amount);
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            ChestCavityHelper.applyDestructiveCollisions(event.getEntityLiving(), chestCavity, event.getSource(), amount);
        }
    }

    /**
     * 在实体跳跃时应用胸腔提供的跳跃修正。
     *
     * @param event 跳跃事件。
     */
    @SubscribeEvent
    public static void livingJump(LivingEvent.LivingJumpEvent event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            ChestCavityHelper.applyJump(event.getEntityLiving(), chestCavity);
        }
    }

    /**
     * 在玩家完成进食后应用器官附带的食物效果。
     *
     * @param event 物品使用完成事件。
     */
    @SubscribeEvent
    public static void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            ChestCavityHelper.applyFoodEffects((EntityPlayer) event.getEntityLiving(), event.getItem());
        }
    }

    /**
     * 在药水生效前按胸腔属性调整其参数。
     *
     * @param event 药水可应用事件。
     */
    @SubscribeEvent
    public static void potionApplicable(PotionEvent.PotionApplicableEvent event) {
        ChestCavityHelper.adjustIncomingPotionEffect(event.getEntityLiving(), event.getPotionEffect());
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
     * 把通过运行时 API 注册的额外器官掉落加入事件结果中。
     *
     * @param event 生物掉落事件。
     */
    private static void addApiDrops(LivingDropsEvent event) {
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
     * 处理普通实体交互，支持丝腺交互和胸腔开启器。
     *
     * @param event 实体交互事件。
     */
    @SubscribeEvent
    public static void entityInteract(PlayerInteractEvent.EntityInteract event) {
        handleSilkInteract(event, event.getTarget());
        handleInteract(event, event.getTarget());
    }

    /**
     * 处理精确实体交互，支持胸腔开启器。
     *
     * @param event 精确实体交互事件。
     */
    @SubscribeEvent
    public static void entityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handleInteract(event, event.getTarget());
    }

    /**
     * 在玩家克隆时复制胸腔数据并重新同步客户端。
     *
     * @param event 玩家克隆事件。
     */
    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        ChestCavityHelper.copy(event.getOriginal(), event.getEntityPlayer(), event.isWasDeath());
        if (event.getEntityPlayer() instanceof EntityPlayerMP) {
            ChestCavityNetwork.sendChestCavitySyncTo(event.getEntityPlayer(), (EntityPlayerMP) event.getEntityPlayer());
            ChestCavityNetwork.sendOrganDataSync((EntityPlayerMP) event.getEntityPlayer());
        }
    }

    /**
     * 玩家登录后立即向客户端下发胸腔与器官注册表数据。
     *
     * @param event 玩家登录事件。
     */
    @SubscribeEvent
    public static void playerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            ChestCavityNetwork.sendChestCavitySyncTo(player, player);
            ChestCavityNetwork.sendOrganDataSync(player);
        }
    }

    /**
     * 当玩家开始追踪实体时，同步目标实体的胸腔数据。
     *
     * @param event 开始追踪事件。
     */
    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP && event.getTarget() instanceof EntityLivingBase) {
            ChestCavityNetwork.sendChestCavitySyncTo((EntityLivingBase) event.getTarget(), (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    /**
     * 按器官分数修正玩家挖掘速度。
     *
     * @param event 挖掘速度事件。
     */
    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityPlayer());
        if (chestCavity != null) {
            event.setNewSpeed(event.getNewSpeed() * ChestCavityHelper.getMiningSpeedMultiplier(chestCavity));
        }
    }

    /**
     * 处理纯净水喷溅带来的胸腔交互效果。
     *
     * @param event 投掷物命中事件。
     */
    @SubscribeEvent
    public static void projectileImpact(ProjectileImpactEvent.Throwable event) {
        if (!(event.getThrowable() instanceof EntityPotion)) {
            return;
        }
        EntityPotion potion = (EntityPotion) event.getThrowable();
        ItemStack stack = potion.getPotion();
        if (PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER && PotionUtils.getEffectsFromStack(stack).isEmpty()) {
            ChestCavityHelper.applyWaterSplash(potion);
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
     * 处理胸腔开启器对目标实体的交互逻辑。
     *
     * @param event 玩家交互事件。
     * @param target 交互目标。
     */
    private static void handleInteract(PlayerInteractEvent event, Entity target) {
        if (target instanceof MultiPartEntityPart && ((MultiPartEntityPart) target).parent instanceof EntityDragon) {
            target = (EntityDragon) ((MultiPartEntityPart) target).parent;
        }
        if (event.isCanceled() || !(target instanceof EntityLivingBase)) {
            return;
        }

        ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
        if (held.isEmpty() || !(held.getItem() instanceof ChestOpener)) {
            return;
        }

        if (event.getEntityPlayer().getCooldownTracker().hasCooldown(held.getItem())) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
            return;
        }

        ChestOpener opener = (ChestOpener) held.getItem();
        if (opener.openChestCavity(event.getEntityPlayer(), (EntityLivingBase) target)) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }

    /**
     * 处理用桶或剪刀从特定生物身上获取丝的交互。
     *
     * @param event 实体交互事件。
     * @param target 交互目标。
     */
    private static void handleSilkInteract(PlayerInteractEvent.EntityInteract event, Entity target) {
        if (event.isCanceled() || event.getWorld().isRemote || !(target instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase living = (EntityLivingBase) target;
        ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
        if (held.isEmpty()) {
            return;
        }

        if ((living instanceof EntityCow || living instanceof EntityMooshroom)
                && held.getItem() == Items.BUCKET
                && !living.isChild()
                && !event.getEntityPlayer().capabilities.isCreativeMode) {
            ChestCavityHelper.milkSilk(living);
            return;
        }

        if ((living instanceof EntitySheep || living instanceof EntityMooshroom)
                && held.getItem() == Items.SHEARS
                && !living.isChild()
                && (!(living instanceof EntitySheep) || !((EntitySheep) living).getSheared())) {
            ChestCavityHelper.shearSilk(living);
        }
    }

    /**
     * 防止已打开但没有 Creepy 分数的苦力怕继续进入自爆状态。
     *
     * @param entity 要处理的实体。
     * @param chestCavity 实体胸腔数据。
     */
    private static void stopOpenedCreeperWithoutCreepy(EntityLivingBase entity, IChestCavity chestCavity) {
        if (!(entity instanceof EntityCreeper) || !chestCavity.isOpened()
                || chestCavity.getOrganScore(CCOrganScores.CREEPY) > 0.0F) {
            return;
        }
        EntityCreeper creeper = (EntityCreeper) entity;
        creeper.setCreeperState(-1);
        if (CREEPER_IGNITION_TIME_FIELD != null) {
            try {
                CREEPER_IGNITION_TIME_FIELD.setInt(creeper, 1);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    /**
     * 当凋灵体内已经取走下界之星时，移除其原版掉落。
     *
     * @param event 生物掉落事件。
     * @param chestCavity 凋灵胸腔数据。
     */
    private static void removeTakenWitherStar(LivingDropsEvent event, IChestCavity chestCavity) {
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
    private static boolean containsOrgan(IChestCavity chestCavity, net.minecraft.item.Item item) {
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
    private static void addDesertPyramidPool(LootTableLoadEvent event, String name, net.minecraft.item.Item item,
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

    /**
     * 通过反射找到苦力怕内部的点火计时字段。
     *
     * @return 点火计时字段；找不到时返回 `null`。
     */
    private static Field findCreeperIgnitionTimeField() {
        try {
            Field field = ReflectionHelper.findField(EntityCreeper.class, "timeSinceIgnited", "field_70833_d");
            field.setAccessible(true);
            return field;
        } catch (ReflectionHelper.UnableToFindFieldException ignored) {
            return null;
        }
    }
}
