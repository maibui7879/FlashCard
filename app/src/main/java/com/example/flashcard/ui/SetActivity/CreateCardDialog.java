package com.example.flashcard.ui.SetActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.UUID;

public class CreateCardDialog {

    private static final String TAG = "CreateCardDialog";

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
        EditText etAns1 = view.findViewById(R.id.et_ans1); // đáp án đúng
        EditText etAns2 = view.findViewById(R.id.et_ans2); // đáp án sai 1
        EditText etAns3 = view.findViewById(R.id.et_ans3); // đáp án sai 2
        EditText etAns4 = view.findViewById(R.id.et_ans4); // đáp án sai 3
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnTranslate = view.findViewById(R.id.btn_translate);
        Button btnCreate = view.findViewById(R.id.btn_create);

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Đang dịch...");
        progressDialog.setCancelable(false);

        String[] types = {"noun", "verb", "adjective", "adverb", "phrase", "other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, types);
        etType.setAdapter(adapter);
        etType.setText("noun", false);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Đồng bộ typing: etMeaning -> etAns1
        etMeaning.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                etAns1.setText(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        RequestQueue queue = Volley.newRequestQueue(context);

        btnTranslate.setOnClickListener(v -> {
            String word = etName.getText().toString().trim();
            if (word.isEmpty()) {
                Toast.makeText(context, "Nhập từ trước khi dịch!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();
            setInputsEnabled(false, etName, etMeaning, etType, etAns2, etAns3, etAns4, btnCancel, btnTranslate, btnCreate);

            String url = "https://api.tracau.vn/WBBcwnwQpV89/s/" + word + "/en";
            Log.d(TAG, "Requesting API: " + url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
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
                            etAns1.setText(meaning);
                            etAns1.setEnabled(false);
                            Toast.makeText(context, "Đã điền nghĩa", Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Exception", e);
                            Toast.makeText(context, "Lỗi khi xử lý dữ liệu!", Toast.LENGTH_SHORT).show();
                        } finally {
                            progressDialog.dismiss();
                            setInputsEnabled(true, etName, etMeaning, etType, etAns2, etAns3, etAns4, btnCancel, btnTranslate, btnCreate);
                        }
                    },
                    error -> {
                        Log.e(TAG, "Volley error", error);
                        Toast.makeText(context, "Không thể kết nối API!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        setInputsEnabled(true, etName, etMeaning, etType, etAns2, etAns3, etAns4, btnCancel, btnTranslate, btnCreate);
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

            List<String> wrongAnswers = new ArrayList<>();
            wrongAnswers.add(ans2);
            wrongAnswers.add(ans3);
            wrongAnswers.add(ans4);

            Flashcard newCard = new Flashcard(
                    UUID.randomUUID().toString(),
                    name,
                    type.isEmpty() ? "other" : type,
                    meaning,
                    wrongAnswers
            );

            storageManager.addFlashcard(setId, newCard);
            Toast.makeText(context, "Đã tạo thẻ mới", Toast.LENGTH_SHORT).show();
            listener.onCardCreated();
            dialog.dismiss();
        });

        dialog.show();
    }

    private static void setInputsEnabled(boolean enabled, EditText etName, EditText etMeaning, AutoCompleteTextView etType,
                                         EditText etAns2, EditText etAns3, EditText etAns4, Button btnCancel, Button btnTranslate, Button btnCreate) {
        etName.setEnabled(enabled);
        etMeaning.setEnabled(enabled);
        etType.setEnabled(enabled);
        etAns2.setEnabled(enabled);
        etAns3.setEnabled(enabled);
        etAns4.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
        btnTranslate.setEnabled(enabled);
        btnCreate.setEnabled(enabled);
    }
}
