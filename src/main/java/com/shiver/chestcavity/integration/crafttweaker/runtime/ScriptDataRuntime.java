package com.shiver.chestcavity.integration.crafttweaker.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

public class ScriptDataRuntime {

    private final Map<String, Object> values = new LinkedHashMap<String, Object>();

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public void remove(String key) {
        values.remove(key);
    }

    public void clear() {
        values.clear();
    }

    public int getInt(String key) {
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    public void setInt(String key, int value) {
        values.put(key, value);
    }

    public float getFloat(String key) {
        Object value = values.get(key);
        return value instanceof Number ? ((Number) value).floatValue() : 0.0F;
    }

    public void setFloat(String key, float value) {
        values.put(key, value);
    }

    public boolean getBool(String key) {
        Object value = values.get(key);
        return value instanceof Boolean ? (Boolean) value : false;
    }

    public void setBool(String key, boolean value) {
        values.put(key, value);
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    public void setString(String key, String value) {
        values.put(key, value);
    }
}
