package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.adapters.MainSetAdapter;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.storage.StorageManager;

import java.util.List;

public class MainActivity extends BaseActivity implements MainSetAdapter.OnItemClickListener {

    private TextView tvTotalSetsMain;
    private StorageManager storageManager;
    private Button btnGoSetList;
    private RecyclerView rvLearnSets;
    private RecyclerView rvQuizSets;

    private TextView tvEmptyState;

    private List<FlashcardSet> allSets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storageManager = new StorageManager(this);

        tvTotalSetsMain = findViewById(R.id.tvTotalSetsMain);
        btnGoSetList = findViewById(R.id.btnGoSetList);
        rvLearnSets = findViewById(R.id.rvLearnSets);
        rvQuizSets = findViewById(R.id.rvQuizSets);
        // Ánh xạ TextView trống mới
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Thiết lập RecyclerViews
        rvLearnSets.setLayoutManager(new LinearLayoutManager(this));
        rvQuizSets.setLayoutManager(new LinearLayoutManager(this));

        // Sự kiện chuyển đến SetListActivity
        btnGoSetList.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SetListActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu mỗi khi quay lại màn hình chính
        loadData();
    }

    private void loadData() {
        allSets = storageManager.getAllSets();

        if (allSets.isEmpty()) {
            // Trường hợp không có bộ thẻ nào
            handleEmptyState();
        } else {
            // Trường hợp có bộ thẻ
            handleDataLoaded();
        }
    }

    private void handleEmptyState() {
        // Ẩn RecyclerViews
        rvLearnSets.setVisibility(View.GONE);
        rvQuizSets.setVisibility(View.GONE);

        // Hiển thị TextView thông báo trống
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText(R.string.msg_no_sets_created); // Tham chiếu đến String resource
        }

        // Cập nhật TextView tổng số
        tvTotalSetsMain.setText("0");
    }

    private void handleDataLoaded() {
        // Hiển thị RecyclerViews
        rvLearnSets.setVisibility(View.VISIBLE);
        rvQuizSets.setVisibility(View.VISIBLE);

        // Ẩn TextView trống
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }

        // 1. Cập nhật tổng số Set
        loadTotalSets();

        // 2. Thiết lập Adapter cho Học
        MainSetAdapter learnAdapter = new MainSetAdapter(this, allSets, "LEARN", this);
        rvLearnSets.setAdapter(learnAdapter);

        // 3. Thiết lập Adapter cho Quiz
        MainSetAdapter quizAdapter = new MainSetAdapter(this, allSets, "QUIZ", this);
        rvQuizSets.setAdapter(quizAdapter);
    }


    private void loadTotalSets() {
        int totalSets = allSets.size();
        tvTotalSetsMain.setText(String.valueOf(totalSets));
    }

    @Override
    protected String getHeaderTitle() {
        return "FlashCard - Thẻ nhớ Tiếng Anh";
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    /**
     * Xử lý sự kiện khi nhấn vào một Set trong danh sách Học hoặc Quiz
     */
    @Override
    public void onItemClick(FlashcardSet set, String mode) {
        if (set.getFlashcards() == null || set.getFlashcards().isEmpty()) {
            Toast.makeText(this, "Bộ thẻ trống, vui lòng thêm thẻ!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;
        if (mode.equals("LEARN")) {
            // Chuyển sang StudyActivity
            intent = new Intent(MainActivity.this, StudyActivity.class);
            intent.putExtra("SET_ID_TO_STUDY", set.getId());
            startActivity(intent);
        } else if (mode.equals("QUIZ")) {
            // Chuyển sang QuizActivity
            intent = new Intent(MainActivity.this, QuizActivity.class);
            intent.putExtra("SET_ID", set.getId());
            intent.putExtra("SET_NAME", set.getName());
            startActivity(intent);
        }
    }
}