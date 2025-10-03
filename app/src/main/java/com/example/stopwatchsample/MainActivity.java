package com.example.stopwatchsample;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

/**
 * The landing screen ( main ) activity.
 */
public class MainActivity extends AppCompatActivity {

    private Button btnStopwatch, btnWhackmole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnWhackmole = findViewById(R.id.btnWhackmole);

        // Set up button listener for whackmole
        btnWhackmole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WhackmoleActivity.class);
                startActivity(intent);
            }
        });
    }

}
