package com.shiver.chestcavity.registry;

import com.shiver.chestcavity.Tags;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class CCTabs {

    public static final CreativeTabs MAIN = new CreativeTabs(Tags.MOD_ID + ".main") {
        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(CCItems.CHEST_OPENER);
        }
    };

    private CCTabs() {
    }
}