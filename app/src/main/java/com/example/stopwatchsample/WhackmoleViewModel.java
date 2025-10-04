package com.example.stopwatchsample;

import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contains the Whack-A-Mole game logic.
 */
public class WhackmoleViewModel extends ViewModel {

    private final int numHoles = 9;
    public final long moleDur = 5000;
    private final long spawnInterval = 1000;
    private final double rate = 0.9;


    private boolean running = false;
    private final Random rand = new Random();

    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> lives = new MutableLiveData<>(3);
    private final MutableLiveData<List<Integer>> activeMoles = new MutableLiveData<>(new ArrayList<>());

    private final Handler moleHandler = new Handler();
    private final java.util.Map<Integer, Runnable> moleRunnables = new java.util.HashMap<>();


    private final Runnable spawnRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            List<Integer> list = activeMoles.getValue() != null ?
                    new ArrayList<>(activeMoles.getValue()) :
                    new ArrayList<>();


            int targetMoles = 1 + (score.getValue() == null ? 0 : score.getValue() / 10 );
            targetMoles = Math.min(targetMoles, numHoles);

            if (list.size() < targetMoles) {
                addMole( targetMoles );
            }

            int level = ( score.getValue() == null ? 0 : score.getValue() ) / 10;
            if( level <= 0 ){
                moleHandler.post( this );
            } else {
                moleHandler.postDelayed(this, (long)( spawnInterval * Math.pow( rate, level ) ) );
            }
        }
    };

    /**
     * Returns the positions of the moles.
     * @return The indexes of the holes that contain moles.
     */
    public LiveData<List<Integer>> getActiveMoles() {
        return activeMoles;
    }

    /**
     * Returns the number of points that have accumulated in the current game.
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
        Log.d("WMVM", "Start() called");
        reset();
        Log.d("WMVM", "reset() success");
        running = true;
        moleHandler.post(spawnRunnable);
    }

    /**
     * Stops the game.
     */
    public void stop() {
        running = false;
        moleHandler.removeCallbacksAndMessages(null);
        moleRunnables.clear();
        activeMoles.postValue(new ArrayList<>());
    }


    /**
     * Returns whether the game is active.
     * @return The running boolean.
     */
    public boolean is_running() {
        return running;
    }

    /**
     * Hit the hole w/ the given index.
     * @param index  The index of the hole to hit.
     */
    public void hitHole(int index) {
        List<Integer> list = new ArrayList<>();
        if (activeMoles.getValue() != null) {
            list.addAll(activeMoles.getValue());
        }

        if (list.contains(index)) {
            Integer val = score.getValue();
            score.setValue(val != null ? val + 1 : 1);
            list.remove(Integer.valueOf(index));
            activeMoles.postValue(list);

            // cancel the timeout runnable for this mole
            Runnable toCancel = moleRunnables.remove(index);
            if (toCancel != null) {
                moleHandler.removeCallbacks(toCancel);
            }

            spawnMoles();
        }
    }

    // remove a life
    private void loseLife() {
        if (lives.getValue() != null) {
            int newLives = lives.getValue() - 1;
            lives.setValue(newLives);
            if (newLives <= 0) stop();
        }
    }

    /**
     * Resets the game.
     */
    public void reset() {
        stop();
        score.setValue(0);
        lives.setValue(3);
        activeMoles.postValue( new ArrayList<>() );

    }

    // add a mole with timer
    private void addMole(int targetMoles) {
        Log.d("WMVM", "addMole() called");

        if (!running) {
            Log.d("WMVM", "not running in addMole");
            return;
        }

        List<Integer> list = new ArrayList<>();
        if (activeMoles.getValue() != null) {
            list.addAll(activeMoles.getValue());
        }

        if (list.size() >= targetMoles) return;
        if (list.size() >= numHoles) {
            stop();
            Log.d("WMVM", "addMole() too many moles");
            return;
        }

        int moleIdx = rand.nextInt(numHoles);
        while (list.contains(moleIdx)) {
            moleIdx = rand.nextInt(numHoles);
        }

        final int finalMoleIdx = moleIdx;

        // add to active list
        list.add(finalMoleIdx);
        activeMoles.postValue(list);

        Runnable moleTimeout = () -> {
            List<Integer> timeoutList = new ArrayList<>();
            if (activeMoles.getValue() != null) {
                timeoutList.addAll(activeMoles.getValue());
            }

            if (timeoutList.contains(finalMoleIdx)) {
                timeoutList.remove(Integer.valueOf(finalMoleIdx));
                activeMoles.postValue(timeoutList);
                loseLife();

                spawnMoles();
            }

            moleRunnables.remove(finalMoleIdx);
        };

        moleRunnables.put(finalMoleIdx, moleTimeout);

        int level = (score.getValue() == null ? 0 : score.getValue() ) / 5;
        moleHandler.postDelayed(moleTimeout, (long)(moleDur * Math.pow(rate, level)));
    }

    // spawn moles until there are enough
    private void spawnMoles() {
        Log.d("WMVM", "spawnMoles() called");

        if (!running) return;

        List<Integer> list = activeMoles.getValue() != null ? new ArrayList<>(activeMoles.getValue()) : new ArrayList<>();

        int targetMoles = 1 + (score.getValue() == null ? 0 : score.getValue() / 10 );
        targetMoles = Math.min(targetMoles, numHoles);
        Log.d("WMVM", "targetMoles value set");

        if (list.size() < targetMoles) {
            addMole( targetMoles );
        }
    }
}
