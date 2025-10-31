package com.example.flashcard.ui.SetActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.flashcard.ui.BaseActivity;
import com.example.flashcard.ui.QuizActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SetActivity extends BaseActivity implements FlashcardAdapter.OnFlashcardClickListener {

    private RecyclerView recyclerViewCards;
    private FloatingActionButton fabAddCard;
    private StorageManager storageManager;
    private FlashcardSet currentSet;
    private FlashcardAdapter adapter;
    private List<Flashcard> cardList;
    private List<Flashcard> filteredList;

    private EditText etSearch;
    private AutoCompleteTextView spinnerFilter;
    private Button btnLearn, btnQuiz;

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
        if (currentSet == null) {
            Toast.makeText(this, "Bộ thẻ không tồn tại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerViewCards = findViewById(R.id.recyclerViewCards);
        fabAddCard = findViewById(R.id.fab_add_card);
        etSearch = findViewById(R.id.et_search);
        spinnerFilter = findViewById(R.id.spinner_filter);
        btnLearn = findViewById(R.id.btn_learn);
        btnQuiz = findViewById(R.id.btn_quiz);

        recyclerViewCards.setLayoutManager(new GridLayoutManager(this, 2));

        setupFilter();
        loadCards();
        setupSearch();

        fabAddCard.setOnClickListener(v ->
                CreateCardDialog.show(this, storageManager, currentSet.getId(), () -> {
                    refreshCurrentSet(currentSet.getId());
                    loadCards();
                })
        );

        // Chế độ học (placeholder)
        btnLearn.setOnClickListener(v ->
                Toast.makeText(this, "Vào học", Toast.LENGTH_SHORT).show()
        );

        // Chế độ quiz: mở QuizActivity, truyền SET_ID, chặn khi set trống
        btnQuiz.setOnClickListener(v -> {
            if (currentSet.getFlashcards() == null || currentSet.getFlashcards().isEmpty()) {
                Toast.makeText(this, "Bộ thẻ trống, hãy thêm thẻ trước khi làm quiz!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(SetActivity.this, QuizActivity.class);
            intent.putExtra("SET_ID", currentSet.getId());
            intent.putExtra("SET_NAME", currentSet.getName()); // optional
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // quay lại từ Quiz/Chỉnh sửa thẻ → refresh dữ liệu
        if (currentSet != null) {
            refreshCurrentSet(currentSet.getId());
            loadCards();
        }
    }

    private void refreshCurrentSet(String setId) {
        List<FlashcardSet> sets = storageManager.getAllSets();
        currentSet = null;
        for (FlashcardSet set : sets) {
            if (set.getId().equals(setId)) {
                currentSet = set;
                break;
            }
        }
    }

    private void loadCards() {
        cardList = (currentSet.getFlashcards() != null) ? currentSet.getFlashcards() : new ArrayList<>();
        filteredList = new ArrayList<>(cardList);
        if (adapter == null) {
            adapter = new FlashcardAdapter(filteredList, this);
            recyclerViewCards.setAdapter(adapter);
        } else {
            adapter.updateList(filteredList);
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterCards(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilter() {
        String[] types = {"Tất cả", "noun", "verb", "adjective", "adverb"};
        ArrayAdapter<String> adapterFilter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types);
        spinnerFilter.setAdapter(adapterFilter);

        spinnerFilter.setText("Tất cả", false);

        spinnerFilter.setOnItemClickListener((parent, view, position, id) -> {
            String selected = adapterFilter.getItem(position);
            spinnerFilter.setText(selected, false);
            filterCards();
        });

        spinnerFilter.setOnClickListener(v -> spinnerFilter.showDropDown());
        spinnerFilter.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) spinnerFilter.showDropDown(); });
    }

    private void filterCards() {
        String query = etSearch.getText().toString().toLowerCase().trim();
        String selectedType = spinnerFilter.getText().toString();

        filteredList.clear();
        for (Flashcard card : cardList) {
            boolean matchesSearch = card.getName().toLowerCase().contains(query);
            boolean matchesType = selectedType.equals("Tất cả") ||
                    card.getType().equalsIgnoreCase(selectedType);
            if (matchesSearch && matchesType) filteredList.add(card);
        }
        adapter.updateList(filteredList);
    }

    @Override
    public void onDeleteClick(Flashcard card) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa thẻ này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    storageManager.deleteFlashcard(currentSet.getId(), card.getId());
                    refreshCurrentSet(currentSet.getId());
                    loadCards();
                    Toast.makeText(this, "Đã xóa thẻ", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onEditClick(Flashcard card) {
        EditCardDialog.show(this, storageManager, currentSet.getId(), card, () -> {
            refreshCurrentSet(currentSet.getId());
            loadCards();
        });
    }
}
