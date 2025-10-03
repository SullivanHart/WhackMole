package com.example.stopwatchsample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * The pop-up that is shown after the game has ended.
 */
public class GameOverFragment extends DialogFragment {

    private static final String ARG_SCORE = "score";
    private static final String ARG_HIGHSCORE = "highscore";

    /**
     * Create a new pop-up to display end-of-game info.
     *
     * @param score The score of the game that just ended.
     * @param highscore The high score ( managed by view ).
     * @return The pop-up fragment object.
     */
    public static GameOverFragment newInstance( int score, int highscore ) {
        GameOverFragment fragment = new GameOverFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SCORE, score);
        args.putInt(ARG_HIGHSCORE, highscore);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Builds the pop-up's view object.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The pop-up view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gameover, container, false);

        TextView tvHighScore = view.findViewById(R.id.tvHighScore);
        TextView tvFinalScore = view.findViewById(R.id.tvFinalScore);
        Button btnRestart = view.findViewById(R.id.btnRestart);

        int highScore = getArguments() != null ? getArguments().getInt(ARG_HIGHSCORE) : 0;
        tvHighScore.setText("High Score: " + highScore);

        int score = getArguments() != null ? getArguments().getInt(ARG_SCORE) : 0;
        tvFinalScore.setText("Score: " + score);

        btnRestart.setOnClickListener(v -> {
            dismiss(); // close popup
            if (getActivity() instanceof GameOverListener) {
                ((GameOverListener) getActivity()).onRestartGame();
            }
        });

        return view;
    }

    /**
     * Inflates the view more ( otherwise the pop-up is too small ).
     */
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * Called when the game is over and restarts the game.
     */
    public interface GameOverListener {
        void onRestartGame();
    }
}



