package com.example.flashcard.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.example.flashcard.R;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.models.QuizResult;
import com.example.flashcard.storage.QuizRepository;
import com.example.flashcard.storage.StorageManager;

import java.util.List;

public class DashboardActivity extends BaseActivity {

    private TextView tvTotalSets, tvTotalCards, tvProgress, tvTotalQuizzes, tvQuizAccuracy;
    private StorageManager storageManager;
    private QuizRepository quizRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storageManager = new StorageManager(this);
        quizRepository = new QuizRepository(this);

        tvTotalSets = findViewById(R.id.tvTotalSets);
        tvTotalCards = findViewById(R.id.tvTotalCards);
        tvProgress = findViewById(R.id.tvProgress);
        tvTotalQuizzes = findViewById(R.id.tvTotalQuizzes);
        tvQuizAccuracy = findViewById(R.id.tvQuizAccuracy);

        loadDashboardData();
    }

    @Override
    protected String getHeaderTitle() {
        return "Dashboard";
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_dashboard;
    }

    private void loadDashboardData() {
        List<FlashcardSet> allSets = storageManager.getAllSets();

        int totalSets = allSets.size();
        int totalCards = 0;
        int rememberedCards = 0;
        int totalQuizzes = 0;
        int totalCorrectAnswers = 0;
        int totalQuestions = 0;

        for (FlashcardSet set : allSets) {
            String setId = set.getId();

            totalCards += storageManager.getAllCards(setId).size();
            rememberedCards += storageManager.getRememberedCards(setId).size();

            List<QuizResult> quizResults = quizRepository.getResults(setId);
            totalQuizzes += quizResults.size();

            for (QuizResult qr : quizResults) {
                totalCorrectAnswers += qr.getCorrectAnswers();
                totalQuestions += qr.getTotalQuestions();
            }
        }

        tvTotalSets.setText(String.valueOf(totalSets));
        tvTotalCards.setText(String.valueOf(totalCards));

        int progress = totalCards == 0 ? 0 : (rememberedCards * 100 / totalCards);
        tvProgress.setText(progress + "%");

        tvTotalQuizzes.setText(String.valueOf(totalQuizzes));

        int quizAccuracy = totalQuestions == 0 ? 0 : (totalCorrectAnswers * 100 / totalQuestions);
        tvQuizAccuracy.setText(quizAccuracy + "%");
    }
}
