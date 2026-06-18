package com.shiver.chestcavity.network;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.integration.crafttweaker.callback.AbilityCallbacks;
import com.shiver.chestcavity.integration.crafttweaker.context.SkillActivateContext;
import com.shiver.chestcavity.integration.crafttweaker.runtime.AbilityDefinition;
import com.shiver.chestcavity.integration.crafttweaker.runtime.AbilityRegistry;
import com.shiver.chestcavity.integration.crafttweaker.runtime.RuntimeStateRegistry;
import com.shiver.chestcavity.chest.organs.OrganManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientNetworkHooks {

    private ClientNetworkHooks() {
    }

    public static void handleChestCavitySync(MessageChestCavitySync message) {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.addScheduledTask(() -> {
            if (minecraft.world == null) {
                return;
            }

            Entity entity = minecraft.world.getEntityByID(message.getEntityId());
            IChestCavity chestCavity = ChestCavityHelper.getOrNull(entity);
            if (chestCavity != null) {
                chestCavity.deserializeNBT(message.getData());
            }
        });
    }

    public static void handleOrganDataSync(MessageOrganDataSync message) {
        Minecraft minecraft = Minecraft.getMinecraft();
        NBTTagCompound organData = message.getOrganData().copy();
        minecraft.addScheduledTask(() -> OrganManager.readRegistryFromNbt(organData));
    }

    public static void handleScriptAbilityClientActivation(MessageScriptAbilityClientActivation message) {
        Minecraft minecraft = Minecraft.getMinecraft();
        ResourceLocation abilityId = message.getAbilityId();
        minecraft.addScheduledTask(() -> {
            if (!(minecraft.player instanceof EntityPlayerSP) || abilityId == null) {
                return;
            }
            EntityPlayerSP player = (EntityPlayerSP) minecraft.player;
            IChestCavity chestCavity = ChestCavityHelper.getOrNull(player);
            AbilityDefinition definition = AbilityRegistry.get(abilityId);
            if (chestCavity == null || definition == null) {
                return;
            }
            if (!(definition.getActivateClientContextCallback() instanceof AbilityCallbacks.OnActivateClientContext)) {
                return;
            }
            SkillActivateContext context = new SkillActivateContext(null, abilityId, chestCavity.getOrganScore(abilityId), 0.0F, RuntimeStateRegistry.getAbilityData(player, abilityId));
            ((AbilityCallbacks.OnActivateClientContext) definition.getActivateClientContextCallback()).handle(context);
        });
    }
}
