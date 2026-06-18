package com.shiver.chestcavity.integration.crafttweaker.runtime;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuntimeStateRegistry {

    private static final Map<Entity, ScriptDataRuntime> ENTITY_DATA = new IdentityHashMap<Entity, ScriptDataRuntime>();
    private static final Map<Entity, Map<String, ScriptDataRuntime>> SCORE_DATA = new IdentityHashMap<Entity, Map<String, ScriptDataRuntime>>();
    private static final Map<Entity, Map<String, ScriptDataRuntime>> ABILITY_DATA = new IdentityHashMap<Entity, Map<String, ScriptDataRuntime>>();

    private RuntimeStateRegistry() {
    }

    public static ScriptDataRuntime getEntityData(Entity entity) {
        if (entity == null) {
            return new ScriptDataRuntime();
        }
        ScriptDataRuntime data = ENTITY_DATA.get(entity);
        if (data == null) {
            data = new ScriptDataRuntime();
            ENTITY_DATA.put(entity, data);
        }
        return data;
    }

    public static void clearEntity(Entity entity) {
        if (entity != null) {
            ENTITY_DATA.remove(entity);
            SCORE_DATA.remove(entity);
            ABILITY_DATA.remove(entity);
        }
    }

    public static ScriptDataRuntime getScoreData(Entity entity, ResourceLocation scoreId) {
        return getScopedData(SCORE_DATA, entity, scoreId == null ? null : scoreId.toString());
    }

    public static ScriptDataRuntime getAbilityData(Entity entity, ResourceLocation abilityId) {
        return getScopedData(ABILITY_DATA, entity, abilityId == null ? null : abilityId.toString());
    }

    public static int getAbilityCooldown(Entity entity, ResourceLocation abilityId) {
        return getAbilityData(entity, abilityId).getInt("__cooldown");
    }

    public static void setAbilityCooldown(Entity entity, ResourceLocation abilityId, int cooldown) {
        getAbilityData(entity, abilityId).setInt("__cooldown", Math.max(0, cooldown));
    }

    public static int getAbilityActiveTicks(Entity entity, ResourceLocation abilityId) {
        return getAbilityData(entity, abilityId).getInt("__active_ticks");
    }

    public static void setAbilityActiveTicks(Entity entity, ResourceLocation abilityId, int ticks) {
        getAbilityData(entity, abilityId).setInt("__active_ticks", Math.max(0, ticks));
    }

    public static int getAbilityActiveTickIndex(Entity entity, ResourceLocation abilityId) {
        return getAbilityData(entity, abilityId).getInt("__active_tick_index");
    }

    public static void setAbilityActiveTickIndex(Entity entity, ResourceLocation abilityId, int tickIndex) {
        getAbilityData(entity, abilityId).setInt("__active_tick_index", Math.max(0, tickIndex));
    }

    private static ScriptDataRuntime getScopedData(Map<Entity, Map<String, ScriptDataRuntime>> root, Entity entity, String key) {
        if (entity == null || key == null || key.isEmpty()) {
            return new ScriptDataRuntime();
        }
        Map<String, ScriptDataRuntime> scoped = root.get(entity);
        if (scoped == null) {
            scoped = new LinkedHashMap<String, ScriptDataRuntime>();
            root.put(entity, scoped);
        }
        ScriptDataRuntime data = scoped.get(key);
        if (data == null) {
            data = new ScriptDataRuntime();
            scoped.put(key, data);
        }
        return data;
    }
}
