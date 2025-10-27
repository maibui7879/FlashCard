package com.example.flashcard.api;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiRequest {

    private static final String BASE_URL = "https://api.tracau.vn/WBBcwnwQpV89/s/";

    public interface Callback {
        void onSuccess(String meanings);
        void onError(String error);
    }

    public static void getMeaning(RequestQueue queue, String word, Callback callback) {
        if (word == null || word.trim().isEmpty()) {
            callback.onError("Từ trống");
            return;
        }

        String url = BASE_URL + word.trim() + "/en";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray sentences = response.getJSONArray("sentences");
                            StringBuilder meanings = new StringBuilder();
                            for (int i = 0; i < sentences.length(); i++) {
                                JSONObject fields = sentences.getJSONObject(i).getJSONObject("fields");
                                meanings.append("- ").append(fields.getString("vi")).append("\n");
                            }
                            callback.onSuccess(meanings.toString());
                        } catch (JSONException e) {
                            callback.onError("Lỗi phân tích dữ liệu");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Lỗi gọi API: " + error.toString());
                    }
                }
        );

        queue.add(request);
    }
}
