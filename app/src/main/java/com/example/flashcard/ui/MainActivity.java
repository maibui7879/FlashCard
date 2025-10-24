package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flashcard.R;

public class MainActivity extends AppCompatActivity {

    private Button button; // khai báo biến button ở đây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ánh xạ ID button
        button = findViewById(R.id.button);

        // bắt sự kiện click
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StudyActivity.class);
            startActivity(intent);
        });
    }
}

