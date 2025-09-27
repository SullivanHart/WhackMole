package com.example.stopwatchsample;

import android.os.Handler;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Random;


public class WhackmoleViewModel extends ViewModel {
    private long startTime = 0L;
    private boolean running = false;

    private Handler handler = new Handler();
    private Runnable updateRunnable;

    private Random rand = new Random();

    private final MutableLiveData<Integer> activeMoleIndex = new MutableLiveData<>(-1);
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> lives = new MutableLiveData<>(0);
    private MutableLiveData<Long> elapsedTime = new MutableLiveData<>(0L);

    public LiveData<Long> getElapsedTime() {
        return elapsedTime;
    }
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

        elapsedTime.setValue(0L);
        score.setValue( 0 );

        if (!running) {
            startTime = SystemClock.elapsedRealtime() - (elapsedTime.getValue() != null ? elapsedTime.getValue() : 0);
            running = true;
            updateRunnable = new Runnable() {
                @Override
                public void run() {
                    elapsedTime.setValue(SystemClock.elapsedRealtime() - startTime);
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
    }
    // hide the mole
    public void hideMole() {
        activeMoleIndex.setValue( -1 );
    }

    // hit a hole
    public void hitHole(int index) {
        // check for mole
        if ( activeMoleIndex.getValue() != null && activeMoleIndex.getValue() == index ) {
            score.setValue( score.getValue() + 1 );
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
}
