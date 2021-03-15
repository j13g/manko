package de.j13g.manko.core.managers;

import java.io.Serializable;
import java.util.HashMap;

public class ScoreManager<E> implements Serializable {

    private static final int MIN_SCORE = 0;
    private static final int DEFAULT_SCORE = MIN_SCORE;

    private final HashMap<E, Integer> scores = new HashMap<>();

    /**
     * Explicitly adds an entrant with default score.
     * @param entrant The entrant to add.
     */
    public void add(E entrant) {
        setScore(entrant, DEFAULT_SCORE);
    }

    /**
     * Increments the score of an entrant.
     * @param entrant The entrant.
     * @return Their new score.
     */
    public int incrementScore(E entrant) {
        return addScore(entrant, 1);
    }

    /**
     * Decrements the score of an entrant.
     * @param entrant The entrant.
     * @return Their new score.
     */
    public int decrementScore(E entrant) {
        return addScore(entrant, -1);
    }

    /**
     * Resets the score of an entrant.
     * @param entrant The entrant.
     * @return Their old score.
     */
    public int resetScore(E entrant) {
        int score = getOrDefault(entrant);
        scores.remove(entrant);
        return score;
    }

    public int getScore(E entrant) {
        return getOrDefault(entrant);
    }

    /**
     * Adds a value to the score of an entrant.
     * @param entrant The entrant.
     * @param value The value to add
     * @return The entrant's new score.
     */
    private int addScore(E entrant, int value) {
        int newScore = getOrDefault(entrant) + value;
        setScore(entrant, newScore);
        return newScore;
    }

    /**
     * Sets the score of an entrant.
     * @param entrant The entrant.
     * @param score The new score.
     */
    private void setScore(E entrant, int score) {
        scores.put(entrant, score);
    }

    private int getOrDefault(E entrant) {
        return scores.getOrDefault(entrant, DEFAULT_SCORE);
    }
}
