package com.shiver.chestcavity.registry;

import com.shiver.chestcavity.ability.ActiveOrganAbilities;
import com.shiver.chestcavity.ability.ActiveOrganAbility;
import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.score.Score;

public final class CCScores {

    private static int nextDisplayOrder;

    private CCScores() {
    }

    public static void registerDefaults() {
        register(
                score(CCOrganScores.HEALTH),
                score(CCOrganScores.LUCK),
                score(CCOrganScores.STRENGTH),
                score(CCOrganScores.SPEED),
                score(CCOrganScores.DEFENSE),
                score(CCOrganScores.NERVES),
                score(CCOrganScores.MINING_SPEED),
                score(CCOrganScores.EASE_OF_ACCESS),
                negative(CCOrganScores.INCOMPATIBILITY),
                score(CCOrganScores.DIGESTION),
                score(CCOrganScores.NUTRITION),
                score(CCOrganScores.FILTRATION),
                score(CCOrganScores.DETOXIFICATION),
                negative(CCOrganScores.METABOLISM),
                score(CCOrganScores.ENDURANCE),
                score(CCOrganScores.BREATH),
                score(CCOrganScores.BREATH_CAPACITY),
                score(CCOrganScores.WATER_BREATH),
                negative(CCOrganScores.EXPLOSIVE),
                score(CCOrganScores.FIRE_RESISTANT),
                negative(CCOrganScores.HYDROALLERGENIC),
                negative(CCOrganScores.HYDROPHOBIA),
                score(CCOrganScores.IMPACT_RESISTANT),
                score(CCOrganScores.KNOCKBACK_RESISTANT),
                score(CCOrganScores.LEAPING),
                score(CCOrganScores.LIGHTWEIGHT),
                score(CCOrganScores.SWIM_SPEED),
                score(CCOrganScores.GLOWING),
                score(CCOrganScores.LAUNCHING),
                score(CCOrganScores.VENOMOUS),
                score(CCOrganScores.ARROW_DODGING),
                score(CCOrganScores.BUFF_PURGING),
                negative(CCOrganScores.WITHERED),
                score(CCOrganScores.CRYSTALSYNTHESIS),
                score(CCOrganScores.PHOTOSYNTHESIS),
                score(CCOrganScores.DESTRUCTIVE_COLLISIONS)
        );

        active(CCOrganScores.FURNACE_POWERED, ActiveOrganAbilities::activateFurnacePowered);
        active(CCOrganScores.GRAZING, ActiveOrganAbilities::activateGrazing);
        active(CCOrganScores.IRON_REPAIR, ActiveOrganAbilities::activateIronRepair);
        active(CCOrganScores.BUOYANT, ActiveOrganAbilities::activateBuoyant);
        active(CCOrganScores.CREEPY, ActiveOrganAbilities::activateCreepy);
        active(CCOrganScores.PYROMANCY, ActiveOrganAbilities::activatePyromancy);
        active(CCOrganScores.DRAGON_BOMBS, ActiveOrganAbilities::activateDragonBombs);
        active(CCOrganScores.FORCEFUL_SPIT, ActiveOrganAbilities::activateForcefulSpit);
        active(CCOrganScores.GHASTLY, ActiveOrganAbilities::activateGhastly);
        active(CCOrganScores.SHULKER_BULLETS, ActiveOrganAbilities::activateShulkerBullets);
        active(CCOrganScores.SILK, ActiveOrganAbilities::activateSilk);
    }

    private static Score score(String scoreId) {
        return new Score(scoreId).setDisplayOrder(nextDisplayOrder++);
    }

    private static Score negative(String scoreId) {
        return score(scoreId).setNegative(true);
    }

    private static void active(String scoreId, ActiveOrganAbility ability) {
        score(scoreId).register(ChestCavityApis.SCORES);
        ChestCavityApis.ABILITIES.registerWheelEntry(scoreId, null, 0, true);
        ChestCavityApis.ABILITIES.registerActiveAbility(scoreId, ability);
    }

    private static void register(Score... scores) {
        for (Score score : scores) {
            score.register(ChestCavityApis.SCORES);
        }
    }
}
