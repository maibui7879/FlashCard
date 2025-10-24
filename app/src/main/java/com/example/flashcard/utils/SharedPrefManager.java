package com.example.flashcard.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "study_prefs";
    private static final String KEY_PROGRESS = "progress_index";

    public static void saveStudyProgress(Context context, int index) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_PROGRESS, index).apply();
    }

    public static int getStudyProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_PROGRESS, 0);
    }
}

