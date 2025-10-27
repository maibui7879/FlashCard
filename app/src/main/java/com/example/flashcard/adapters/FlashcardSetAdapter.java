package com.example.flashcard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.models.FlashcardSet;

import java.util.List;

public class FlashcardSetAdapter extends RecyclerView.Adapter<FlashcardSetAdapter.SetViewHolder> {

    private List<FlashcardSet> setList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FlashcardSet set);
        void onMenuClick(FlashcardSet set, View menuView);
    }

    public FlashcardSetAdapter(List<FlashcardSet> setList, OnItemClickListener listener) {
        this.setList = setList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard_set, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        FlashcardSet set = setList.get(position);
        holder.tvSetName.setText(set.getName());
        holder.tvCardCount.setText(set.getFlashcards().size() + " thẻ");

        // Xử lý sự kiện click vào toàn bộ item
        holder.itemView.setOnClickListener(v -> listener.onItemClick(set));

        // Xử lý sự kiện click vào nút Menu/Sửa
        holder.btnEditMenu.setOnClickListener(v -> listener.onMenuClick(set, v));
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    public static class SetViewHolder extends RecyclerView.ViewHolder {
        TextView tvSetName;
        TextView tvCardCount;
        LinearLayout btnEditMenu; // Bọc chữ Sửa và icon 3 chấm

        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSetName = itemView.findViewById(R.id.tv_set_name);
            tvCardCount = itemView.findViewById(R.id.tv_card_count);
            btnEditMenu = itemView.findViewById(R.id.btn_edit_menu);
        }
    }
}