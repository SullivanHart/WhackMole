 package com.example.stopwatchsample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

 /**
  * The Whack-A-Mole activity ( game-screen ).
  */
 public class WhackmoleActivity extends AppCompatActivity implements GameOverFragment.GameOverListener {

    // Storage stuff
    private static final String PREFS_NAME = "WhackMolePrefs";
    private static final String KEY_HIGH_SCORE = "high_score";

    // Mole stuff
    private SoundPool soundPool;
    private int mole_dur = 1000;
    private int[] moleSounds;
    private Random random = new Random();

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
    private int gridSize = 3;

    private LinearLayout livesContainer;
    private List<ImageView> hearts = new ArrayList<>();
    private int numLives = 3;

    private TextView tvScore;
    private Button btnStartStop;

    private WhackmoleViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whackmole);

        // get UI refs
        gridHoles   = findViewById(R.id.gridHoles);
        tvScore     = findViewById(R.id.tvScore);
        btnStartStop = findViewById(R.id.btnStartStop);
        livesContainer = findViewById(R.id.livesContainer);

        // get high score
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0); // default 0 if not set


        // setup lives container
        setupLives(numLives);

        // setup the model
        viewModel = new ViewModelProvider(this).get(WhackmoleViewModel.class);

        // watch the score
        viewModel.getScore().observe(this, num ->
                tvScore.setText(String.format("Score: %d", num))
        );

        // watch the lives
        viewModel.getLives().observe(this, lives -> updateHearts( lives ) );

        // setup the start/stop button
        btnStartStop.setOnClickListener(v -> {
            if (viewModel.is_running()) {
                endGame();
            } else {
                startGame();
            }
        });

        // setup mole sounds
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        moleSounds = new int[]{
                soundPool.load(this, R.raw.ow0, 1),
                soundPool.load(this, R.raw.ow1, 1),
                soundPool.load(this, R.raw.ow2, 1),
                soundPool.load(this, R.raw.ow3, 1),
                soundPool.load(this, R.raw.ow4, 1),
                soundPool.load(this, R.raw.ow5, 1),
                soundPool.load(this, R.raw.ow6, 1),
                soundPool.load(this, R.raw.ow7, 1),
                soundPool.load(this, R.raw.ow8, 1),
                soundPool.load(this, R.raw.ow9, 1),
                soundPool.load(this, R.raw.ow10, 1),
                soundPool.load(this, R.raw.ow11, 1),
                soundPool.load(this, R.raw.ow12, 1),
                soundPool.load(this, R.raw.ow13, 1)
        };

        // setup the grid ( once its ready )
        gridHoles.post( () -> {
            int parentWidth = gridHoles.getWidth();
            int holeCellSize = parentWidth / gridSize;

            imgMoles = new ImageView[gridSize * gridSize];

            for (int i = 0; i < gridSize * gridSize; i++) {
                FrameLayout frame = new FrameLayout(this);

                int holeRes = holeDrawables[ random.nextInt(holeDrawables.length)];
                ImageView holeImg = new ImageView(this);
                holeImg.setImageResource(holeRes);
                holeImg.setScaleType(ImageView.ScaleType.FIT_CENTER);

                ImageView imgMole = new ImageView(this);
                imgMole.setImageResource(R.drawable.mole);
                imgMole.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imgMole.setVisibility(View.INVISIBLE);
                imgMoles[i] = imgMole;

                frame.addView(holeImg);
                frame.addView(imgMole);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = holeCellSize;
                params.height = holeCellSize;
                frame.setLayoutParams(params);

                int finalI = i;
                frame.setOnClickListener(v -> {
                        viewModel.hitHole(finalI);

                        // pick a random sound
                        int soundId = moleSounds[random.nextInt(moleSounds.length)];
                        int streamId = soundPool.play(soundId, 1f, 1f, 1, 0, 1f);

                        // stop sound after mole_dur
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            soundPool.stop(streamId);
                        }, mole_dur);
                });

                gridHoles.addView(frame);
            }

            // Now that grid is ready, watch mole index
            viewModel.getActiveMoleIndex().observe(this, index -> {
                for (int i = 0; i < imgMoles.length; i++) {
                    if (i == index) {
                        animateMolePop(imgMoles[i]);
                    } else if (imgMoles[i].getVisibility() == View.VISIBLE) {
                        animateMoleHide(imgMoles[i]);
                    }
                }
            } );
        } );
    }

    private void animateMole( ImageView moleImg, boolean show ) {
        int width = moleImg.getWidth();
        int height = moleImg.getHeight();
        int dur = 100; // dur / 1000 seconds

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

    private void setupLives(int count) {
        hearts.clear();
        livesContainer.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView heart = new ImageView(this);
            heart.setImageResource(R.drawable.heart);
            heart.setAdjustViewBounds(true);   // keep aspect ratio
            heart.setScaleType(ImageView.ScaleType.FIT_CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            );
            params.setMargins(8, 0, 8, 0);
            heart.setLayoutParams(params);

            livesContainer.addView(heart);
            hearts.add(heart);
        }
    }

    private void updateHearts(int lives) {
        // Update the heart images based on lives
        for (int i = 0; i < hearts.size(); i++) {
            if (i < lives) {
                hearts.get(i).setImageResource(R.drawable.heart);
            } else {
                hearts.get(i).setImageResource(R.drawable.heart_broken);
            }
        }

        if (lives == 0) {
            endGame();
        }
    }

    private void endGame(){
        viewModel.stop();

        btnStartStop.setText("Restart");
        int finalScore = viewModel.getScore().getValue() != null ? viewModel.getScore().getValue() : 0;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);

        if (finalScore > highScore) {
            // Save new high score
            prefs.edit().putInt( KEY_HIGH_SCORE, finalScore ).apply();
            highScore = finalScore;
        }

        GameOverFragment fragment = GameOverFragment.newInstance( finalScore, highScore );
        fragment.show(getSupportFragmentManager(), "GameOverDialog");
    }

    private void startGame() {
        btnStartStop.setText("Stop");
        viewModel.start();
    }

     /**
      * Restarts the game ( resets views, interact with the game model ).
      * Public so the fragment can reset the game.
      */
    @Override
    public void onRestartGame() {
        viewModel.reset();
        setupLives(numLives);
        startGame();
    }
}
