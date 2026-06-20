package com.shiver.chestcavity.score;

public final class ScoreRef {

    private final String id;

    private ScoreRef(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Score id must not be empty");
        }
        this.id = id;
    }

    public static ScoreRef of(String id) {
        return new ScoreRef(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
