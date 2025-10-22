package com.example.flashcard.models;

import java.io.Serializable;
//thống kê ng dùng
public class UserStats implements Serializable {
    private String id;
    private String name;
    private int wordsRemembered;
    private int quizzesCompleted;
    private double correctRate;

    public UserStats(String id, String name) {
        this.id = id;
        this.name = (name == null || name.isEmpty()) ? "Người dùng " + id : name;
        this.wordsRemembered = 0;
        this.quizzesCompleted = 0;
        this.correctRate = 0.0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getWordsRemembered() { return wordsRemembered; }
    public int getQuizzesCompleted() { return quizzesCompleted; }
    public double getCorrectRate() { return correctRate; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setWordsRemembered(int wordsRemembered) { this.wordsRemembered = wordsRemembered; }
    public void setQuizzesCompleted(int quizzesCompleted) { this.quizzesCompleted = quizzesCompleted; }
    public void setCorrectRate(double correctRate) { this.correctRate = correctRate; }
}
