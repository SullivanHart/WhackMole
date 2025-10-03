package com.example.stopwatchsample;

import android.os.Handler;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Random;

/**
 * Contains the Whack-A-Mole game logic.
 */
public class WhackmoleViewModel extends ViewModel {
    private boolean running = false;

    private Handler handler = new Handler();
    private Runnable updateRunnable;

    private Random rand = new Random();

    private final MutableLiveData<Integer> activeMoleIndex = new MutableLiveData<>(-1);
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> lives = new MutableLiveData<>(3);
    private Handler moleHandler = new Handler();
    private Runnable moleTimeoutRunnable;

    /**
     * Returns the position of the mole.
     * @return The index of the hole that contains the mole.
     */
    public LiveData<Integer> getActiveMoleIndex() {
        return activeMoleIndex;
    }

    /**
     * Returns the number of point the player has accumulated in the current the game.
     * @return The score.
     */
    public LiveData<Integer> getScore() {
        return score;
    }

    /**
     * Returns the number of lives the player has remaining before the game ends.
     * @return The number of lives remaining.
     */
    public LiveData<Integer> getLives() {
        return lives;
    }

    /**
     * Starts the game.
     */
    public void start() {

        reset();

        if (!running) {
            running = true;
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(this, 10); // update every 10ms
                }
            };
            handler.post(updateRunnable);
            showMole();
        }
    }

    /**
     * Stops the game.
     */
    public void stop() {
        if (running) {
            handler.removeCallbacks(updateRunnable);
            running = false;
            hideMole();
        }
    }

    /**
     * Returns whether the game is active.
     * @return The running boolean.
     */
    public boolean is_running(){
        return running;
    }

    /**
     * Makes a mole appear in a random hole.
     */
    public void showMole() {
        activeMoleIndex.setValue( rand.nextInt(9) );

        moleHandler.removeCallbacks(moleTimeoutRunnable);

        moleTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                // if mole is still active ( hasn't been clicked )
                if (activeMoleIndex.getValue() != null && activeMoleIndex.getValue() != -1) {
                    loseLife();
                    hideMole();
                    // spawn another mole ( if another life )
                    if (lives.getValue() != null && lives.getValue() > 0) {
                        showMole();
                    }
                }
            }
        };

        // Every 5 hits, reduce the time. min .5
        long timeout = (long) ( 5000 * Math.pow( 0.75, Math.floor( score.getValue() / 5 ) ) );
        moleHandler.postDelayed( moleTimeoutRunnable, Math.max( timeout, 500 ) );
    }

    /**
     * Hides the current mole.
     */
    public void hideMole() {
        activeMoleIndex.setValue( -1 );
        moleHandler.removeCallbacks( moleTimeoutRunnable );
    }

    /**
     * Hit the hole w/ the given index.
     * @param index  The index of the hole to hit.
     */
    public void hitHole(int index) {
        // check for mole
        if ( activeMoleIndex.getValue() != null && activeMoleIndex.getValue() == index ) {
            Integer val = score.getValue();
            score.setValue( val != null ? val + 1 : 1 );
            hideMole();
            showMole();
        }
    }

    // reduce life
    private void loseLife() {
        if (lives.getValue() != null) {
            int newLives = lives.getValue() - 1;
            lives.setValue(newLives);
            if (newLives <= 0) {
                stop(); // game over
            }
        }
    }

    /**
     * Resets the game.
     */
    public void reset() {
        score.setValue( 0 );
        lives.setValue( 3 );
        stop();
    }
}
