package com.example.flashcard.utils;

import com.example.flashcard.models.Flashcard;
import java.util.ArrayList;
import java.util.List;

public class FlashcardData {

    public static List<Flashcard> getSampleData() {
        List<Flashcard> list = new ArrayList<>();

        list.add(new Flashcard("1", "Apple", "noun", "Quả táo 🍎", null));
        list.add(new Flashcard("2", "Run", "verb", "Chạy 🏃‍♂️", null));
        list.add(new Flashcard("3", "Blue", "adjective", "Màu xanh dương 💙", null));
        list.add(new Flashcard("4", "Book", "noun", "Quyển sách 📖", null));
        list.add(new Flashcard("5", "Happy", "adjective", "Vui vẻ 😊", null));

        return list;
    }
}
