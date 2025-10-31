package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.flashcard.R;

public class QuizResultActivity extends BaseActivity {

    private String setId, setName;
    private int correct, total;

    @Override protected String getHeaderTitle() { return "Kết quả"; }
    @Override protected int getLayoutResourceId() { return R.layout.activity_quiz_result; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setId   = getIntent().getStringExtra("SET_ID");
        setName = getIntent().getStringExtra("SET_NAME");
        correct = getIntent().getIntExtra("CORRECT", 0);
        total   = getIntent().getIntExtra("TOTAL", 0);

        TextView tvTitle   = findViewById(R.id.tvResultTitle);
        TextView tvScore   = findViewById(R.id.tvScore);
        TextView tvPercent = findViewById(R.id.tvPercent);
        Button btnRetry    = findViewById(R.id.btnRetry);
        Button btnBack     = findViewById(R.id.btnBack);

        tvTitle.setText("Bộ: " + (setName == null ? "" : setName));
        tvScore.setText(correct + " / " + total);
        int percent = total == 0 ? 0 : Math.round((correct * 100f) / total);
        tvPercent.setText(percent + "%");

        btnRetry.setOnClickListener(v -> {
            Intent i = new Intent(this, QuizActivity.class);
            i.putExtra("SET_ID", setId);
            startActivity(i);
            finish();
        });
        btnBack.setOnClickListener(v -> finish());
    }
}
