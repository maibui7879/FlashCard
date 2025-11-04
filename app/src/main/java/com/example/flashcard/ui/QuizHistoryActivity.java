package com.example.flashcard.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.flashcard.R;
import com.example.flashcard.models.QuizResult;
import com.example.flashcard.storage.QuizRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class QuizHistoryActivity extends BaseActivity {

    private String setId, setName;

    @Override protected String getHeaderTitle() { return "Lịch sử Quiz"; }
    @Override protected int getLayoutResourceId() { return R.layout.activity_quiz_history; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setId   = getIntent().getStringExtra("SET_ID");
        setName = getIntent().getStringExtra("SET_NAME");

        TextView tvHeader = findViewById(R.id.tvHistoryTitle);
        tvHeader.setText(setName == null ? "Lịch sử Quiz" : "Bộ: " + setName);

        ListView list = findViewById(R.id.lvHistory);
        TextView empty = findViewById(R.id.tvEmpty);

        QuizRepository repo = new QuizRepository(this);
        List<QuizResult> results = repo.getResults(setId);

        if (results == null || results.isEmpty()) {
            list.setEmptyView(empty);
            return;
        }

        List<QuizResult> copy = new ArrayList<>(results);
        Collections.reverse(copy); // gần nhất lên đầu

        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        List<String> lines = new ArrayList<>();
        for (QuizResult r : copy) {
            String time = fmt.format(new Date(r.getTimestamp()));
            int correct = r.getCorrectAnswers();
            int total   = r.getTotalQuestions();
            int percent = total == 0 ? 0 : Math.round((correct * 100f) / total);
            lines.add(time + " • " + correct + "/" + total + " (" + percent + "%)");
        }

        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lines));
    }
}