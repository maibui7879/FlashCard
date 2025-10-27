package com.example.flashcard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.models.Flashcard;

import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {

    public interface OnFlashcardClickListener {
        void onDeleteClick(Flashcard card);
        void onEditClick(Flashcard card);
    }

    private List<Flashcard> cards;
    private OnFlashcardClickListener listener;

    public FlashcardAdapter(List<Flashcard> cards, OnFlashcardClickListener listener) {
        this.cards = cards;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Flashcard card = cards.get(position);
        holder.tvFront.setText(card.getName());

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(card));
        holder.tvFront.setOnClickListener(v -> listener.onEditClick(card)); // click tên để sửa
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void updateList(List<Flashcard> newCards) {
        this.cards = newCards;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFront;
        ImageView btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvFront = itemView.findViewById(R.id.tv_front);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
