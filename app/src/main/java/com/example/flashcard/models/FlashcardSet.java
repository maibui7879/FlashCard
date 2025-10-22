package com.example.flashcard.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FlashcardSet implements Serializable {
    private String id;
    private String name;
    private List<Flashcard> flashcards;
    private List<QuizResult> quizResults;
    private long createdAt;
    private long updatedAt;

    public FlashcardSet(String id, String name) {
        this.id = id;
        this.name = name;
        this.flashcards = new ArrayList<>();
        this.quizResults = new ArrayList<>();
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Flashcard> getFlashcards() { return flashcards; }
    public List<QuizResult> getQuizResults() { return quizResults; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setFlashcards(List<Flashcard> flashcards) { this.flashcards = flashcards; updateTimestamp(); }
    public void setQuizResults(List<QuizResult> quizResults) { this.quizResults = quizResults; updateTimestamp(); }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
}
