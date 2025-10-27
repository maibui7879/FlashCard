package com.example.flashcard.ui.SetActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.flashcard.R;
import com.example.flashcard.adapters.FlashcardAdapter;
import com.example.flashcard.models.Flashcard;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.storage.StorageManager;
import com.example.flashcard.ui.BaseActivity;
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

        fabAddCard.setOnClickListener(v ->
                CreateCardDialog.show(this, storageManager, currentSet.getId(), () -> {
                    refreshCurrentSet(currentSet.getId());
                    loadCards();
                })
        );
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
        // Thay vì mở EditCardDialog, chuyển sang DictionaryActivity
        Intent intent = new Intent(this, DictionaryActivity.class);
        intent.putExtra("WORD", card.getName());
        startActivity(intent);
    }
}
