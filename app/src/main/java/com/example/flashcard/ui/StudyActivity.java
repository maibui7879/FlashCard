package com.example.flashcard.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.flashcard.R;
import com.example.flashcard.models.Flashcard;
import com.example.flashcard.utils.FlashcardData;
import com.example.flashcard.utils.SharedPrefManager;
import java.util.List;

public class StudyActivity extends AppCompatActivity {

    private TextView tvFront, tvBack;
    private Button btnFlip, btnNext;
    private boolean isFrontVisible = true;
    private int currentIndex = 0;
    private List<Flashcard> flashcards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        tvFront = findViewById(R.id.tvFront);
        tvBack = findViewById(R.id.tvBack);
        btnFlip = findViewById(R.id.btnFlip);
        btnNext = findViewById(R.id.btnNext);

        // Lấy dữ liệu mẫu
        flashcards = FlashcardData.getSampleData();

        loadFlashcard(currentIndex);

        btnFlip.setOnClickListener(v -> flipCard());
        btnNext.setOnClickListener(v -> nextFlashcard());
    }

    private void loadFlashcard(int index) {
        Flashcard fc = flashcards.get(index);
        tvFront.setText(fc.getName());
        tvBack.setText(fc.getMeaning() + " (" + fc.getType() + ")");
        tvFront.setVisibility(View.VISIBLE);
        tvBack.setVisibility(View.GONE);
        isFrontVisible = true;
    }

    private void flipCard() {
        if (isFrontVisible) {
            tvFront.setVisibility(View.GONE);
            tvBack.setVisibility(View.VISIBLE);
        } else {
            tvFront.setVisibility(View.VISIBLE);
            tvBack.setVisibility(View.GONE);
        }
        isFrontVisible = !isFrontVisible;
    }

    private void nextFlashcard() {
        currentIndex = (currentIndex + 1) % flashcards.size();
        loadFlashcard(currentIndex);

        // Lưu tiến trình học vào SharedPref
        SharedPrefManager.saveStudyProgress(this, currentIndex);
    }
}

