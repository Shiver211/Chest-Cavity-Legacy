package com.shiver.chestcavity.event;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.item.ChestOpener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 负责处理胸腔系统相关的交互事件（实体交互、使用物品、挖掘速度、投掷物命中等）。
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ChestCavityInteractionEvents {

    private ChestCavityInteractionEvents() {
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
     * 玩家进食完毕后应用消化逻辑。
     *
     * @param event 物品使用完成事件。
     */
    @SubscribeEvent
    public static void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            ChestCavityHelper.finishEatingFood((EntityPlayer) event.getEntityLiving(), event.getItem());
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
     * 处理胸腔开启器对目标实体的交互逻辑。
     *
     * @param event 玩家交互事件。
     * @param target 交互目标。
     */
    static void handleInteract(PlayerInteractEvent event, Entity target) {
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
    static void handleSilkInteract(PlayerInteractEvent.EntityInteract event, Entity target) {
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
            if (ChestCavityHelper.milkSilk(living)) {
                event.getEntityPlayer().swingArm(event.getHand());
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
            return;
        }

        if ((living instanceof EntitySheep || living instanceof EntityMooshroom)
                && held.getItem() == Items.SHEARS
                && !living.isChild()
                && (!(living instanceof EntitySheep) || !((EntitySheep) living).getSheared())) {
            if (ChestCavityHelper.shearSilk(living)) {
                if (living instanceof EntitySheep) {
                    ((EntitySheep) living).setSheared(true);
                }
                if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                    held.damageItem(1, event.getEntityPlayer());
                }
                living.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
                event.getEntityPlayer().swingArm(event.getHand());
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
    }
}

