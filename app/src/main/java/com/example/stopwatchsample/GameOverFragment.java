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

public class GameOverFragment extends DialogFragment {

    private static final String ARG_SCORE = "score";

    public static GameOverFragment newInstance(int score) {
        GameOverFragment fragment = new GameOverFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SCORE, score);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gameover, container, false);

        TextView tvFinalScore = view.findViewById(R.id.tvFinalScore);
        Button btnRestart = view.findViewById(R.id.btnRestart);

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

    @Override
    public void onStart() {
        super.onStart();
        // make dialog a bit nicer
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public interface GameOverListener {
        void onRestartGame();
    }
}



