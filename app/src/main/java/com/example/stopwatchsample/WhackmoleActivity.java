 package com.example.stopwatchsample;

import java.util.Random;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class WhackmoleActivity extends AppCompatActivity {

    // Grid layouts
    private GridLayout grid = findViewById( R.id.gridHoles );
    private int[] holeDrawables = {
            R.drawable.hole0,
            R.drawable.hole1,
            R.drawable.hole2,
            R.drawable.hole3,
            R.drawable.hole4,
            R.drawable.hole5,
            R.drawable.hole6,
            R.drawable.hole7,
            R.drawable.hole8,
            R.drawable.hole9
    };
    private int gridSize = 3;

    private StopwatchViewModel viewModel;
    private TextView tvTime;
    private Button btnStartStop, btnReset;
    private ImageView[] Xs;

    private LiveData<Long> time;
    private long hours;
    private long minutes;
    private long seconds;
    private long tenths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whackmole);

        // Init create
        grid = findViewById(R.id.gridHoles);
        for( int i = 0; i < gridSize  * gridSize; i++ ){
            // create frame
            FrameLayout frame = new FrameLayout( this );

            // assign random hole
            int holeRes = holeDrawables[ new Random().nextInt( holeDrawables.length ) ];
            ImageView holeImg = new ImageView( this );
            holeImg.setImageResource( holeRes );
            holeImg.setScaleType( ImageView.ScaleType.FIT_CENTER );

            // assign mole
            int moleRes = R.drawable.mole;
            ImageView moleImg = new ImageView( this );
            moleImg.setImageResource( moleRes );
            moleImg.setScaleType( ImageView.ScaleType.FIT_CENTER );

            // add to frame
            frame.addView( holeImg );
            frame.addView( moleImg );

            // make cells square
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            int size = (int) getResources().getDisplayMetrics().density*100; // 100 dp
            params.width = size;
            params.height = size;
            frame.setLayoutParams( params );

            // click
            int finalI = i;
            frame.setOnClickListener( v -> {
                Toast.makeText( this, "Clicked hole: " + finalI, Toast.LENGTH_SHORT ).show();
            });
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(StopwatchViewModel.class);

        // Initialize UI components (TextView, Buttons)
        tvTime = findViewById(R.id.tvTime);
        Xs = new ImageView[] {
                findViewById(R.id.x0), findViewById(R.id.x1), findViewById(R.id.x2)
            };

        // Format elapsed time for display
        time = viewModel.getElapsedTime();
        time.observe(this, new Observer<Long>() {
                @Override
                public void onChanged(Long num) {
                    // Update your UI here
                    hours = num / 3600000; // 3600 * 1000 ms in an hour
                    minutes = ( num / 60000 ) % 60; // 60 * 1000 ms in a minute. only show remainder minutes
                    seconds = ( num / 1000 ) % 60; // 1000 ms in a second. only show remainder seconds
                    tenths = ( num / 100 ) % 10; // 100 ms in a tenth. only show remainder tenths

                    tvTime.setText(String.format("%02d:%02d:%02d.%01d", hours, minutes, seconds, tenths));
                }
            }
        );

        // Set up button listeners for Start/Stop and Reset
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( viewModel.is_running() ){
                    viewModel.stop();
                } else {
                    viewModel.start();
                }
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.reset();
            }
        });
    }

}
