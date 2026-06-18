package com.shiver.chestcavity.script.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ScriptScoreCallbacks {

    private final EnumMap<ScriptScoreEvent, Object> callbacks = new EnumMap<ScriptScoreEvent, Object>(ScriptScoreEvent.class);

    public ScriptScoreCallbacks() {
    }

    public ScriptScoreCallbacks(ScriptScoreCallbacks other) {
        if (other != null) {
            callbacks.putAll(other.callbacks);
        }
    }

    public void set(ScriptScoreEvent event, Object callback) {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null");
        }
        if (callback == null) {
            callbacks.remove(event);
        } else {
            callbacks.put(event, callback);
        }
    }

    public Object get(ScriptScoreEvent event) {
        return event == null ? null : callbacks.get(event);
    }

    public Map<ScriptScoreEvent, Object> asMap() {
        return Collections.unmodifiableMap(callbacks);
    }
}
