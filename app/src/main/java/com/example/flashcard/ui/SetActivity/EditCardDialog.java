package com.example.flashcard.ui.SetActivity;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
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

public class EditCardDialog {

    private static final String TAG = "EditCardDialog";

    public interface OnCardUpdatedListener {
        void onCardUpdated();
    }

    public static void show(Context context, StorageManager storageManager, String setId, Flashcard card, OnCardUpdatedListener listener) {
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
        EditText etAns1 = view.findViewById(R.id.et_ans1); // đáp án đúng
        EditText etAns2 = view.findViewById(R.id.et_ans2); // đáp án sai 1
        EditText etAns3 = view.findViewById(R.id.et_ans3); // đáp án sai 2
        EditText etAns4 = view.findViewById(R.id.et_ans4); // đáp án sai 3
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnTranslate = view.findViewById(R.id.btn_translate);
        Button btnCreate = view.findViewById(R.id.btn_create);

        // set dữ liệu cũ
        etName.setText(card.getName());
        etMeaning.setText(card.getMeaning());
        etAns1.setText(card.getMeaning());
        etAns1.setEnabled(false); // đáp án đúng không edit
        etType.setText(card.getType());

        // set các đáp án sai cũ
        List<String> wrongAnswers = card.getAnswers();
        if (wrongAnswers != null && wrongAnswers.size() >= 3) {
            etAns2.setText(wrongAnswers.get(0));
            etAns3.setText(wrongAnswers.get(1));
            etAns4.setText(wrongAnswers.get(2));
        }

        String[] types = {"noun", "verb", "adjective", "adverb", "phrase", "other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, types);
        etType.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnCreate.setText("Cập nhật");

        RequestQueue queue = Volley.newRequestQueue(context);

        // khi nhấn translate
        btnTranslate.setOnClickListener(v -> {
            String word = etName.getText().toString().trim();
            if (word.isEmpty()) {
                Toast.makeText(context, "Nhập từ trước khi dịch!", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "https://api.tracau.vn/WBBcwnwQpV89/s/" + word + "/en";
            Log.d(TAG, "Requesting API: " + url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            Log.d(TAG, "API response: " + response.toString());
                            JSONArray sentences = response.optJSONArray("sentences");
                            if (sentences == null || sentences.length() == 0) {
                                Toast.makeText(context, "Chúng tôi hiện chưa có nghĩa của từ này!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            JSONObject first = sentences.getJSONObject(0);
                            JSONObject fields = first.optJSONObject("fields");
                            if (fields == null) {
                                Toast.makeText(context, "Chúng tôi hiện chưa có nghĩa của từ này!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String meaning = fields.optString("vi", "");
                            if (meaning.isEmpty()) {
                                Toast.makeText(context, "Chúng tôi hiện chưa có nghĩa của từ này!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            etMeaning.setText(meaning);
                            etAns1.setText(meaning); // đồng bộ với đáp án đúng
                            Toast.makeText(context, "Đã điền nghĩa", Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Exception", e);
                            Toast.makeText(context, "Lỗi khi xử lý dữ liệu!", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e(TAG, "Volley error", error);
                        Toast.makeText(context, "Không thể kết nối API!", Toast.LENGTH_SHORT).show();
                    }
            );
            queue.add(request);
        });

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String meaning = etMeaning.getText().toString().trim();
            String type = etType.getText().toString().trim();
            String ans2 = etAns2.getText().toString().trim();
            String ans3 = etAns3.getText().toString().trim();
            String ans4 = etAns4.getText().toString().trim();

            if (name.isEmpty() || meaning.isEmpty() || ans2.isEmpty() || ans3.isEmpty() || ans4.isEmpty()) {
                Toast.makeText(context, "Nhập đủ tên và 4 đáp án!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> newWrongAnswers = new ArrayList<>();
            newWrongAnswers.add(ans2);
            newWrongAnswers.add(ans3);
            newWrongAnswers.add(ans4);

            card.setName(name);
            card.setMeaning(meaning);
            card.setType(type.isEmpty() ? "other" : type);
            card.setAnswers(newWrongAnswers);

            storageManager.updateFlashcard(setId, card);
            Log.d(TAG, "Cập nhật flashcard: " + card.getId());
            Toast.makeText(context, "Đã cập nhật thẻ", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            listener.onCardUpdated();
        });

        dialog.show();
    }
}
