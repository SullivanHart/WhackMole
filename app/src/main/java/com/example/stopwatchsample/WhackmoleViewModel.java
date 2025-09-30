package com.example.stopwatchsample;

import android.os.Handler;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Random;


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

    public LiveData<Integer> getActiveMoleIndex() {
        return activeMoleIndex;
    }
    public LiveData<Integer> getScore() {
        return score;
    }
    public LiveData<Integer> getLives() {
        return lives;
    }

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

    public void stop() {
        if (running) {
            handler.removeCallbacks(updateRunnable);
            running = false;
            hideMole();
        }
    }

    public boolean is_running(){
        return running;
    }

    // Make a mole appear ( so it can be hit )
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
    // hide the mole
    public void hideMole() {
        activeMoleIndex.setValue( -1 );
        moleHandler.removeCallbacks( moleTimeoutRunnable );
    }

    // hit a hole
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

    // reset
    public void reset() {
        score.setValue( 0 );
        lives.setValue( 3 );
        stop();
    }
}
