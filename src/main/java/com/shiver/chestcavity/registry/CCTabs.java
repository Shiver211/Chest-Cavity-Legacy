package com.shiver.chestcavity.registry;

import com.shiver.chestcavity.Tags;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 定义模组使用的创造标签页。
 */
public final class CCTabs {

    public static final CreativeTabs MAIN = new CreativeTabs(Tags.MOD_ID + ".main") {
        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(CCItems.CHEST_OPENER);
        }
    };

    /**
     * 工具类，不允许外部实例化。
     */
    private CCTabs() {
    }
}
