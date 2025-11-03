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

    private static final int MAX_CHOICES = 4; // 1 đúng + 3 sai

    private QuizRepository repo;
    private String setId, setName;

    private TextView tvSetName, tvProgress, tvQuestion;
    private Button[] optionButtons = new Button[6]; // 2 nút dư sẽ ẩn
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
        for (Button b : optionButtons) if (b != null) b.setOnClickListener(this::onOptionClicked);

        btnNext.setOnClickListener(v -> {
            qIndex++;
            if (qIndex < questions.size()) renderQuestion();
            else saveAndFinish();
        });
    }

    private void renderQuestion() {
        enableOptions(true);
        btnNext.setEnabled(false);

        for (Button b : optionButtons) if (b != null) b.setBackgroundColor(0xFFFFFFFF);

        Flashcard card = questions.get(qIndex);
        tvQuestion.setText(card.getName());
        tvProgress.setText((qIndex + 1) + "/" + questions.size());
        currentCorrect = card.getMeaning();

        List<String> options = buildOptions(card, questions, MAX_CHOICES);

        for (int i = 0; i < optionButtons.length; i++) {
            Button b = optionButtons[i];
            if (b == null) continue;
            if (i < options.size()) {
                b.setVisibility(View.VISIBLE);
                b.setText(options.get(i));
            } else {
                b.setVisibility(View.GONE);
            }
        }
    }

    private void onOptionClicked(View v) {
        String chosen = ((Button) v).getText().toString();
        if (chosen.equalsIgnoreCase(currentCorrect)) {
            score++;
            v.setBackgroundColor(0xFF4CAF50); // đúng
        } else {
            v.setBackgroundColor(0xFFE53935); // sai
            for (Button b : optionButtons) {
                if (b != null && b.getVisibility() == View.VISIBLE &&
                        b.getText().toString().equalsIgnoreCase(currentCorrect)) {
                    b.setBackgroundColor(0xFF4CAF50);
                    break;
                }
            }
        }
        enableOptions(false);
        btnNext.setEnabled(true);
    }

    private void enableOptions(boolean enable) {
        for (Button b : optionButtons) if (b != null) b.setEnabled(enable);
    }

    private List<String> buildOptions(Flashcard target, List<Flashcard> pool, int max) {
        String correct = target.getMeaning();
        Set<String> wrongs = new HashSet<>();

        if (target.getAnswers() != null) {
            for (String a : target.getAnswers()) {
                if (a == null) continue;
                String val = a.trim();
                if (!val.equalsIgnoreCase(correct)) wrongs.add(val);
                if (wrongs.size() >= max - 1) break;
            }
        }

        if (wrongs.size() < max - 1) {
            for (Flashcard f : pool) {
                if (f == target) continue;
                String m = f.getMeaning();
                if (m != null && !m.equalsIgnoreCase(correct)) {
                    wrongs.add(m.trim());
                    if (wrongs.size() >= max - 1) break;
                }
            }
        }

        if (wrongs.size() < max - 1) {
            for (Flashcard f : pool) {
                if (f == target) continue;
                String n = f.getName();
                if (n != null && !n.equalsIgnoreCase(correct)) {
                    wrongs.add(n.trim());
                    if (wrongs.size() >= max - 1) break;
                }
            }
        }

        List<String> result = new ArrayList<>(wrongs);
        result.add(correct);
        Collections.shuffle(result);

        if (result.size() > max) result = result.subList(0, max);
        if (!result.contains(correct)) { result.set(0, correct); Collections.shuffle(result); }
        if (result.size() < 2) { result.add("Không có đáp án khác"); Collections.shuffle(result); }
        return result;
    }

    private void saveAndFinish() {
        // Lưu đúng với QuizResult hiện có (setId, totalQuestions, correctAnswers)
        repo.saveQuizResult(setId, new QuizResult(setId, questions.size(), score));

        Intent i = new Intent(this, QuizResultActivity.class);
        i.putExtra("SET_ID", setId);
        i.putExtra("SET_NAME", setName);
        i.putExtra("CORRECT", score);
        i.putExtra("TOTAL", questions.size());
        startActivity(i);
        finish();
    }
}
