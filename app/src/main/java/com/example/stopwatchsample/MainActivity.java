package com.example.stopwatchsample;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnStopwatch, btnWhackmole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnWhackmole = findViewById(R.id.btnWhackmole);
        btnStopwatch = findViewById(R.id.btnStopwatch);

        // Set up button listener for whackmole
        btnWhackmole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WhackmoleActivity.class);
                startActivity(intent);
            }
        });

        // Set up button listener for stopwatch
        btnStopwatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StopwatchActivity.class);
                startActivity(intent);
            }
        });
    }

}
