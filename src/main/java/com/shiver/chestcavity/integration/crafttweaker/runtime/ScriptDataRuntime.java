package com.shiver.chestcavity.integration.crafttweaker.runtime;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.LinkedHashMap;
import java.util.Map;

@ZenClass(CtConstants.CT_NAMESPACE + "ScriptDataRuntime")
@ZenRegister
public class ScriptDataRuntime {

    private final Map<String, Object> values = new LinkedHashMap<String, Object>();

    @ZenMethod
    public boolean has(String key) {
        return values.containsKey(key);
    }

    @ZenMethod
    public void remove(String key) {
        values.remove(key);
    }

    @ZenMethod
    public void clear() {
        values.clear();
    }

    @ZenMethod
    public int getInt(String key) {
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    @ZenMethod
    public void setInt(String key, int value) {
        values.put(key, value);
    }

    @ZenMethod
    public float getFloat(String key) {
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).floatValue() : 0.0F;
    }

    @ZenMethod
    public void setFloat(String key, float value) {
        values.put(key, value);
    }

    @ZenMethod
    public boolean getBool(String key) {
        Object value = values.get(key);
        return value instanceof Boolean ? (Boolean) value : false;
    }

    @ZenMethod
    public void setBool(String key, boolean value) {
        values.put(key, value);
    }

    @ZenMethod
    public String getString(String key) {
        Object value = values.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    @ZenMethod
    public void setString(String key, String value) {
        values.put(key, value);
    }
}
