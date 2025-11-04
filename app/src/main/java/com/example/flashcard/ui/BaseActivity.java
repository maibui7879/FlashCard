package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton; // Import mới
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

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(getHeaderTitle());

        // Thêm nút back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // Quay về Activity trước đó

        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        LayoutInflater.from(this).inflate(getLayoutResourceId(), contentFrame, true);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navFlashcard = findViewById(R.id.navFlashcard);
        LinearLayout navUser = findViewById(R.id.navUser);

        // Chú ý: Cần đảm bảo MainActivity và SetListActivity đã tồn tại
        navHome.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        navFlashcard.setOnClickListener(v -> startActivity(new Intent(this, SetListActivity.class)));
        navUser.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
    }

    protected abstract String getHeaderTitle();
    protected abstract int getLayoutResourceId();
}