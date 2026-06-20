package com.shiver.chestcavity.scoreevent;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.capability.ChestCavityMutations;
import com.shiver.chestcavity.chest.types.ChestCavityType;
import com.shiver.chestcavity.config.CCConfig;
import com.shiver.chestcavity.registry.CCEnchantments;
import com.shiver.chestcavity.util.ChestCavityTypeUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class OrganPersistenceScoreEvents {

    private OrganPersistenceScoreEvents() {
    }

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        ChestCavityHelper.copy(event.getOriginal(), event.getEntityPlayer());
        if (event.isWasDeath()) {
            resetPlayerChestCavityAfterDeath(
                    ChestCavityHelper.getOrNull(event.getOriginal()),
                    ChestCavityHelper.getOrNull(event.getEntityPlayer()));
        }
        if (event.getEntityPlayer() instanceof EntityPlayerMP) {
            ChestCavityNetwork.sendChestCavitySyncTo(event.getEntityPlayer(), (EntityPlayerMP) event.getEntityPlayer());
            ChestCavityNetwork.sendOrganDataSync((EntityPlayerMP) event.getEntityPlayer());
        }
    }

    private static void resetPlayerChestCavityAfterDeath(ChestCavityData oldCavity, ChestCavityData newCavity) {
        if (oldCavity == null || newCavity == null) {
            return;
        }
        if (CCConfig.KEEP_CHEST_CAVITY) {
            ChestCavityMutations.recalculate(newCavity);
            return;
        }

        Map<Integer, ItemStack> preserved = new LinkedHashMap<>();
        if (oldCavity.isOpened()) {
            for (int slot = 0; slot < oldCavity.getSlotCount(); slot++) {
                ItemStack stack = oldCavity.getOrgan(slot);
                if (!stack.isEmpty() && EnchantmentHelper.getEnchantmentLevel(CCEnchantments.O_NEGATIVE, stack) >= 2) {
                    preserved.put(slot, stack.copy());
                }
            }
        }

        ChestCavityMutations.apply(newCavity, ChestCavityMutations.SyncMode.OWNER, mutation -> {
            mutation.setCompatibilityId(UUID.randomUUID());
            if (newCavity.isOpened()) {
                ChestCavityType type = ChestCavityTypeUtil.getChestCavityType(newCavity);
                for (int slot = 0; slot < newCavity.getSlotCount(); slot++) {
                    ItemStack stack = slot < type.getDefaultChestCavity().size()
                            ? type.getDefaultChestCavity().getStack(slot)
                            : ItemStack.EMPTY;
                    mutation.setOrgan(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                }
                mutation.assignOrganCompatibility();
                for (Map.Entry<Integer, ItemStack> entry : preserved.entrySet()) {
                    if (entry.getKey() >= 0 && entry.getKey() < newCavity.getSlotCount()) {
                        mutation.setOrgan(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                mutation.touchRuntime();
            }
        });
    }
}
