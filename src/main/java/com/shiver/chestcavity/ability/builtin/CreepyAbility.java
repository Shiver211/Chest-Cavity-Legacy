package com.shiver.chestcavity.ability.builtin;

import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.IChestCavity;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;

final class CreepyAbility implements ActiveOrganAbility {

    static final CreepyAbility INSTANCE = new CreepyAbility();

    private CreepyAbility() {
    }

    @Override
    public boolean activate(EntityPlayerMP player, IChestCavity chestCavity) {
        float creepy = chestCavity.getOrganScore(CCOrganScores.CREEPY);
        if (creepy <= 0.0F || player.isPotionActive(CCPotions.EXPLOSION_COOLDOWN)) {
            return false;
        }

        float explosive = chestCavity.getOrganScore(CCOrganScores.EXPLOSIVE);
        if (explosive <= 0.0F) {
            return false;
        }

        float strength = MathHelper.sqrt(explosive);
        player.world.createExplosion(player, player.posX, player.posY, player.posZ, strength, false);
        ChestCavityHelper.destroyOrgansWithScore(chestCavity, CCOrganScores.EXPLOSIVE);
        if (player.isEntityAlive()) {
            player.addPotionEffect(new PotionEffect(CCPotions.EXPLOSION_COOLDOWN,
                    CCConfig.EXPLOSION_COOLDOWN, 0, false, false));
        }
        return true;
    }
}
