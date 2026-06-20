package com.shiver.chestcavity.event;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.api.ChestCavityApis;
import com.shiver.chestcavity.capability.ChestCavityHelper;
import com.shiver.chestcavity.capability.ChestCavityData;
import com.shiver.chestcavity.chest.organs.OrganData;
import com.shiver.chestcavity.entity.EntityForcefulSpit;
import com.shiver.chestcavity.registry.CCEnchantments;
import com.shiver.chestcavity.registry.CCItems;
import com.shiver.chestcavity.util.OrganCompatibilityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLlamaSpit;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ForgeClientEvents {

    private ForgeClientEvents() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Item item : CCItems.getItems()) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
        RenderingRegistry.registerEntityRenderingHandler(EntityForcefulSpit.class, RenderLlamaSpit::new);
    }

    @SubscribeEvent
    public static void itemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        addSpecialItemTooltip(event, stack);

        OrganData organData = OrganData.get(stack);
        if (organData == null) {
            organData = OrganData.fromStack(stack);
        }
        if (organData == null || organData.getOrganScoresView().isEmpty()) {
            return;
        }

        event.getToolTip().add(TextFormatting.DARK_GREEN + "Organ scores:");
        for (Map.Entry<String, Float> entry : organData.getOrganScoresView().entrySet()) {
            event.getToolTip().add(TextFormatting.GRAY + "  " + getScoreName(entry.getKey(), entry.getValue()) + ": " + formatScore(entry.getValue()));
        }
        addCompatibilityTooltip(event, stack);
    }

    private static void addSpecialItemTooltip(ItemTooltipEvent event, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return;
        }
        ResourceLocation id = stack.getItem().getRegistryName();
        if ("chestcavity".equals(id.getNamespace()) && "creeper_appendix".equals(id.getPath())) {
            event.getToolTip().add(TextFormatting.ITALIC + "This appears to be a fuse.");
            event.getToolTip().add(TextFormatting.ITALIC + "It won't do much by itself.");
        }
    }

    private static String getScoreName(String id, float value) {
        String displayName = ChestCavityApis.SCORES.getDisplayName(id);
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        String key = "organscore.chestcavity." + id;
        if (I18n.hasKey(key)) {
            String format = value > 0.0F ? "+" : "";
            return I18n.format(key, format);
        }
        return id;
    }

    private static String formatScore(float value) {
        return value > 0.0F ? "+" + value : Float.toString(value);
    }

    private static void addCompatibilityTooltip(ItemTooltipEvent event, ItemStack stack) {
        ChestCavityData playerCavity = Minecraft.getMinecraft().player == null
                ? null
                : ChestCavityHelper.getOrNull(Minecraft.getMinecraft().player);
        int compatibility = playerCavity == null ? -1 : OrganCompatibilityUtil.getCompatibilityLevel(playerCavity, stack);

        TextFormatting color = compatibility > 0 ? TextFormatting.GREEN : compatibility == 0 ? TextFormatting.RED : TextFormatting.YELLOW;
        if (EnchantmentHelper.getEnchantmentLevel(CCEnchantments.MALPRACTICE, stack) > 0) {
            event.getToolTip().add(color + "Unsafe to use");
        } else if (OrganCompatibilityUtil.hasCompatibilityTag(stack)
                && EnchantmentHelper.getEnchantmentLevel(CCEnchantments.O_NEGATIVE, stack) <= 0) {
            event.getToolTip().add(color + "Only compatible with: " + OrganCompatibilityUtil.getCompatibilityName(stack));
        } else {
            event.getToolTip().add(color + "Safe to use");
        }
    }
}
