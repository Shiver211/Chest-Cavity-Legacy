package com.shiver.chestcavity.registry;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.entity.EntityForcefulSpit;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class RegistryHandler {

    private RegistryHandler() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        CCItems.register(event.getRegistry());
        CCItems.registerOreDictionary();
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        CCPotions.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        CCEnchantments.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        CCRecipes.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(EntityForcefulSpit.class)
                .id(new ResourceLocation(Tags.MOD_ID, "forceful_spit"), 0)
                .name("forceful_spit")
                .tracker(64, 10, true)
                .build());
    }
}
