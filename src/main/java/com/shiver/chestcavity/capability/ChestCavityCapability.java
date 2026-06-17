package com.shiver.chestcavity.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public final class ChestCavityCapability {

    @CapabilityInject(IChestCavity.class)
    public static Capability<IChestCavity> CAPABILITY = null;

    private static boolean registered;

    private ChestCavityCapability() {
    }

    public static synchronized boolean ensureRegistered() {
        if (!registered) {
            CapabilityManager.INSTANCE.register(IChestCavity.class, new ChestCavityStorage(), ChestCavityData::new);
            registered = true;
        }
        return true;
    }
}
