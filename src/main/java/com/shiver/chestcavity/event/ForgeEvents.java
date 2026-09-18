package com.shiver.chestcavity.event;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 集中事件入口（已弃用）。
 *
 * <p>事件监听逻辑现已拆分为独立的模块化类：
 * <ul>
 *   <li>{@link ChestCavityCombatEvents} 战斗与碰撞伤害</li>
 *   <li>{@link ChestCavityInteractionEvents} 玩家与实体交互</li>
 *   <li>{@link ChestCavityLifecycleEvents} 能力附加与生命周期 tick</li>
 *   <li>{@link ChestCavityDropEvents} 器官掉落与战利品表</li>
 *   <li>{@link ChestCavityNetworkEvents} 网络同步与追踪</li>
 * </ul>
 *
 * @deprecated 请直接使用各子模块事件类，此类仅作为向前兼容外观保留。
 */
@Deprecated
public final class ForgeEvents {

    private ForgeEvents() {
    }

    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        ChestCavityLifecycleEvents.attachCapabilities(event);
    }

    public static void livingUpdate(LivingEvent.LivingUpdateEvent event) {
        ChestCavityLifecycleEvents.livingUpdate(event);
    }

    public static void livingAttack(LivingAttackEvent event) {
        ChestCavityCombatEvents.livingAttack(event);
    }

    public static void livingDamage(LivingDamageEvent event) {
        ChestCavityCombatEvents.livingDamage(event);
    }

    public static void livingJump(LivingEvent.LivingJumpEvent event) {
        ChestCavityLifecycleEvents.livingJump(event);
    }

    public static void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
        ChestCavityInteractionEvents.finishUsingItem(event);
    }

    public static void potionApplicable(PotionEvent.PotionApplicableEvent event) {
        ChestCavityLifecycleEvents.potionApplicable(event);
    }

    public static void livingDrops(LivingDropsEvent event) {
        ChestCavityDropEvents.livingDrops(event);
    }

    public static void entityInteract(PlayerInteractEvent.EntityInteract event) {
        ChestCavityInteractionEvents.entityInteract(event);
    }

    public static void entityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        ChestCavityInteractionEvents.entityInteractSpecific(event);
    }

    public static void playerClone(PlayerEvent.Clone event) {
        ChestCavityLifecycleEvents.playerClone(event);
    }

    public static void playerLoggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        ChestCavityNetworkEvents.playerLoggedIn(event);
    }

    @SideOnly(Side.CLIENT)
    public static void clientDisconnected(net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ChestCavityNetworkEvents.clientDisconnected(event);
    }

    public static void startTracking(PlayerEvent.StartTracking event) {
        ChestCavityNetworkEvents.startTracking(event);
    }

    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        ChestCavityInteractionEvents.breakSpeed(event);
    }

    public static void projectileImpact(ProjectileImpactEvent.Throwable event) {
        ChestCavityInteractionEvents.projectileImpact(event);
    }

    public static void lootTableLoad(LootTableLoadEvent event) {
        ChestCavityDropEvents.lootTableLoad(event);
    }
}
