package com.shiver.chestcavity.event;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityProvider;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.mixin.EntityCreeperAccessor;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.potion.FurnacePower;
import com.shiver.chestcavity.registry.CCOrganScores;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 负责处理胸腔能力附加、实体每 tick 更新、跳跃、药水结算与玩家克隆等生命周期事件。
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ChestCavityLifecycleEvents {

    private ChestCavityLifecycleEvents() {
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
     * 在药水生效前按胸腔属性调整其参数。
     *
     * @param event 药水可应用事件。
     */
    @SubscribeEvent
    public static void potionApplicable(PotionEvent.PotionApplicableEvent event) {
        ChestCavityHelper.adjustIncomingPotionEffect(event.getEntityLiving(), event.getPotionEffect());
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
     * 防止已打开但没有 Creepy 分数的苦力怕继续进入自爆状态。
     *
     * @param entity 要处理的实体。
     * @param chestCavity 实体胸腔数据。
     */
    static void stopOpenedCreeperWithoutCreepy(EntityLivingBase entity, IChestCavity chestCavity) {
        if (!(entity instanceof EntityCreeper) || !chestCavity.isOpened()
                || chestCavity.getOrganScore(CCOrganScores.CREEPY) > 0.0F) {
            return;
        }
        EntityCreeper creeper = (EntityCreeper) entity;
        creeper.setCreeperState(-1);
        if (creeper instanceof EntityCreeperAccessor) {
            ((EntityCreeperAccessor) creeper).chestcavity$setTimeSinceIgnited(1);
        }
    }
}

