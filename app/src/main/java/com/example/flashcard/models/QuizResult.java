package com.example.flashcard.models;

import java.io.Serializable;
//kết quả của test
public class QuizResult implements Serializable {
    private String setId;
    private int totalQuestions;
    private int correctAnswers;
    private long timestamp;

    public QuizResult(String setId, int totalQuestions, int correctAnswers) {
        this.setId = setId;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSetId() { return setId; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public long getTimestamp() { return timestamp; }

    public void setSetId(String setId) { this.setId = setId; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getCorrectRate() {
        return totalQuestions == 0 ? 0 : ((double) correctAnswers / totalQuestions) * 100;
    }
}
