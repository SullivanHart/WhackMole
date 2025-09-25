package com.example.whackamole.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.whackamole.util.ScoreManager;
import com.example.whackamole.controller.GameController;

/**
 * ViewModel that exposes LiveData for the UI and forwards actions to GameController.
 * Uses AndroidViewModel so we can access Application context for persistence.
 */
public class GameViewModel extends AndroidViewModel implements GameController.GameEventListener {

    private final GameController controller;
    private final MutableLiveData<int[]> holesLive = new MutableLiveData<>();
    private final MutableLiveData<Integer> scoreLive = new MutableLiveData<>();
    private final MutableLiveData<Integer> missesLive = new MutableLiveData<>();
    private final MutableLiveData<Integer> highScoreLive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> gameOverLive = new MutableLiveData<>();

    private final ScoreManager scoreManager;

    public GameViewModel(@NonNull Application application) {
        super(application);

        int numHoles = 9; // 3x3 grid
        int maxMisses = 3;

        controller = new GameController(numHoles, maxMisses);
        controller.setListener(this);

        scoreManager = new ScoreManager(application.getApplicationContext());

        // initialize
        holesLive.setValue(new int[numHoles]);
        missesLive.setValue(0);
        highScoreLive.setValue(scoreManager.getHighScore());
        gameOverLive.setValue(false);
    }

    // --- getters ---
    public LiveData<int[]> getHoles() {
        return holesLive;
    }

    public LiveData<Integer> getScore() {
        return scoreLive;
    }

    public LiveData<Integer> getMisses() {
        return missesLive;
    }

    public LiveData<Integer> getHighScore() {
        return highScoreLive;
    }

    public LiveData<Boolean> getGameOver() {
        return gameOverLive;
    }

    // --- commands to controller ---
    public void startGame() {
        gameOverLive.setValue(false);
        controller.start();
    }

    public void stopGame() {
        controller.stop();
    }

    public void resetGame() {
        controller.reset();
        scoreLive.setValue(0);
        missesLive.setValue(0);
        gameOverLive.setValue(false);
        highScoreLive.setValue(scoreManager.getHighScore());
    }

    /**
     * Attempt to tap a hole. Returns true if it was a hit (mole).
     * The controller will still post updates (score/misses) via the GameEventListener callbacks.
     *
     * @param index hole index tapped
     * @return true if hit, false otherwise
     */
    public boolean tapHole(int index) {
        return controller.tapHole(index);
    }

    // --- callbacks ---
    @Override
    public void onHolesUpdated(int[] holes) {
        holesLive.postValue(holes);
    }

    @Override
    public void onScoreUpdated(int score) {
        scoreLive.postValue(score);
    }

    @Override
    public void onMissesUpdated(int misses) {
        missesLive.postValue(misses);
    }

    @Override
    public void onGameOver(int finalScore) {
        int hs = scoreManager.getHighScore();
        if (finalScore > hs) {
            scoreManager.setHighScore(finalScore);
            highScoreLive.postValue(finalScore);
        }
        gameOverLive.postValue(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        controller.destroy();
    }
}
