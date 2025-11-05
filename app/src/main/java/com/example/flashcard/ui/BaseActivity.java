package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import com.example.flashcard.R;
import com.example.flashcard.MainActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected View headerBar;
    protected TextView headerTitle;
    protected ImageButton btnHeaderBack;
    protected FrameLayout headerRightContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        headerBar = findViewById(R.id.headerBar);
        headerTitle = findViewById(R.id.headerTitle);
        headerRightContainer = findViewById(R.id.headerRightContainer);
        btnHeaderBack = findViewById(R.id.btnHeaderBack);

        if (!showHeader()) {
            headerBar.setVisibility(View.GONE);
        } else {
            headerBar.setVisibility(View.VISIBLE);

            CharSequence title = getHeaderTitle();
            if (title != null) headerTitle.setText(title);

            btnHeaderBack.setVisibility(showHeaderBackButton() ? View.VISIBLE : View.GONE);
            btnHeaderBack.setOnClickListener(v -> onBackPressed());

            int customRight = getHeaderRightLayoutId();
            if (customRight != 0 && headerRightContainer != null) {
                headerRightContainer.removeAllViews();
                LayoutInflater.from(this).inflate(customRight, headerRightContainer, true);
                onBindHeaderRight(headerRightContainer);
            }

            onSetupHeader(headerBar);
        }

        FrameLayout contentFrame = findViewById(R.id.contentFrame);
        LayoutInflater.from(this).inflate(getLayoutResourceId(), contentFrame, true);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navFlashcard = findViewById(R.id.navFlashcard);
        LinearLayout navUser = findViewById(R.id.navUser);

        navHome.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        navFlashcard.setOnClickListener(v -> startActivity(new Intent(this, SetListActivity.class)));
        navUser.setOnClickListener(v -> startActivity(new Intent(this, SetListActivity.class)));
    }

    protected boolean showHeader() { return true; }

    protected boolean showHeaderBackButton() { return false; }

    protected CharSequence getHeaderTitle() { return getTitle(); }

    protected int getHeaderRightLayoutId() { return 0; }

    protected void onBindHeaderRight(@NonNull ViewGroup rightContainer) { /* optional */ }

    protected void onSetupHeader(@NonNull View header) { /* optional */ }

    protected abstract int getLayoutResourceId();
}

