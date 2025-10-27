package com.example.flashcard.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.adapters.FlashcardAdapter;
import com.example.flashcard.models.Flashcard;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.storage.StorageManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SetActivity extends BaseActivity implements FlashcardAdapter.OnFlashcardClickListener {

    private RecyclerView recyclerViewCards;
    private FloatingActionButton fabAddCard;
    private StorageManager storageManager;
    private FlashcardSet currentSet;
    private FlashcardAdapter adapter;
    private List<Flashcard> cardList;

    @Override
    protected String getHeaderTitle() {
        return "Bộ thẻ của bạn";
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_set;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String setId = getIntent().getStringExtra("SET_ID");
        if (setId == null) {
            Toast.makeText(this, "Không tìm thấy bộ thẻ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        storageManager = new StorageManager(this);
        refreshCurrentSet(setId);

        recyclerViewCards = findViewById(R.id.recyclerViewCards);
        fabAddCard = findViewById(R.id.fab_add_card);

        recyclerViewCards.setLayoutManager(new GridLayoutManager(this, 2));
        loadCards();

        fabAddCard.setOnClickListener(v -> showCreateCardDialog());
    }

    private void refreshCurrentSet(String setId) {
        List<FlashcardSet> sets = storageManager.getAllSets();
        for (FlashcardSet set : sets) {
            if (set.getId().equals(setId)) {
                currentSet = set;
                break;
            }
        }
    }

    private void loadCards() {
        cardList = currentSet.getFlashcards() != null ? currentSet.getFlashcards() : new ArrayList<>();
        if (adapter == null) {
            adapter = new FlashcardAdapter(cardList, this);
            recyclerViewCards.setAdapter(adapter);
        } else {
            adapter.updateList(cardList);
        }
    }

    @Override
    public void onDeleteClick(Flashcard card) {
        storageManager.deleteFlashcard(currentSet.getId(), card.getId());
        refreshCurrentSet(currentSet.getId());
        loadCards();
        Toast.makeText(this, "Đã xóa thẻ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(Flashcard card) {
        showEditCardDialog(card);
    }

    private void showCreateCardDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_create_card, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etName = view.findViewById(R.id.et_name);
        EditText etMeaning = view.findViewById(R.id.et_meaning);
        EditText etType = view.findViewById(R.id.et_type);
        EditText etWrong1 = view.findViewById(R.id.et_wrong1);
        EditText etWrong2 = view.findViewById(R.id.et_wrong2);
        EditText etWrong3 = view.findViewById(R.id.et_wrong3);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnCreate = view.findViewById(R.id.btn_create);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String meaning = etMeaning.getText().toString().trim();
            String type = etType.getText().toString().trim();
            List<String> wrongAnswers = new ArrayList<>();
            if (!etWrong1.getText().toString().trim().isEmpty()) wrongAnswers.add(etWrong1.getText().toString().trim());
            if (!etWrong2.getText().toString().trim().isEmpty()) wrongAnswers.add(etWrong2.getText().toString().trim());
            if (!etWrong3.getText().toString().trim().isEmpty()) wrongAnswers.add(etWrong3.getText().toString().trim());

            if (name.isEmpty() || meaning.isEmpty()) {
                Toast.makeText(this, "Tên và nghĩa không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            Flashcard newCard = new Flashcard(
                    UUID.randomUUID().toString(),
                    name,
                    type.isEmpty() ? "default" : type,
                    meaning,
                    wrongAnswers
            );

            storageManager.addFlashcard(currentSet.getId(), newCard);
            refreshCurrentSet(currentSet.getId());
            loadCards();
            Toast.makeText(this, "Đã tạo thẻ mới", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditCardDialog(Flashcard card) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_create_card, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etName = view.findViewById(R.id.et_name);
        EditText etMeaning = view.findViewById(R.id.et_meaning);
        EditText etType = view.findViewById(R.id.et_type);
        EditText etWrong1 = view.findViewById(R.id.et_wrong1);
        EditText etWrong2 = view.findViewById(R.id.et_wrong2);
        EditText etWrong3 = view.findViewById(R.id.et_wrong3);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnCreate = view.findViewById(R.id.btn_create);

        etName.setText(card.getName());
        etMeaning.setText(card.getMeaning());
        etType.setText(card.getType());
        List<String> wrongs = new ArrayList<>(card.getAnswers());
        wrongs.removeIf(ans -> ans.equalsIgnoreCase(card.getMeaning()));
        if (wrongs.size() > 0) etWrong1.setText(wrongs.size() > 0 ? wrongs.get(0) : "");
        if (wrongs.size() > 1) etWrong2.setText(wrongs.get(1));
        if (wrongs.size() > 2) etWrong3.setText(wrongs.get(2));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setText("Cập nhật");
        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String meaning = etMeaning.getText().toString().trim();
            String type = etType.getText().toString().trim();
            List<String> wrongAnswers = new ArrayList<>();
            if (!etWrong1.getText().toString().trim().isEmpty()) wrongAnswers.add(etWrong1.getText().toString().trim());
            if (!etWrong2.getText().toString().trim().isEmpty()) wrongAnswers.add(etWrong2.getText().toString().trim());
            if (!etWrong3.getText().toString().trim().isEmpty()) wrongAnswers.add(etWrong3.getText().toString().trim());

            if (name.isEmpty() || meaning.isEmpty()) {
                Toast.makeText(this, "Tên và nghĩa không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            card.setName(name);
            card.setMeaning(meaning);
            card.setType(type.isEmpty() ? "default" : type);
            card.setAnswers(wrongAnswers);

            storageManager.updateFlashcard(currentSet.getId(), card);
            refreshCurrentSet(currentSet.getId());
            loadCards();
            Toast.makeText(this, "Đã cập nhật thẻ", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
