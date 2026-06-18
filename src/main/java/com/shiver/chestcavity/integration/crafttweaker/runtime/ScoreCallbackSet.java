package com.shiver.chestcavity.integration.crafttweaker.runtime;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ScoreCallbackSet {

    private final EnumMap<ScoreEvent, Object> callbacks = new EnumMap<ScoreEvent, Object>(ScoreEvent.class);

    public ScoreCallbackSet() {
    }

    public ScoreCallbackSet(ScoreCallbackSet other) {
        if (other != null) {
            callbacks.putAll(other.callbacks);
        }
    }

    public void set(ScoreEvent event, Object callback) {
        if (event == null) {
            throw new IllegalArgumentException("event cannot be null");
        }
        if (callback == null) {
            callbacks.remove(event);
        } else {
            callbacks.put(event, callback);
        }
    }

    public Object get(ScoreEvent event) {
        return event == null ? null : callbacks.get(event);
    }

    public Map<ScoreEvent, Object> asMap() {
        return Collections.unmodifiableMap(callbacks);
    }
}
