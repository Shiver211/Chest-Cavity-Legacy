package com.shiver.chestcavity.capability;

import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.registry.CCOrganScores;
import com.shiver.chestcavity.registry.CCPotions;
import com.shiver.chestcavity.runtime.ChestCavityRuntime;
import com.shiver.chestcavity.util.ChestCavityTypeUtil;
import com.shiver.chestcavity.util.OrganCompatibilityUtil;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public final class ChestCavityMutations {

    public enum SyncMode {
        NONE,
        OWNER
    }

    private ChestCavityMutations() {
    }

    public static void setOrgan(ChestCavityData chestCavity, int slot, ItemStack stack) {
        apply(chestCavity, SyncMode.OWNER, mutation -> mutation.setOrgan(slot, stack));
    }

    public static void recalculate(ChestCavityData chestCavity) {
        apply(chestCavity, SyncMode.OWNER, Mutation::touchRuntime);
    }

    public static void initialize(ChestCavityData chestCavity, UUID compatibilityId) {
        apply(chestCavity, SyncMode.NONE, mutation -> mutation.setCompatibilityId(compatibilityId));
    }

    public static void open(ChestCavityData chestCavity) {
        apply(chestCavity, SyncMode.OWNER, mutation -> {
            if (chestCavity.isOpened()) {
                return;
            }
            ChestCavityType type = ChestCavityTypeUtil.getAssignedChestCavityType(chestCavity);
            if (type == null) {
                return;
            }
            chestCavity.ensureOrganStorage();
            for (int i = 0; i < chestCavity.getSlotCount() && i < type.getDefaultChestCavity().size(); i++) {
                ItemStack stack = type.getDefaultChestCavity().getStack(i);
                mutation.setOrgan(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }
            chestCavity.setOpenedRaw(true);
            chestCavity.clearProjectileQueue();
            chestCavity.disconnectCrystal();
            mutation.assignOrganCompatibility();
        });
    }

    public static void resetCompatibility(ChestCavityData chestCavity, UUID compatibilityId) {
        apply(chestCavity, SyncMode.NONE, mutation -> {
            chestCavity.setCompatibilityIdRaw(compatibilityId);
            chestCavity.invalidateOrganInstancesRaw();
            mutation.touchRuntime();
        });
    }

    public static void destroyOrgansWithScore(ChestCavityData chestCavity, String scoreId) {
        if (scoreId == null) {
            return;
        }
        apply(chestCavity, SyncMode.OWNER, mutation -> {
            ChestCavityType type = ChestCavityTypeUtil.getChestCavityType(chestCavity);
            for (int slot = 0; slot < chestCavity.getSlotCount(); slot++) {
                ItemStack stack = chestCavity.getOrgan(slot);
                OrganData data = chestCavity.resolveOrganData(type, stack);
                if (data != null && data.getOrganScores().containsKey(scoreId)) {
                    mutation.setOrgan(slot, ItemStack.EMPTY);
                }
            }
        });
    }

    public static void apply(ChestCavityData chestCavity, SyncMode syncMode, MutationConsumer consumer) {
        if (chestCavity == null || consumer == null) {
            return;
        }
        Mutation mutation = new Mutation(chestCavity);
        consumer.accept(mutation);
        mutation.commit(syncMode == null ? SyncMode.OWNER : syncMode);
    }

    public interface MutationConsumer {
        void accept(Mutation mutation);
    }

    public static final class Mutation {
        private final ChestCavityData chestCavity;
        private boolean runtimeTouched;

        private Mutation(ChestCavityData chestCavity) {
            this.chestCavity = chestCavity;
        }

        public void setOrgan(int slot, ItemStack stack) {
            ItemStack oldStack = chestCavity.getOrgan(slot);
            ItemStack oldCopy = oldStack.isEmpty() ? ItemStack.EMPTY : oldStack.copy();
            ItemStack newCopy = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
            chestCavity.setOrganInternal(slot, newCopy);
            chestCavity.publishOrganChange(slot, oldCopy, newCopy);
            runtimeTouched = true;
        }

        public void setCompatibilityId(UUID compatibilityId) {
            chestCavity.setCompatibilityIdRaw(compatibilityId);
            chestCavity.invalidateOrganInstancesRaw();
            runtimeTouched = true;
        }

        public void assignOrganCompatibility() {
            OrganCompatibilityUtil.assignOrganCompatibility(chestCavity);
            chestCavity.invalidateOrganInstancesRaw();
            runtimeTouched = true;
        }

        public void setOrganSilently(int slot, ItemStack stack) {
            chestCavity.setOrganInternal(slot, stack);
            runtimeTouched = true;
        }

        public void touchRuntime() {
            runtimeTouched = true;
        }

        private void commit(SyncMode syncMode) {
            if (!runtimeTouched) {
                return;
            }
            ChestCavityRuntime oldRuntime = chestCavity.peekRuntime();
            ChestCavityRuntime newRuntime = ChestCavityRuntime.rebuild(chestCavity, ChestCavityTypeUtil.getChestCavityType(chestCavity));
            chestCavity.setRuntimeCommitted(newRuntime);
            applyScoreSideEffects(chestCavity, oldRuntime);
            if (syncMode == SyncMode.OWNER) {
                chestCavity.syncOwner();
            }
        }

        private void applyScoreSideEffects(ChestCavityData chestCavity, ChestCavityRuntime oldRuntime) {
            if (oldRuntime == null) {
                return;
            }
            if (chestCavity.getOwner() != null
                    && !chestCavity.getOwner().world.isRemote
                    && oldRuntime.getScoreValue(CCOrganScores.INCOMPATIBILITY) != chestCavity.getOrganScore(CCOrganScores.INCOMPATIBILITY)) {
                chestCavity.getOwner().removePotionEffect(CCPotions.ORGAN_REJECTION);
            }
            if (chestCavity.getOrganScore(CCOrganScores.FILTRATION) >= oldRuntime.getScoreValue(CCOrganScores.FILTRATION)) {
                chestCavity.setBloodPoisonTimer(0);
            }
            if (chestCavity.getOrganScore(CCOrganScores.HEALTH) > 0.0F) {
                chestCavity.setHeartBleedTimer(0);
            }
        }
    }
}
