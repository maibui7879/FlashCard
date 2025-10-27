package com.example.flashcard.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.adapters.FlashcardSetAdapter;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.storage.StorageManager;
import com.example.flashcard.ui.SetActivity.SetActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.UUID;

public class SetListActivity extends BaseActivity implements FlashcardSetAdapter.OnItemClickListener {

    private RecyclerView recyclerViewSets;
    private FloatingActionButton fabAddSet;
    private StorageManager storageManager;
    private List<FlashcardSet> setList;
    private FlashcardSetAdapter adapter;

    @Override
    protected String getHeaderTitle() {
        return "Bộ thẻ";
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_set_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storageManager = new StorageManager(this);
        recyclerViewSets = findViewById(R.id.recyclerViewSets);
        fabAddSet = findViewById(R.id.fab_add_set);

        recyclerViewSets.setLayoutManager(new LinearLayoutManager(this));
        loadSets();

        fabAddSet.setOnClickListener(v -> showCreateSetDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSets();
    }

    private void loadSets() {
        setList = storageManager.getAllSets();
        adapter = new FlashcardSetAdapter(setList, this);
        recyclerViewSets.setAdapter(adapter);
    }

    @Override
    public void onItemClick(FlashcardSet set) {
        Intent intent = new Intent(SetListActivity.this, SetActivity.class);
        intent.putExtra("SET_ID", set.getId());
        startActivity(intent);
    }


    @Override
    public void onMenuClick(FlashcardSet set, View menuView) {
        PopupMenu popup = new PopupMenu(this, menuView);
        popup.getMenuInflater().inflate(R.menu.set_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                // Mở SetActivity và truyền id của set
                Intent intent = new Intent(SetListActivity.this, SetActivity.class);
                intent.putExtra("SET_ID", set.getId());
                startActivity(intent);
                return true;
            } else if (itemId == R.id.action_delete) {
                storageManager.deleteSet(set.getId());
                loadSets();
                Toast.makeText(this, "Đã xóa Set: " + set.getName(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }


    private void showCreateSetDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_create_set, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etSetName = view.findViewById(R.id.et_set_name);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnCreate = view.findViewById(R.id.btn_create);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String setName = etSetName.getText().toString().trim();
            if (setName.isEmpty()) {
                etSetName.setError("Tên Set không được để trống!");
                return;
            }

            String newId = UUID.randomUUID().toString();
            FlashcardSet newSet = new FlashcardSet(newId, setName);
            storageManager.addSet(newSet);

            loadSets();
            Toast.makeText(this, "Đã tạo Set mới: " + setName, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}
