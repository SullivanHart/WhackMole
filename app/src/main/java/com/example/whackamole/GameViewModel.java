package com.example.whackamole;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

        // configure number of holes and allowed misses here
        int numHoles = 9; // 3x3 grid
        int maxMisses = 3;

        controller = new GameController(numHoles, maxMisses);
        controller.setListener(this);

        scoreManager = new ScoreManager(application.getApplicationContext());

        // initialize LiveData with sensible defaults
        holesLive.setValue(new int[numHoles]); // all zeros (no mole)
        scoreLive.setValue(0);
        missesLive.setValue(0);
        highScoreLive.setValue(scoreManager.getHighScore());
        gameOverLive.setValue(false);
    }

    // --- LiveData getters ---
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

    // --- Commands forwarded to controller ---
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
     * Attempt to tap a hole. Returns true if it was a hit (mole present).
     * The controller will still post updates (score/misses) via the GameEventListener callbacks.
     *
     * @param index hole index tapped
     * @return true if hit, false otherwise
     */
    public boolean tapHole(int index) {
        return controller.tapHole(index);
    }

    // --- GameController.GameEventListener callbacks ---
    @Override
    public void onHolesUpdated(int[] holes) {
        // postValue so it works from background threads as well
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
        // persist high score if necessary
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
        // cleanup controller to avoid leaked handlers/runnables
        controller.destroy();
    }
}
