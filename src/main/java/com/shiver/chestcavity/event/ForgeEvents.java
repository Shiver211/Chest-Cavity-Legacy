package com.shiver.chestcavity.event;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityProvider;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.item.ChestOpener;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.potion.FurnacePower;
import com.shiver.chestcavity.registry.CCItems;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.Tags;
import net.minecraft.entity.Entity;
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

@Mod.EventBusSubscriber(modid = "chestcavity")
public final class ForgeEvents {

    private static final LootCondition[] NO_CONDITIONS = new LootCondition[0];
    private static final LootFunction[] NO_FUNCTIONS = new LootFunction[0];
    private static final Field CREEPER_IGNITION_TIME_FIELD = findCreeperIgnitionTimeField();

    private ForgeEvents() {
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase) {
            event.addCapability(ChestCavityHelper.CAPABILITY_ID, new ChestCavityProvider((EntityLivingBase) event.getObject()));
        }
    }

    @SubscribeEvent
    public static void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        FurnacePower.tickFuelLayers(event.getEntityLiving());
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            ChestCavityHelper.tick(event.getEntityLiving(), chestCavity);
            stopOpenedCreeperWithoutCreepy(event.getEntityLiving(), chestCavity);
        }
    }

    @SubscribeEvent
    public static void livingAttack(LivingAttackEvent event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null && ChestCavityHelper.attemptProjectileDodge(event.getEntityLiving(), chestCavity, event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void livingHurt(LivingHurtEvent event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            event.setAmount(ChestCavityHelper.applyDefense(chestCavity, event.getSource(), event.getAmount()));
        }
    }

    @SubscribeEvent
    public static void livingDamage(LivingDamageEvent event) {
        float amount = ChestCavityHelper.applyFinalDamageEffects(event.getEntityLiving(), event.getSource(), event.getAmount());
        event.setAmount(amount);
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            ChestCavityHelper.applyDestructiveCollisions(event.getEntityLiving(), chestCavity, event.getSource(), amount);
        }
    }

    @SubscribeEvent
    public static void livingJump(LivingEvent.LivingJumpEvent event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            ChestCavityHelper.applyJump(event.getEntityLiving(), chestCavity);
        }
    }

    @SubscribeEvent
    public static void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            ChestCavityHelper.applyFoodEffects((EntityPlayer) event.getEntityLiving(), event.getItem());
        }
    }

    @SubscribeEvent
    public static void potionApplicable(PotionEvent.PotionApplicableEvent event) {
        ChestCavityHelper.adjustIncomingPotionEffect(event.getEntityLiving(), event.getPotionEffect());
    }

    @SubscribeEvent
    public static void livingDrops(LivingDropsEvent event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity == null) {
            return;
        }

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

    @SubscribeEvent
    public static void entityInteract(PlayerInteractEvent.EntityInteract event) {
        handleSilkInteract(event, event.getTarget());
        handleInteract(event, event.getTarget());
    }

    @SubscribeEvent
    public static void entityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handleInteract(event, event.getTarget());
    }

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        ChestCavityHelper.copy(event.getOriginal(), event.getEntityPlayer(), event.isWasDeath());
        if (event.getEntityPlayer() instanceof EntityPlayerMP) {
            ChestCavityNetwork.sendChestCavitySyncTo(event.getEntityPlayer(), (EntityPlayerMP) event.getEntityPlayer());
            ChestCavityNetwork.sendOrganDataSync((EntityPlayerMP) event.getEntityPlayer());
        }
    }

    @SubscribeEvent
    public static void playerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            ChestCavityNetwork.sendChestCavitySyncTo(player, player);
            ChestCavityNetwork.sendOrganDataSync(player);
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP && event.getTarget() instanceof EntityLivingBase) {
            ChestCavityNetwork.sendChestCavitySyncTo((EntityLivingBase) event.getTarget(), (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        IChestCavity chestCavity = ChestCavityHelper.getOrNull(event.getEntityPlayer());
        if (chestCavity != null) {
            event.setNewSpeed(event.getNewSpeed() * ChestCavityHelper.getMiningSpeedMultiplier(chestCavity));
        }
    }

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

    @SubscribeEvent
    public static void lootTableLoad(LootTableLoadEvent event) {
        if (!LootTableList.CHESTS_DESERT_PYRAMID.equals(event.getName())) {
            return;
        }
        addDesertPyramidPool(event, "rotten_rib", CCItems.ROTTEN_RIB, 4, 0.25F,
                new SetCount(NO_CONDITIONS, new RandomValueRange(1, 4)));
        addDesertPyramidPool(event, "rotten_spine", CCItems.ROTTEN_SPINE, 1, 0.3F);
    }

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

    private static boolean containsOrgan(IChestCavity chestCavity, net.minecraft.item.Item item) {
        for (ItemStack stack : chestCavity.getOrgans()) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

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
