package com.example.whackamole.controller;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Random;

/**
 * Controller for Whack-a-Mole game logic.
 * Handles spawning moles, hiding them after a timeout, counting hits and misses,
 * and scaling difficulty over time.
 */
public class GameController {

    /** Listener interface to report events back to the ViewModel/UI. */
    public interface GameEventListener {
        void onHolesUpdated(@NonNull int[] holes); // 1 = mole visible, 0 = empty
        void onScoreUpdated(int score);
        void onMissesUpdated(int misses);
        void onGameOver(int finalScore);
    }

    private final int numHoles;
    private final boolean[] occupied;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final HashMap<Integer, Runnable> hideRunnables = new HashMap<>();

    private GameEventListener listener;

    // gameplay params (modifiable)
    private long spawnIntervalMs = 1200L;
    private long moleVisibleMs = 1000L;
    private final long minSpawnIntervalMs = 350L;
    private final long minMoleVisibleMs = 250L;

    private int score = 0;
    private int misses = 0;
    private final int maxMisses;

    private boolean running = false;
    private final Runnable spawnRunnable = new Runnable() {
        @Override
        public void run() {
            spawnMole();
            if (running) handler.postDelayed(this, spawnIntervalMs);
        }
    };

    private int totalSpawns = 0; // used for difficulty scaling

    public GameController(int numHoles, int maxMisses) {
        this.numHoles = numHoles;
        this.maxMisses = maxMisses;
        this.occupied = new boolean[numHoles];
    }

    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }

    /** Start the game (resets state). */
    public void start() {
        resetState();
        running = true;
        handler.post(spawnRunnable);
        notifyHoles();
        notifyScore();
        notifyMisses();
    }

    /** Stop/pause the game (keeps state). */
    public void stop() {
        running = false;
        handler.removeCallbacks(spawnRunnable);
        cancelAllHideRunnables();
    }

    /** Reset game state but doesn't automatically restart. */
    public void reset() {
        stop();
        resetState();
        notifyHoles();
        notifyScore();
        notifyMisses();
    }

    private void resetState() {
        for (int i = 0; i < numHoles; ++i) occupied[i] = false;
        cancelAllHideRunnables();
        score = 0;
        misses = 0;
        spawnIntervalMs = 1200L;
        moleVisibleMs = 1000L;
        totalSpawns = 0;
    }

    private void cancelAllHideRunnables() {
        for (Runnable r : hideRunnables.values()) {
            handler.removeCallbacks(r);
        }
        hideRunnables.clear();
    }

    private void spawnMole() {
        int idx = pickRandomUnoccupiedHole();
        if (idx < 0) return;
        occupied[idx] = true;
        totalSpawns++;

        final int holeIndex = idx;
        Runnable hideRunnable = new Runnable() {
            @Override
            public void run() {
                if (occupied[holeIndex]) {
                    occupied[holeIndex] = false;
                    hideRunnables.remove(holeIndex);
                    misses++;
                    notifyHoles();
                    notifyMisses();
                    checkGameOver();
                }
            }
        };
        hideRunnables.put(holeIndex, hideRunnable);
        handler.postDelayed(hideRunnable, moleVisibleMs);

        notifyHoles();

        if (totalSpawns % 5 == 0) {
            spawnIntervalMs = Math.max(minSpawnIntervalMs, (long) (spawnIntervalMs - 75));
            moleVisibleMs = Math.max(minMoleVisibleMs, (long) (moleVisibleMs - 50));
        }
    }

    private int pickRandomUnoccupiedHole() {
        int attempts = 0;
        int maxAttempts = numHoles * 2;
        while (attempts < maxAttempts) {
            int idx = random.nextInt(numHoles);
            if (!occupied[idx]) return idx;
            attempts++;
        }
        for (int i = 0; i < numHoles; ++i) if (!occupied[i]) return i;
        return -1; // all occupied
    }

    /**
     * Called when the UI taps a hole.
     * @param index hole index tapped
     * @return true if it was a hit (mole visible), false otherwise
     */
    public boolean tapHole(int index) {
        if (index < 0 || index >= numHoles) return false;
        if (occupied[index]) {
            // hit
            occupied[index] = false;
            // cancel scheduled hide
            Runnable r = hideRunnables.remove(index);
            if (r != null) handler.removeCallbacks(r);
            score++;
            notifyHoles();
            notifyScore();
            return true;
        } else {
            return false;
        }
    }

    private void checkGameOver() {
        if (misses >= maxMisses) {
            running = false;
            handler.removeCallbacks(spawnRunnable);
            cancelAllHideRunnables();
            if (listener != null) listener.onGameOver(score);
        }
    }

    private void notifyHoles() {
        if (listener != null) {
            int[] arr = new int[numHoles];
            for (int i = 0; i < numHoles; ++i) arr[i] = occupied[i] ? 1 : 0;
            listener.onHolesUpdated(arr);
        }
    }

    private void notifyScore() {
        if (listener != null) listener.onScoreUpdated(score);
    }

    private void notifyMisses() {
        if (listener != null) listener.onMissesUpdated(misses);
    }

    /** Clean up handler callbacks call when ViewModel is cleared. */
    public void destroy() {
        stop();
        handler.removeCallbacksAndMessages(null);
    }

    public boolean isRunning() {
        return running;
    }
}
