package com.shiver.chestcavity.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public final class ChestCavityCapability {

    @CapabilityInject(ChestCavityData.class)
    public static Capability<ChestCavityData> CAPABILITY = null;

    private static boolean registered;

    private ChestCavityCapability() {
    }

    public static synchronized boolean ensureRegistered() {
        if (!registered) {
            CapabilityManager.INSTANCE.register(ChestCavityData.class, new ChestCavityStorage(), ChestCavityData::new);
            registered = true;
        }
        return true;
    }
}
