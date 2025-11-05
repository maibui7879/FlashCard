package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import com.example.flashcard.R;
import com.example.flashcard.models.QuizResult;
import com.example.flashcard.storage.QuizRepository;

import java.util.List;

public class QuizResultActivity extends BaseActivity {

    private String setId, setName;
    private int correct, total;

    @Override
    protected String getHeaderTitle() {
        return "Kết quả";
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_quiz_result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setId   = getIntent().getStringExtra("SET_ID");
        setName = getIntent().getStringExtra("SET_NAME");
        correct = getIntent().getIntExtra("CORRECT", 0);
        total   = getIntent().getIntExtra("TOTAL", 0);

        TextView tvTitle   = findViewById(R.id.tvResultTitle);
        TextView tvScore   = findViewById(R.id.tvScore);
        TextView tvRecord  = findViewById(R.id.tvRecord);
        ProgressBar progressBar = findViewById(R.id.progressPercent);
        MaterialButton btnRetry = findViewById(R.id.btnRetryQuiz);
        MaterialButton btnBack  = findViewById(R.id.btnBackResult);

        tvTitle.setText("Bộ: " + (setName == null ? "" : setName));
        tvScore.setText(correct + " / " + total);

        int percent = total == 0 ? 0 : Math.round((correct * 100f) / total);
        progressBar.setProgress(percent);

        // Lấy điểm kỷ lục của bộ
        QuizRepository repo = new QuizRepository(this);
        List<QuizResult> results = repo.getResults(setId);
        int maxScore = 0;
        for (QuizResult r : results) {
            if (r.getCorrectAnswers() > maxScore) maxScore = r.getCorrectAnswers();
        }
        tvRecord.setText("Kỷ lục: " + maxScore + " / " + total);

        btnRetry.setOnClickListener(v -> {
            Intent i = new Intent(this, QuizActivity.class);
            i.putExtra("SET_ID", setId);
            startActivity(i);
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
