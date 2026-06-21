package com.shiver.chestcavity.ability;

import com.shiver.chestcavity.ChestCavityLegacy;
import com.shiver.chestcavity.ability.builtin.BuiltinOrganAbilities;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.crt.CrTChestCavityEvents;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ActiveOrganAbilities {

    private static final Map<String, ActiveOrganAbility> ABILITIES = new LinkedHashMap<String, ActiveOrganAbility>();

    static {
        BuiltinOrganAbilities.registerAll();
    }

    private ActiveOrganAbilities() {
    }

    public static void register(String id, ActiveOrganAbility ability) {
        ABILITIES.put(id, ability);
    }

    public static boolean activate(EntityPlayerMP player, IChestCavity chestCavity, String abilityId) {
        ActiveOrganAbility ability = ABILITIES.get(abilityId);
        if (ability == null) {
            ChestCavityLegacy.LOGGER.debug("Ignoring unknown active organ ability {}.", abilityId);
            return false;
        }
        if (chestCavity.getOrganScore(abilityId) <= 0.0F) {
            ChestCavityLegacy.LOGGER.debug("Ignoring inactive organ ability {} for {}.", abilityId, player.getName());
            return false;
        }
        boolean activated = ability.activate(player, chestCavity);
        if (activated) {
            CrTChestCavityEvents.publishAbilityActivated(player, abilityId, chestCavity.getOrganScore(abilityId));
        }
        return activated;
    }

    public static boolean fireQueuedProjectile(EntityLivingBase entity, String abilityId) {
        return QueuedProjectileAbilities.fire(entity, abilityId);
    }
}
