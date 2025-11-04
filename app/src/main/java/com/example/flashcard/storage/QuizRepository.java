package com.example.flashcard.storage;

import android.content.Context;

import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.models.QuizResult;

import java.util.ArrayList;
import java.util.List;

public class QuizRepository {
    private final StorageManager storage;

    public QuizRepository(Context context) {
        this.storage = new StorageManager(context);
    }

    public FlashcardSet getSetById(String setId) {
        List<FlashcardSet> sets = storage.getAllSets();
        for (FlashcardSet s : sets) {
            if (s.getId().equals(setId)) return s;
        }
        return null;
    }

    public void saveQuizResult(String setId, QuizResult result) {
        List<FlashcardSet> sets = storage.getAllSets();
        for (FlashcardSet s : sets) {
            if (s.getId().equals(setId)) {
                List<QuizResult> list = s.getQuizResults();
                if (list == null) list = new ArrayList<>();
                list.add(result);
                s.setQuizResults(list);
                break;
            }
        }
        storage.saveAllSets(sets);
    }

    public List<QuizResult> getResults(String setId) {
        FlashcardSet s = getSetById(setId);
        return (s != null && s.getQuizResults() != null) ? s.getQuizResults() : new ArrayList<>();
    }
}