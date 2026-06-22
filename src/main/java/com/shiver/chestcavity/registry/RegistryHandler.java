package com.shiver.chestcavity.registry;

import com.shiver.chestcavity.Tags;
import com.shiver.chestcavity.entity.EntityForcefulSpit;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

/**
 * 统一处理模组内容的 Forge 注册事件。
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class RegistryHandler {

    /**
     * 工具类，不允许外部实例化。
     */
    private RegistryHandler() {
    }

    /**
     * 注册全部模组物品和矿辞条目。
     *
     * @param event 物品注册事件。
     */
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        CCItems.register(event.getRegistry());
        CCItems.registerOreDictionary();
    }

    /**
     * 注册全部模组药水效果。
     *
     * @param event 药水注册事件。
     */
    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        CCPotions.register(event.getRegistry());
    }

    /**
     * 注册全部模组附魔。
     *
     * @param event 附魔注册事件。
     */
    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        CCEnchantments.register(event.getRegistry());
    }

    /**
     * 注册模组使用的自定义实体。
     *
     * @param event 实体注册事件。
     */
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
