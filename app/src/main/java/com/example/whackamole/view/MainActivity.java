package com.example.whackamole.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.whackamole.R;
import com.example.whackamole.util.SoundManager;
import com.example.whackamole.viewmodel.GameViewModel;

/**
 * Main Activity for Whack-a-Mole game. Wires UI to GameViewModel.
 */
public class MainActivity extends AppCompatActivity {
    private GameViewModel viewModel;
    private ImageView[] holes;
    private TextView tvScore;
    private TextView tvHighScore;
    private TextView tvMisses;
    private Button btnStart;
    private Button btnReset;
    private SoundManager soundManager;
    private int[] previousHoles; // track previous state to detect new spawns

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundManager = new SoundManager(this);
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        // UI references
        holes = new ImageView[] {
                findViewById(R.id.hole0),
                findViewById(R.id.hole1),
                findViewById(R.id.hole2),
                findViewById(R.id.hole3),
                findViewById(R.id.hole4),
                findViewById(R.id.hole5),
                findViewById(R.id.hole6),
                findViewById(R.id.hole7),
                findViewById(R.id.hole8)
        };

        tvScore = findViewById(R.id.tvScore);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvMisses = findViewById(R.id.tvMisses);
        btnStart = findViewById(R.id.btnStart);
        btnReset = findViewById(R.id.btnReset);

        previousHoles = new int[holes.length]; // all zeros at start

        // Set listeners for hole clicks
        for (int i = 0; i < holes.length; ++i) {
            final int idx = i;
            holes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean hit = viewModel.tapHole(idx);
                    if (hit) {
                        soundManager.playHit();
                    }
                }
            });
        }

        // Look at hole state and play spawn sound when a hole changes
        viewModel.getHoles().observe(this, new Observer<int[]>() {
            @Override
            public void onChanged(int[] holesState) {
                if (holesState == null) return;

                for (int i = 0; i < holesState.length && i < holes.length; ++i) {
                    if (holesState[i] == 1) {
                        holes[i].setImageResource(R.drawable.mole_up);
                    } else {
                        holes[i].setImageResource(R.drawable.hole_empty);
                    }

                    if (previousHoles != null && previousHoles.length == holesState.length) {
                        if (previousHoles[i] == 0 && holesState[i] == 1) {
                            // play spawn SFX
                            soundManager.playSpawn();
                        }
                    }
                }

                System.arraycopy(holesState, 0, previousHoles, 0, Math.min(previousHoles.length, holesState.length));
            }
        });

        // score
        viewModel.getScore().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer score) {
                tvScore.setText("Score: " + score);
            }
        });

        viewModel.getMisses().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer misses) {
                tvMisses.setText("Misses: " + misses);
            }
        });

        viewModel.getHighScore().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer high) {
                tvHighScore.setText("High: " + high);
            }
        });

        viewModel.getGameOver().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isOver) {
                if (isOver != null && isOver) {
                    int finalScore = viewModel.getScore().getValue() != null ? viewModel.getScore().getValue() : 0;
                    showGameOverDialog(finalScore);
                }
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewModel.getGameOver().getValue() != null && viewModel.getGameOver().getValue()) {
                    // if game over, reset then start
                    viewModel.resetGame();
                }
                viewModel.startGame();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.resetGame();
            }
        });
    }

    private void showGameOverDialog(int finalScore) {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("Final score: " + finalScore)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Release resources. Only stop the game when the Activity is really finishing
     * (so it doesnt pause the game on rotation).
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If the activity is finishing stop the game so controller's handler stops
        if (isFinishing()) {
            viewModel.stopGame();
        }

        // release soundManager to avoid leaks
        if (soundManager != null) {
            soundManager.release();
            soundManager = null;
        }
    }
}
