 package com.example.stopwatchsample;

import java.util.Random;

import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class WhackmoleActivity extends AppCompatActivity {

    // Grid layouts
    private GridLayout gridHoles;
    private ImageView[] imgMoles;
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
    private int numLives = 3;
    private int gridSize = 3;

    private WhackmoleViewModel viewModel;
    private TextView tvTime, tvScore;
    private Button btnStartStop, btnReset;
    private ImageView[] Xs;
    private LiveData<Integer> score;

    private LiveData<Long> time;
    private long hours;
    private long minutes;
    private long seconds;
    private long tenths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whackmole);

        gridHoles = findViewById(R.id.gridHoles);
        gridHoles.post(() -> {
            int parentWidth = gridHoles.getWidth();


            int xCellSize = parentWidth / numLives;


            int holeCellSize = parentWidth / gridSize;

            imgMoles = new ImageView[gridSize * gridSize];

            // Init create
            for (int i = 0; i < gridSize * gridSize; i++) {
                // create frame
                FrameLayout frame = new FrameLayout(this);

                // assign random hole
                int holeRes = holeDrawables[new Random().nextInt(holeDrawables.length)];
                ImageView holeImg = new ImageView(this);
                holeImg.setImageResource(holeRes);
                holeImg.setScaleType(ImageView.ScaleType.FIT_CENTER);

                // assign mole
                int resMole = R.drawable.mole;
                ImageView imgMole = new ImageView(this);
                imgMole.setImageResource(resMole);
                imgMole.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imgMole.setVisibility(View.INVISIBLE);
                imgMoles[ i ] = imgMole;

                // add to frame
                frame.addView(holeImg);
                frame.addView(imgMole);

                // make cells square
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = holeCellSize;
                params.height = holeCellSize;
                frame.setLayoutParams(params);

                // click
                int finalI = i;
                frame.setOnClickListener(v -> {
                    if( finalI == viewModel.getActiveMoleIndex().getValue() ){
                        animateMoleHide( imgMoles[ finalI ] );
                    }
                    viewModel.hitHole( finalI );
                });

                // add to grid
                gridHoles.addView(frame);
            }

            // Initialize ViewModel
            viewModel = new ViewModelProvider(this).get(WhackmoleViewModel.class);

            // Observe mole index
            viewModel.getActiveMoleIndex().observe(this, index -> {
                for (int i = 0; i < imgMoles.length; i++) {
                    if( i == index ){
                        animateMolePop( imgMoles[i] ); // animate mole popping
                    }
                }
            });

            // Initialize UI components (TextView, Buttons)
            tvTime = findViewById(R.id.tvTime);
            tvScore = findViewById(R.id.tvScore);
            Xs = new ImageView[] {
                    findViewById(R.id.x0), findViewById(R.id.x1), findViewById(R.id.x2)
                };
            btnStartStop = findViewById( R.id.btnStartStop );
            btnReset = findViewById( R.id.btnReset );

            // Format elapsed time for display
            time = viewModel.getElapsedTime();
            time.observe(this, new Observer<Long>() {
                    @Override
                    public void onChanged(Long num) {
                        // Update UI
                        hours = num / 3600000; // 3600 * 1000 ms in an hour
                        minutes = ( num / 60000 ) % 60; // 60 * 1000 ms in a minute. only show remainder minutes
                        seconds = ( num / 1000 ) % 60; // 1000 ms in a second. only show remainder seconds
                        tenths = ( num / 100 ) % 10; // 100 ms in a tenth. only show remainder tenths

                        tvTime.setText(String.format("%02d:%02d:%02d.%01d", hours, minutes, seconds, tenths));
                    }
                }
            );

            score = viewModel.getScore();
            score.observe(this, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer num) {
                        // Update UI
                        tvScore.setText( String.format("Score: %d", num ));
                    }
                }
            );

            // Set up button listeners for Start/Stop and Reset
            btnStartStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( viewModel.is_running() ){
                        viewModel.stop();
                        btnStartStop.setText("Start");
                    } else {
                        viewModel.start();
                        btnStartStop.setText("Stop");
                    }
                }
            });
        } );
    }
    private void animateMole(ImageView moleImg, boolean show) {
        int width = moleImg.getWidth();
        int height = moleImg.getHeight();
        int dur = 500; // .5 second

        int hiddenY = (int) (height * 0.6f);

        moleImg.setVisibility(View.VISIBLE);

        // Start at appropriate Y
        moleImg.setTranslationY(show ? hiddenY : 0);

        // Start clip depending on direction
        final Rect clip = new Rect(0, 0, width, show ? 0 : height / 2);
        moleImg.setClipBounds(clip);

        ValueAnimator animator = ValueAnimator.ofInt(
                show ? 0 : height / 2, // from
                show ? height / 2 : 0 // to
        );
        animator.setDuration(dur);
        animator.addUpdateListener(animation -> {
            int clipBottom = (int) animation.getAnimatedValue();
            clip.bottom = clipBottom;
            moleImg.setClipBounds(clip);
        });

        moleImg.animate()
                .translationY(show ? 0 : hiddenY)
                .setDuration(dur)
                .setInterpolator(new PathInterpolator(0.5f, 0, 0.5f, 1f))
                .withEndAction(() -> {
                    if (!show) moleImg.setVisibility(View.INVISIBLE); // fully hide after retract
                })
                .start();

        animator.start();
    }

    // wrappers
    private void animateMolePop(ImageView moleImg) {
        animateMole(moleImg, true);
    }

    private void animateMoleHide(ImageView moleImg) {
        animateMole(moleImg, false);
    }

    private void loseLife() {
        if (numLives > 0) {
            numLives--;
            Xs[  numLives ].setVisibility(View.VISIBLE); // show an X
        }
        if (numLives == 0) {
            // TODO: end game logic (stop timer, etc.)
        }
    }

}
