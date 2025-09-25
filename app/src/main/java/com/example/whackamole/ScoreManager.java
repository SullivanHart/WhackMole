package com.example.whackamole;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple persistence wrapper for high score using SharedPreferences.
 */
public class ScoreManager {

    private static final String PREFS = "whack_prefs";
    private static final String KEY_HIGH = "high_score";
    private final SharedPreferences prefs;

    public ScoreManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getHighScore() {
        return prefs.getInt(KEY_HIGH, 0);
    }

    public void setHighScore(int newHigh) {
        prefs.edit().putInt(KEY_HIGH, newHigh).apply();
    }
}
