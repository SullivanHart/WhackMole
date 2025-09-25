package com.example.stopwatchsample;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.os.Handler;
import android.os.SystemClock;


public class StopwatchViewModel extends ViewModel {
    private long startTime = 0L;
    private boolean running = false;

    private Handler handler = new Handler();
    private Runnable updateRunnable;

    private MutableLiveData<Long> elapsedTime = new MutableLiveData<>(0L);

    public LiveData<Long> getElapsedTime() {
        return elapsedTime;
    }

    public void start() {
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
        }
    }

    public void stop() {
        if (running) {
            handler.removeCallbacks(updateRunnable);
            running = false;
        }
    }

    public void reset() {
        if (running) {
            startTime = SystemClock.elapsedRealtime();
            elapsedTime.setValue(0L);
        }
        elapsedTime.setValue(0L);
    }

    public boolean is_running(){
        return running;
    }
}
