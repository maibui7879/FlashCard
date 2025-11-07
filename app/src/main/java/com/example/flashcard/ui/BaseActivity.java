package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.flashcard.R;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(getHeaderTitle());

        // Nút back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Content frame
        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        LayoutInflater.from(this).inflate(getLayoutResourceId(), contentFrame, true);

        // Nav
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navFlashcard = findViewById(R.id.navFlashcard);
        LinearLayout navUser = findViewById(R.id.navUser);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);
        });
        navFlashcard.setOnClickListener(v -> {
            startActivity(new Intent(this, SetListActivity.class));
            overridePendingTransition(0, 0);
        });
        navUser.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            overridePendingTransition(0, 0);
        });

        updateNavSelection(navHome, navFlashcard, navUser);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    protected abstract String getHeaderTitle();
    protected abstract int getLayoutResourceId();

    private void updateNavSelection(LinearLayout navHome, LinearLayout navFlashcard, LinearLayout navUser) {
        int smallSize = dpToPx(24);
        int largeSize = dpToPx(32);

        ImageView homeIcon = navHome.findViewById(R.id.nav_icon);
        ImageView flashcardIcon = navFlashcard.findViewById(R.id.nav_icon);
        ImageView userIcon = navUser.findViewById(R.id.nav_icon);

        // Reset tất cả icon về mặc định nhỏ và không background
        resetIcon(homeIcon, smallSize);
        resetIcon(flashcardIcon, smallSize);
        resetIcon(userIcon, smallSize);

        ImageView selectedIcon = null;
        String current = this.getClass().getSimpleName();
        if (current.equals("MainActivity")) selectedIcon = homeIcon;
        else if (current.equals("SetListActivity") || current.equals("SetActivity")) selectedIcon = flashcardIcon;
        else if (current.equals("DashboardActivity")) selectedIcon = userIcon;

        if (selectedIcon != null) {
            selectedIcon.getLayoutParams().width = largeSize;
            selectedIcon.getLayoutParams().height = largeSize;
            selectedIcon.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            selectedIcon.requestLayout();
        }
    }

    private void resetIcon(ImageView icon, int size) {
        icon.getLayoutParams().width = size;
        icon.getLayoutParams().height = size;
        icon.setBackground(null);
        icon.requestLayout();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
