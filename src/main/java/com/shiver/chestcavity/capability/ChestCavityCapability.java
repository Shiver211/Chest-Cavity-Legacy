package com.shiver.chestcavity.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * 负责注册并暴露胸腔能力类型。
 */
public final class ChestCavityCapability {

    @CapabilityInject(IChestCavity.class)
    public static Capability<IChestCavity> CAPABILITY = null;

    private static boolean registered;

    /**
     * 工具类，不允许外部实例化。
     */
    private ChestCavityCapability() {
    }

    /**
     * 确保胸腔能力已经向 Forge 完成注册。
     *
     * @return 始终返回 `true`，便于直接放入初始化流程中调用。
     */
    public static synchronized boolean ensureRegistered() {
        if (!registered) {
            CapabilityManager.INSTANCE.register(IChestCavity.class, new ChestCavityStorage(), ChestCavityData::new);
            registered = true;
        }
        return true;
    }
}
