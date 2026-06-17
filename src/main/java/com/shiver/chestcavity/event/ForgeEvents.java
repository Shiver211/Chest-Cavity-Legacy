package com.shiver.chestcavity.event;

import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityProvider;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.item.ChestOpener;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.potion.FurnacePower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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

import java.util.List;

@Mod.EventBusSubscriber(modid = "chestcavity")
public final class ForgeEvents {

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

    private static void handleInteract(PlayerInteractEvent event, Entity target) {
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
}
