package com.example.flashcard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flashcard.R;
import com.example.flashcard.models.Flashcard;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.models.QuizResult;
import com.example.flashcard.storage.QuizRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuizActivity extends BaseActivity {

    private QuizRepository repo;
    private String setId, setName;

    private TextView tvSetName, tvProgress, tvQuestion;
    private Button[] optionButtons = new Button[6];
    private Button btnNext;

    private List<Flashcard> questions = new ArrayList<>();
    private int qIndex = 0, score = 0;
    private String currentCorrect;

    @Override protected String getHeaderTitle() { return "Quiz"; }
    @Override protected int getLayoutResourceId() { return R.layout.activity_quiz; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repo = new QuizRepository(this);
        setId = getIntent().getStringExtra("SET_ID");
        if (setId == null) { Toast.makeText(this, "Thiếu SET_ID", Toast.LENGTH_SHORT).show(); finish(); return; }

        FlashcardSet set = repo.getSetById(setId);
        if (set == null) { Toast.makeText(this, "Không tìm thấy bộ thẻ", Toast.LENGTH_SHORT).show(); finish(); return; }
        setName = set.getName();

        questions = new ArrayList<>(set.getFlashcards() != null ? set.getFlashcards() : new ArrayList<>());
        if (questions.isEmpty()) { Toast.makeText(this, "Bộ thẻ đang trống", Toast.LENGTH_SHORT).show(); finish(); return; }
        Collections.shuffle(questions);

        bindViews();
        renderQuestion();
    }

    private void bindViews() {
        tvSetName  = findViewById(R.id.tvSetName);
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        optionButtons[0] = findViewById(R.id.btnOption1);
        optionButtons[1] = findViewById(R.id.btnOption2);
        optionButtons[2] = findViewById(R.id.btnOption3);
        optionButtons[3] = findViewById(R.id.btnOption4);
        optionButtons[4] = findViewById(R.id.btnOption5);
        optionButtons[5] = findViewById(R.id.btnOption6);
        btnNext = findViewById(R.id.btnNext);

        tvSetName.setText(setName);
        for (Button b : optionButtons) b.setOnClickListener(this::onOptionClicked);

        btnNext.setOnClickListener(v -> {
            qIndex++;
            if (qIndex < questions.size()) renderQuestion();
            else saveAndFinish();
        });
    }

    private void renderQuestion() {
        enableOptions(true);
        btnNext.setEnabled(false);

        Flashcard card = questions.get(qIndex);
        tvQuestion.setText(card.getName());            // Câu hỏi: dùng name
        tvProgress.setText((qIndex + 1) + "/" + questions.size());
        currentCorrect = card.getMeaning();            // Đáp án đúng: meaning

        List<String> options = buildOptions(card, questions, 6);

        for (int i = 0; i < optionButtons.length; i++) {
            if (i < options.size()) {
                optionButtons[i].setVisibility(View.VISIBLE);
                optionButtons[i].setText(options.get(i));
            } else optionButtons[i].setVisibility(View.GONE);
        }
    }

    private void onOptionClicked(View v) {
        String chosen = ((Button) v).getText().toString();
        if (chosen.equalsIgnoreCase(currentCorrect)) score++;
        enableOptions(false);
        btnNext.setEnabled(true);
    }

    private void enableOptions(boolean enable) { for (Button b : optionButtons) b.setEnabled(enable); }

    /** Sinh tối đa `max` đáp án (1 đúng + max-1 sai), không trùng, shuffle */
    private List<String> buildOptions(Flashcard target, List<Flashcard> pool, int max) {
        String correct = target.getMeaning();
        Set<String> opts = new HashSet<>();

        // Sai từ answers của chính card
        if (target.getAnswers() != null) {
            for (String a : target.getAnswers()) {
                if (a != null && !a.equalsIgnoreCase(correct)) opts.add(a.trim());
            }
        }
        // Sai từ meaning của các card khác
        for (Flashcard f : pool) {
            if (opts.size() >= max - 1) break;
            if (f == target) continue;
            String m = f.getMeaning();
            if (m != null && !m.equalsIgnoreCase(correct)) opts.add(m.trim());
        }
        // Fallback: lấy name của các card khác nếu vẫn thiếu
        for (Flashcard f : pool) {
            if (opts.size() >= max - 1) break;
            if (f == target) continue;
            String n = f.getName();
            if (n != null && !n.equalsIgnoreCase(correct)) opts.add(n.trim());
        }

        List<String> result = new ArrayList<>(opts);
        result.add(correct);
        Collections.shuffle(result);

        if (result.size() > max) result = result.subList(0, max);
        if (!result.contains(correct)) { result.set(0, correct); Collections.shuffle(result); }
        if (result.size() < 2) { result.add("Không có đáp án khác"); Collections.shuffle(result); }
        return result;
    }

    private void saveAndFinish() {
        repo.saveQuizResult(setId,
                new QuizResult(setId, setName, score, questions.size(), System.currentTimeMillis()));

        Intent i = new Intent(this, QuizResultActivity.class);
        i.putExtra("SET_ID", setId);
        i.putExtra("SET_NAME", setName);
        i.putExtra("CORRECT", score);
        i.putExtra("TOTAL", questions.size());
        startActivity(i);
        finish();
    }
}
