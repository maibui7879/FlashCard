package com.example.flashcard.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Flashcard implements Serializable {
    private String id;
    private String name;
    private String type;
    private String meaning;
    private List<String> answers;
    private long createdAt;
    private long updatedAt;

    public Flashcard(String id, String name, String type, String meaning, List<String> wrongAnswers) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.meaning = meaning;
        this.answers = new ArrayList<>();
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
        setAnswers(wrongAnswers);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getMeaning() { return meaning; }
    public List<String> getAnswers() { return answers; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; updateTimestamp(); }
    public void setName(String name) { this.name = name; updateTimestamp(); }
    public void setType(String type) { this.type = type; updateTimestamp(); }
    public void setMeaning(String meaning) { this.meaning = meaning; updateTimestamp(); }

    public void setAnswers(List<String> wrongAnswers) {
        this.answers.clear();
        if (wrongAnswers != null) {
            for (String ans : wrongAnswers) {
                if (!ans.equalsIgnoreCase(meaning)) this.answers.add(ans);
            }
        }
        if (!this.answers.contains(meaning)) this.answers.add(meaning);
        while (this.answers.size() > 4) this.answers.remove(0);
        Collections.shuffle(this.answers);
        updateTimestamp();
    }

    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
}
