package com.example.flashcard.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.flashcard.R;
import com.example.flashcard.models.Flashcard;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.models.QuizResult;
import com.example.flashcard.storage.QuizRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuizActivity extends BaseActivity {

    private static final int MAX_CHOICES = 4;

    private QuizRepository repo;
    private String setId, setName;

    private TextView tvSetName, tvProgress, tvQuestion;
    // Sửa: Sử dụng MaterialButton thay vì Button
    private MaterialButton[] optionButtons = new MaterialButton[4];
    private MaterialButton btnNext;
    private ProgressBar progressBar;

    private List<Flashcard> questions = new ArrayList<>();
    private int qIndex = 0, score = 0;
    private String currentCorrect;

    // Sửa: Thêm các biến màu để quản lý dễ dàng hơn
    private ColorStateList correctColor, wrongColor, neutralColor;

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
        setupColors();
        renderQuestion();
    }

    private void bindViews() {
        tvSetName  = findViewById(R.id.tvSetName);
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        progressBar = findViewById(R.id.progressBar);

        optionButtons[0] = findViewById(R.id.btnOption1);
        optionButtons[1] = findViewById(R.id.btnOption2);
        optionButtons[2] = findViewById(R.id.btnOption3);
        optionButtons[3] = findViewById(R.id.btnOption4);

        btnNext = findViewById(R.id.btnNext);

        tvSetName.setText(setName);
        for (MaterialButton b : optionButtons) {
            if (b != null) b.setOnClickListener(this::onOptionClicked);
        }

        btnNext.setOnClickListener(v -> {
            qIndex++;
            if (qIndex < questions.size()) renderQuestion();
            else saveAndFinish();
        });
    }

    // Sửa: Phương thức mới để khởi tạo màu sắc
    private void setupColors() {
        correctColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_correct));
        wrongColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_wrong));
        neutralColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white));
    }

    private void renderQuestion() {
        enableOptions(true);
        btnNext.setEnabled(false);

        Flashcard card = questions.get(qIndex);
        tvQuestion.setText(card.getName());
        tvProgress.setText((qIndex + 1) + "/" + questions.size());

        int progress = (int) (((float)(qIndex + 1) / questions.size()) * 100);
        progressBar.setProgress(progress);

        currentCorrect = card.getMeaning();
        List<String> options = buildOptions(card, questions, MAX_CHOICES);

        for (int i = 0; i < optionButtons.length; i++) {
            MaterialButton b = optionButtons[i];
            if (b == null) continue;
            if (i < options.size()) {
                b.setVisibility(View.VISIBLE);
                b.setText(options.get(i));
                // Reset màu nền và màu chữ
                b.setBackgroundTintList(neutralColor);
                b.setTextColor(ContextCompat.getColor(this, R.color.blue_primary));
            } else {
                b.setVisibility(View.GONE);
            }
        }
    }

    private void onOptionClicked(View v) {
        MaterialButton chosenButton = (MaterialButton) v;
        String chosen = chosenButton.getText().toString();

        if (chosen.equalsIgnoreCase(currentCorrect)) {
            score++;
            chosenButton.setBackgroundTintList(correctColor);
            chosenButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            chosenButton.setBackgroundTintList(wrongColor);
            chosenButton.setTextColor(ContextCompat.getColor(this, R.color.white));
            // Tô đáp án đúng
            for (MaterialButton b : optionButtons) {
                if (b != null && b.getVisibility() == View.VISIBLE &&
                        b.getText().toString().equalsIgnoreCase(currentCorrect)) {
                    b.setBackgroundTintList(correctColor);
                    b.setTextColor(ContextCompat.getColor(this, R.color.white));
                    break;
                }
            }
        }

        enableOptions(false);
        btnNext.setEnabled(true);
    }

    private void enableOptions(boolean enable) {
        for (MaterialButton b : optionButtons) {
            if (b != null) b.setEnabled(enable);
        }
    }

    private List<String> buildOptions(Flashcard target, List<Flashcard> pool, int max) {
        String correct = target.getMeaning();
        Set<String> wrongs = new HashSet<>();

        if (target.getAnswers() != null) {
            for (String a : target.getAnswers()) {
                if (a == null) continue;
                String val = a.trim();
                if (!val.isEmpty() && !val.equalsIgnoreCase(correct)) wrongs.add(val);
                if (wrongs.size() >= max - 1) break;
            }
        }

        if (wrongs.size() < max - 1) {
            for (Flashcard f : pool) {
                if (f == target) continue;
                String m = f.getMeaning();
                if (m != null && !m.isEmpty() && !m.equalsIgnoreCase(correct)) {
                    wrongs.add(m.trim());
                    if (wrongs.size() >= max - 1) break;
                }
            }
        }

        if (wrongs.size() < max - 1) {
            for (Flashcard f : pool) {
                if (f == target) continue;
                String n = f.getName();
                if (n != null && !n.isEmpty() && !n.equalsIgnoreCase(correct)) {
                    wrongs.add(n.trim());
                    if (wrongs.size() >= max - 1) break;
                }
            }
        }

        List<String> result = new ArrayList<>(wrongs);
        if (result.size() > max - 1) {
            result = new ArrayList<>(result.subList(0, max - 1));
        }
        result.add(correct);
        Collections.shuffle(result);

        // Đảm bảo các tùy chọn luôn đúng số lượng và không có lỗi
        if (result.size() < 2 && !result.contains(correct)) {
            result.add("Không có đáp án khác");
        }
        while (result.size() < max && result.size() < pool.size()) {
            // Thêm một tùy chọn ngẫu nhiên khác nếu cần
            Flashcard randomCard = pool.get(new java.util.Random().nextInt(pool.size()));
            String randomOption = randomCard.getMeaning();
            if (!result.contains(randomOption)) {
                result.add(randomOption);
            }
        }

        return result.subList(0, Math.min(result.size(), max));
    }

    private void saveAndFinish() {
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
