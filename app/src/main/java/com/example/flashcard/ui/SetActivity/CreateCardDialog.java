package com.example.flashcard.ui.SetActivity;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.flashcard.R;
import com.example.flashcard.models.Flashcard;
import com.example.flashcard.storage.StorageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateCardDialog {
    public interface OnCardCreatedListener {
        void onCardCreated();
    }

    public static void show(Context context, StorageManager storageManager, String setId, OnCardCreatedListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_create_card, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etName = view.findViewById(R.id.et_name);
        EditText etMeaning = view.findViewById(R.id.et_meaning);
        AutoCompleteTextView etType = view.findViewById(R.id.et_type);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnTranslate = view.findViewById(R.id.btn_translate); // nút dịch
        Button btnCreate = view.findViewById(R.id.btn_create);

        etMeaning.setEnabled(false); // nghĩa tự điền, không sửa

        String[] types = {"noun", "verb", "adjective", "adverb", "phrase", "other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, types);
        etType.setAdapter(adapter);
        etType.setText("noun", false);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // tạo queue Volley
        RequestQueue queue = Volley.newRequestQueue(context);

        btnTranslate.setOnClickListener(v -> {
            String word = etName.getText().toString().trim();
            if (word.isEmpty()) {
                Toast.makeText(context, "Nhập từ trước khi dịch!", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "https://api.tracau.vn/WBBcwnwQpV89/s/" + word + "/en";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray sentences = response.getJSONArray("sentences");
                            if (sentences.length() > 0) {
                                JSONObject first = sentences.getJSONObject(0);
                                JSONObject fields = first.getJSONObject("fields");
                                String meaning = fields.getString("vi");
                                etMeaning.setText(meaning);
                            } else {
                                Toast.makeText(context, "Không tìm thấy nghĩa!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Lỗi khi xử lý dữ liệu!", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(context, "Lỗi kết nối API!", Toast.LENGTH_SHORT).show()
            );
            queue.add(request);
        });

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String meaning = etMeaning.getText().toString().trim();
            String type = etType.getText().toString().trim();

            if (name.isEmpty() || meaning.isEmpty()) {
                Toast.makeText(context, "Tên và nghĩa không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy ngẫu nhiên 3 đáp án sai từ các flashcards khác trong set
            List<Flashcard> allCards = storageManager.getFlashcards(setId);
            List<String> wrongAnswers = new ArrayList<>();
            for (Flashcard card : allCards) {
                if (!card.getMeaning().equals(meaning)) wrongAnswers.add(card.getMeaning());
            }
            while (wrongAnswers.size() > 3) wrongAnswers.remove((int) (Math.random() * wrongAnswers.size()));

            Flashcard newCard = new Flashcard(
                    UUID.randomUUID().toString(),
                    name,
                    type.isEmpty() ? "other" : type,
                    meaning,
                    wrongAnswers
            );

            storageManager.addFlashcard(setId, newCard);
            Toast.makeText(context, "Đã tạo thẻ mới", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            listener.onCardCreated();
        });

        dialog.show();
    }
}
