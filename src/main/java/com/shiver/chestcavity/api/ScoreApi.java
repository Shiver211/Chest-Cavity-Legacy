package com.shiver.chestcavity.api;

import com.shiver.chestcavity.score.Score;
import com.shiver.chestcavity.score.ScoreRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ScoreApi {

    private final Map<String, Score> scores = new LinkedHashMap<>();
    private int nextIndex;

    ScoreApi() {
    }

    public void addScore(String scoreId, String displayName) {
        if (scoreId == null) {
            return;
        }
        Score score = scores.get(scoreId);
        if (score == null) {
            score = new Score(scoreId, displayName);
            register(score);
        } else if (displayName != null) {
            score.setDisplayName(displayName);
        }
    }

    public void addScore(ScoreRef scoreRef, String displayName) {
        if (scoreRef != null) {
            addScore(scoreRef.getId(), displayName);
        }
    }

    public void register(Score score) {
        if (score != null) {
            Score existing = scores.get(score.getId());
            if (existing != null && existing.getIndex() >= 0) {
                score.setIndex(existing.getIndex());
            } else if (score.getIndex() < 0) {
                score.setIndex(nextIndex++);
            } else {
                nextIndex = Math.max(nextIndex, score.getIndex() + 1);
            }
            scores.put(score.getId(), score);
        }
    }

    public Score getScore(String scoreId) {
        return scoreId == null ? null : scores.get(scoreId);
    }

    public Score getScore(ScoreRef scoreRef) {
        return scoreRef == null ? null : getScore(scoreRef.getId());
    }

    public Score getOrCreateScore(String scoreId) {
        if (scoreId == null) {
            return null;
        }
        Score score = scores.get(scoreId);
        if (score == null) {
            score = new Score(scoreId);
            register(score);
        }
        return score;
    }

    public Score getOrCreateScore(ScoreRef scoreRef) {
        return scoreRef == null ? null : getOrCreateScore(scoreRef.getId());
    }

    public int getScoreCount() {
        return nextIndex;
    }

    public int getIndex(String scoreId) {
        Score score = getScore(scoreId);
        return score == null ? -1 : score.getIndex();
    }

    public int getIndex(ScoreRef scoreRef) {
        Score score = getScore(scoreRef);
        return score == null ? -1 : score.getIndex();
    }

    public Map<String, Score> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    public List<Score> getScoresInDisplayOrder() {
        List<Score> result = new ArrayList<>(scores.values());
        result.sort(Comparator.comparingInt(Score::getDisplayOrder).thenComparing(Score::getId));
        return Collections.unmodifiableList(result);
    }

    public List<ScoreValue> getSummaryScores(Map<String, Float> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScoreValue> result = new ArrayList<>();
        for (Score score : getScoresInDisplayOrder()) {
            if (!score.isSummaryVisible()) {
                continue;
            }
            Float value = values.get(score.getId());
            if (value != null && value > 0.0F) {
                result.add(new ScoreValue(score, value));
            }
        }
        for (Map.Entry<String, Float> entry : values.entrySet()) {
            if (entry.getValue() > 0.0F && !scores.containsKey(entry.getKey())) {
                result.add(new ScoreValue(getOrCreateScore(entry.getKey()), entry.getValue()));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public String getDisplayName(String scoreId) {
        Score score = getScore(scoreId);
        return score == null ? null : score.getDisplayName();
    }

    public String getDisplayName(ScoreRef scoreRef) {
        Score score = getScore(scoreRef);
        return score == null ? null : score.getDisplayName();
    }

    public Map<String, String> getDisplayNames() {
        Map<String, String> displayNames = new LinkedHashMap<>();
        for (Map.Entry<String, Score> entry : scores.entrySet()) {
            if (entry.getValue().getDisplayName() != null) {
                displayNames.put(entry.getKey(), entry.getValue().getDisplayName());
            }
        }
        return Collections.unmodifiableMap(displayNames);
    }

    public static final class ScoreValue {
        private final Score score;
        private final float value;

        private ScoreValue(Score score, float value) {
            this.score = score;
            this.value = value;
        }

        public Score getScore() {
            return score;
        }

        public String getId() {
            return score.getId();
        }

        public float getValue() {
            return value;
        }
    }
}
