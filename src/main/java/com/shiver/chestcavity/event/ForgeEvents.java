package com.shiver.chestcavity.event;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityProvider;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.item.ChestOpener;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.potion.FurnacePower;
import com.shiver.chestcavity.registry.CCItems;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.scoreevent.HydroScoreEvents;
import com.shiver.chestcavity.Tags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityPotion;
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
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
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
        ChestCavityData chestCavity = ChestCavityHelper.getOrNull(event.getEntityLiving());
        if (chestCavity != null) {
            ChestCavityHelper.tick(event.getEntityLiving(), chestCavity);
            stopOpenedCreeperWithoutCreepy(event.getEntityLiving(), chestCavity);
        }
    }

    @SubscribeEvent
    public static void entityInteract(PlayerInteractEvent.EntityInteract event) {
        handleInteract(event, event.getTarget());
    }

    @SubscribeEvent
    public static void entityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handleInteract(event, event.getTarget());
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
    public static void projectileImpact(ProjectileImpactEvent.Throwable event) {
        if (!(event.getThrowable() instanceof EntityPotion)) {
            return;
        }
        EntityPotion potion = (EntityPotion) event.getThrowable();
        ItemStack stack = potion.getPotion();
        if (PotionUtils.getPotionFromItem(stack) == PotionTypes.WATER && PotionUtils.getEffectsFromStack(stack).isEmpty()) {
            HydroScoreEvents.applyWaterSplash(potion);
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

    private static void stopOpenedCreeperWithoutCreepy(EntityLivingBase entity, ChestCavityData chestCavity) {
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
