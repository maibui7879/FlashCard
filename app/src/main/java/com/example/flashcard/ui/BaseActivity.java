package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.example.flashcard.R;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Set header title
        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(getHeaderTitle());

        // Inflate content của Activity con vào FrameLayout
        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        LayoutInflater.from(this).inflate(getLayoutResourceId(), contentFrame, true);

        // Nav buttons
        LinearLayout navFlashcard = findViewById(R.id.navFlashcard);
        LinearLayout navUser = findViewById(R.id.navUser);

        // Xử lý click
        navFlashcard.setOnClickListener(v -> startActivity(new Intent(this, SetListActivity.class)));
        navUser.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));

        // navHome đã bị bỏ
    }

    protected abstract String getHeaderTitle();
    protected abstract int getLayoutResourceId();
}
