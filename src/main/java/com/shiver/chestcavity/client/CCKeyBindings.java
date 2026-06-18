package com.shiver.chestcavity.client;

import com.shiver.chestcavity.Tags;
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

@SideOnly(Side.CLIENT)
public final class CCKeyBindings {

    private static final String CATEGORY = "category." + Tags.MOD_ID + ".organ_abilities";
    private static final ResourceLocation ABILITY_WHEEL_ID = id("ability_wheel");
    private static final ResourceLocation RELEASE_ABILITY_ID = id("release_ability");

    public static KeyBinding abilityWheel;
    public static KeyBinding releaseAbility;

    private static boolean registered;

    private CCKeyBindings() {
    }

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

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().player == null) {
            return;
        }

        while (releaseAbility.isPressed()) {
            ResourceLocation abilityId = CCAbilityWheel.getSelectedAbility();
            if (abilityId != null) {
                ChestCavityNetwork.sendHotkeyActivation(abilityId);
            }
        }
    }

    private static KeyBinding register(ResourceLocation id, int defaultKey) {
        KeyBinding keyBinding = new KeyBinding("key." + id.getNamespace() + "." + id.getPath(), defaultKey, CATEGORY);
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }
}
