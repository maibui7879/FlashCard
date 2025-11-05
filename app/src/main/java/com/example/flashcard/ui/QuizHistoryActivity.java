package com.example.flashcard.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.flashcard.R;
import com.example.flashcard.models.QuizResult;
import com.example.flashcard.storage.QuizRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QuizHistoryActivity extends BaseActivity {

    private String setId, setName;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static class HistoryItem {
        boolean isHeader;
        String text;
        QuizResult result;

        HistoryItem(String dateText) {
            this.isHeader = true;
            this.text = dateText;
        }

        HistoryItem(QuizResult result) {
            this.isHeader = false;
            this.result = result;
            int correct = result.getCorrectAnswers();
            int total = result.getTotalQuestions();
            int percent = total == 0 ? 0 : Math.round((correct * 100f) / total);
            String time = TIME_FORMAT.format(new Date(result.getTimestamp()));
            this.text = time + " • " + correct + "/" + total + " (" + percent + "%)";
        }
    }

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

        Collections.sort(results, (r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));

        Map<String, List<QuizResult>> groupedResults = groupResultsByDate(results);

        List<HistoryItem> historyItems = new ArrayList<>();
        for (Map.Entry<String, List<QuizResult>> entry : groupedResults.entrySet()) {
            historyItems.add(new HistoryItem(entry.getKey()));
            for (QuizResult result : entry.getValue()) {
                historyItems.add(new HistoryItem(result));
            }
        }

        list.setAdapter(new HistoryAdapter(this, historyItems));
        list.setEmptyView(empty);
    }

    private Map<String, List<QuizResult>> groupResultsByDate(List<QuizResult> results) {
        Map<String, List<QuizResult>> grouped = new LinkedHashMap<>();
        for (QuizResult result : results) {
            String dateKey = DATE_FORMAT.format(new Date(result.getTimestamp()));
            grouped.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(result);
        }
        return grouped;
    }

    private class HistoryAdapter extends BaseAdapter {
        private final Context context;
        private final List<HistoryItem> items;
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_RESULT = 1;

        public HistoryAdapter(Context context, List<HistoryItem> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() { return items.size(); }

        @Override
        public Object getItem(int position) { return items.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).isHeader ? TYPE_HEADER : TYPE_RESULT;
        }

        @Override
        public int getViewTypeCount() { return 2; }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) == TYPE_RESULT;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HistoryItem item = items.get(position);
            int viewType = getItemViewType(position);

            if (viewType == TYPE_HEADER) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(context)
                            .inflate(android.R.layout.simple_list_item_1, parent, false);
                    TextView tv = convertView.findViewById(android.R.id.text1);
                    tv.setTextSize(16);
                    tv.setTextColor(getResources().getColor(R.color.blue_primary));
                    tv.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(item.text);
            } else {
                if (convertView == null) {
                    convertView = LayoutInflater.from(context)
                            .inflate(R.layout.item_history, parent, false);
                }
                TextView tvDate = convertView.findViewById(R.id.tvItemDate);

                int correct = item.result.getCorrectAnswers();
                int total = item.result.getTotalQuestions();
                int percent = total == 0 ? 0 : Math.round((correct * 100f) / total);
                String time = TIME_FORMAT.format(new Date(item.result.getTimestamp()));
                tvDate.setText(time + " • " + correct + "/" + total + " (" + percent + "%)");
            }

            return convertView;
        }
    }
}
