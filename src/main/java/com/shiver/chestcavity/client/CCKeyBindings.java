package com.shiver.chestcavity.client;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.client.abilitywheel.CCAbilityWheel;
import com.shiver.chestcavity.network.ChestCavityNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

/**
 * 注册并处理客户端的器官能力快捷键。
 */
@SideOnly(Side.CLIENT)
public final class CCKeyBindings {

    private static final String CATEGORY = "category." + Tags.MOD_ID + ".organ_abilities";
    private static final ResourceLocation ABILITY_WHEEL_ID = id("ability_wheel");
    private static final ResourceLocation RELEASE_ABILITY_ID = id("release_ability");

    public static KeyBinding abilityWheel;
    public static KeyBinding releaseAbility;

    private static boolean registered;

    /**
     * 工具类，不允许外部实例化。
     */
    private CCKeyBindings() {
    }

    /**
     * 注册能力轮盘与释放能力快捷键，并挂接输入事件监听。
     */
    public static void register() {
        if (registered) {
            return;
        }

        abilityWheel = register(ABILITY_WHEEL_ID, Keyboard.KEY_R);
        releaseAbility = register(RELEASE_ABILITY_ID, Keyboard.KEY_X);

        FMLCommonHandler.instance().bus().register(new CCKeyBindings());
        MinecraftForge.EVENT_BUS.register(new CCAbilityWheel());
        registered = true;
    }

    /**
     * 监听按键输入，并在按下释放键时向服务端发送能力激活请求。
     *
     * @param event Forge 键盘输入事件。
     */
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().player == null) {
            return;
        }

        while (releaseAbility.isPressed()) {
            String abilityId = CCAbilityWheel.getSelectedAbility();
            if (abilityId != null) {
                ChestCavityNetwork.sendHotkeyActivation(abilityId);
            }
        }
    }

    /**
     * 创建并注册一个按键绑定。
     *
     * @param id 按键标识。
     * @param defaultKey 默认键位。
     * @return 注册后的按键对象。
     */
    private static KeyBinding register(ResourceLocation id, int defaultKey) {
        KeyBinding keyBinding = new KeyBinding("key." + id.getNamespace() + "." + id.getPath(), defaultKey, CATEGORY);
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    /**
     * 根据路径创建本模组命名空间下的资源标识。
     *
     * @param path 资源路径。
     * @return 对应的资源标识。
     */
    private static ResourceLocation id(String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }
}
