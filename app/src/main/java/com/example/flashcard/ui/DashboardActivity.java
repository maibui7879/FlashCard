package com.example.flashcard.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.example.flashcard.R;
import com.example.flashcard.storage.StorageManager;

public class DashboardActivity extends BaseActivity {

    private TextView tvTotalSets, tvTotalCards, tvProgress;
    private StorageManager storageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // StorageManager
        storageManager = new StorageManager(this);

        // Ánh xạ các TextView trong layout dashboard
        tvTotalSets = findViewById(R.id.tvTotalSets);
        tvTotalCards = findViewById(R.id.tvTotalCards);
        tvProgress = findViewById(R.id.tvProgress);

        loadDashboardData();
    }

    @Override
    protected String getHeaderTitle() {
        return "Dashboard"; // Header trên cùng
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_dashboard; // Layout riêng của dashboard
    }

    private void loadDashboardData() {
        // Tổng số Set
        int totalSets = storageManager.getAllSets().size();
        tvTotalSets.setText(String.valueOf(totalSets));

        // Tổng số thẻ
        int totalCards = 0;
        for (int i = 0; i < totalSets; i++) {
            totalCards += storageManager.getAllCards(storageManager.getAllSets().get(i).getId()).size();
        }
        tvTotalCards.setText(String.valueOf(totalCards));

        // Tiến trình học tập tổng quát: tính trung bình % thẻ đã nhớ
        int rememberedCards = 0;
        for (int i = 0; i < totalSets; i++) {
            String setId = storageManager.getAllSets().get(i).getId();
            rememberedCards += storageManager.getRememberedCards(setId).size();
        }
        int progress = totalCards == 0 ? 0 : (rememberedCards * 100 / totalCards);
        tvProgress.setText(progress + "%");
    }
}
