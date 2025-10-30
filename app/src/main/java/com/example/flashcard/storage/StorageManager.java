package com.example.flashcard.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.flashcard.models.Flashcard;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.models.UserStats;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {

    private static final String PREF_NAME = "QUIZZLET_PREF";
    private static final String KEY_SETS = "FLASHCARD_SETS";
    private static final String KEY_USER = "USER_STATS";
    private static final String KEY_STARRED = "STARRED_CARD_IDS";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public StorageManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<FlashcardSet> getAllSets() {
        String json = sharedPreferences.getString(KEY_SETS, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<FlashcardSet>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveAllSets(List<FlashcardSet> sets) {
        String json = gson.toJson(sets);
        sharedPreferences.edit().putString(KEY_SETS, json).apply();
    }

    public void addSet(FlashcardSet set) {
        List<FlashcardSet> sets = getAllSets();
        sets.add(set);
        saveAllSets(sets);
    }

    public void updateSet(FlashcardSet updatedSet) {
        List<FlashcardSet> sets = getAllSets();
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).getId().equals(updatedSet.getId())) {
                sets.set(i, updatedSet);
                break;
            }
        }
        saveAllSets(sets);
    }

    public void deleteSet(String setId) {
        List<FlashcardSet> sets = getAllSets();
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).getId().equals(setId)) {
                sets.remove(i);
                break;
            }
        }
        saveAllSets(sets);
    }

    public List<Flashcard> getAllFlashcards() {
        List<Flashcard> allCards = new ArrayList<>();
        List<FlashcardSet> sets = getAllSets();
        for (FlashcardSet set : sets) {
            if (set.getFlashcards() != null) {
                allCards.addAll(set.getFlashcards());
            }
        }
        return allCards;
    }

    public void addFlashcard(String setId, Flashcard card) {
        List<FlashcardSet> sets = getAllSets();
        for (FlashcardSet set : sets) {
            if (set.getId().equals(setId)) {
                List<Flashcard> cards = set.getFlashcards();
                if (cards == null) cards = new ArrayList<>();
                cards.add(card);
                set.setFlashcards(cards);
                break;
            }
        }
        saveAllSets(sets);
    }

    public void updateFlashcard(String setId, Flashcard updatedCard) {
        List<FlashcardSet> sets = getAllSets();
        for (FlashcardSet set : sets) {
            if (set.getId().equals(setId)) {
                List<Flashcard> cards = set.getFlashcards();
                if (cards == null) return;
                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).getId().equals(updatedCard.getId())) {
                        cards.set(i, updatedCard);
                        break;
                    }
                }
                set.setFlashcards(cards);
                break;
            }
        }
        saveAllSets(sets);
    }

    public void deleteFlashcard(String setId, String cardId) {
        List<FlashcardSet> sets = getAllSets();
        for (FlashcardSet set : sets) {
            if (set.getId().equals(setId)) {
                List<Flashcard> cards = set.getFlashcards();
                if (cards == null) return;
                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).getId().equals(cardId)) {
                        cards.remove(i);
                        break;
                    }
                }
                set.setFlashcards(cards);
                break;
            }
        }
        saveAllSets(sets);
    }

    public UserStats getUserStats() {
        String json = sharedPreferences.getString(KEY_USER, null);
        if (json == null) return null;
        return gson.fromJson(json, UserStats.class);
    }

    public void saveUserStats(UserStats stats) {
        String json = gson.toJson(stats);
        sharedPreferences.edit().putString(KEY_USER, json).apply();
    }
    public List<Flashcard> getFlashcards(String setId) {
        List<FlashcardSet> sets = getAllSets();
        for (FlashcardSet set : sets) {
            if (set.getId().equals(setId)) {
                List<Flashcard> cards = set.getFlashcards();
                return cards != null ? cards : new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
    /**
     * Lấy danh sách ID của các thẻ đã yêu thích
     */
    public List<String> getStarredCardIds() {
        String json = sharedPreferences.getString(KEY_STARRED, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }

    /**
     * Lưu lại toàn bộ danh sách ID thẻ yêu thích
     */
    private void saveStarredCardIds(List<String> starredIds) {
        String json = gson.toJson(starredIds);
        sharedPreferences.edit().putString(KEY_STARRED, json).apply();
    }

    /**
     * Kiểm tra xem một thẻ có đang được yêu thích không
     */
    public boolean isCardStarred(String cardId) {
        List<String> starredIds = getStarredCardIds();
        return starredIds.contains(cardId);
    }

    /**
     * Thêm một thẻ vào danh sách yêu thích
     */
    public void addStarredCard(String cardId) {
        List<String> starredIds = getStarredCardIds();
        if (!starredIds.contains(cardId)) {
            starredIds.add(cardId);
            saveStarredCardIds(starredIds);
        }
    }

    /**
     * Xóa một thẻ khỏi danh sách yêu thích
     */
    public void removeStarredCard(String cardId) {
        List<String> starredIds = getStarredCardIds();
        if (starredIds.contains(cardId)) {
            starredIds.remove(cardId);
            saveStarredCardIds(starredIds);
        }
    }

}
