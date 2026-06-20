package com.shiver.chestcavity.layout;

import com.shiver.chestcavity.Tags;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ChestLayouts {

    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(Tags.MOD_ID, "default_27");
    public static final ChestLayoutDef DEFAULT = new ChestLayoutDef(
            DEFAULT_ID,
            27,
            9,
            176,
            168,
            8,
            6,
            8,
            18,
            18,
            18);

    private static final Map<ResourceLocation, ChestLayoutDef> LAYOUTS = new LinkedHashMap<>();

    static {
        register(DEFAULT);
    }

    private ChestLayouts() {
    }

    public static void register(ChestLayoutDef layout) {
        if (layout != null) {
            LAYOUTS.put(layout.getId(), layout);
        }
    }

    public static void replaceAll(Map<ResourceLocation, ChestLayoutDef> layouts) {
        LAYOUTS.clear();
        LAYOUTS.put(DEFAULT_ID, DEFAULT);
        if (layouts != null) {
            LAYOUTS.putAll(layouts);
        }
    }

    public static ChestLayoutDef get(ResourceLocation id) {
        ChestLayoutDef layout = id == null ? null : LAYOUTS.get(id);
        return layout == null ? DEFAULT : layout;
    }

    public static ChestLayoutDef getDefault() {
        return DEFAULT;
    }

    public static Map<ResourceLocation, ChestLayoutDef> getLayouts() {
        return Collections.unmodifiableMap(LAYOUTS);
    }
}
