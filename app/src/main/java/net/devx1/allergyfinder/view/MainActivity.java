package net.devx1.allergyfinder.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import net.devx1.allergyfinder.R;

public class MainActivity extends AppCompatActivity {
    Button btnMyAllergies, btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMyAllergies = findViewById(R.id.btnMyAllergies);
        btnScan = findViewById(R.id.btnScan);

        btnMyAllergies.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                startActivity(
                    new Intent(
                        MainActivity.this, MyAllergiesActivity.class
                    )
                );
                }
            }
        );

        btnScan.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(
                        new Intent(
                            MainActivity.this, AllergyScanActivity.class
                        )
                    );
                }
            }
        );
    }
}
