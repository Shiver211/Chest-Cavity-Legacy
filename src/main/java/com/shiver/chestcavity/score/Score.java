package com.shiver.chestcavity.score;

import com.shiver.chestcavity.api.ScoreApi;

public class Score {

    private final String id;
    private int index = -1;
    private String displayName;
    private int displayOrder = Integer.MAX_VALUE;
    private boolean negative;
    private boolean summaryVisible = true;

    public Score(String id) {
        this(id, null);
    }

    public Score(String id, String displayName) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Score id must not be empty");
        }
        this.id = id;
        this.displayName = displayName;
    }

    public final String getId() {
        return id;
    }

    public final int getIndex() {
        return index;
    }

    public final void setIndex(int index) {
        if (this.index >= 0 && this.index != index) {
            throw new IllegalStateException("Score index for " + id + " is already assigned");
        }
        this.index = index;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Score setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public Score setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
        return this;
    }

    public boolean isNegative() {
        return negative;
    }

    public Score setNegative(boolean negative) {
        this.negative = negative;
        return this;
    }

    public boolean isSummaryVisible() {
        return summaryVisible;
    }

    public Score setSummaryVisible(boolean summaryVisible) {
        this.summaryVisible = summaryVisible;
        return this;
    }

    public void register(ScoreApi registry) {
        if (registry != null) {
            registry.register(this);
        }
    }
}
