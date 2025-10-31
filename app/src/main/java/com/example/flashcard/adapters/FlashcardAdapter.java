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
        holder.tvType.setText(card.getType());

        int color;
        switch (card.getType().toLowerCase()) {
            case "verb":
                color = 0xFFE53935; // đỏ
                break;
            case "adjective":
                color = 0xFFFB8C00; // cam
                break;
            case "adverb":
                color = 0xFF8E24AA; // tím
                break;
            case "noun":
            default:
                color = 0xFF1976D2; // xanh dương
                break;
        }

        holder.tvType.setTextColor(color);
        holder.borderTop.setBackgroundColor(color);

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(card));
        holder.tvFront.setOnClickListener(v -> listener.onEditClick(card));
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
        TextView tvFront, tvType;
        ImageView btnDelete;
        View borderTop;

        ViewHolder(View itemView) {
            super(itemView);
            tvFront = itemView.findViewById(R.id.tv_front);
            tvType = itemView.findViewById(R.id.tv_type);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            borderTop = itemView.findViewById(R.id.type_border_top);
        }
    }
}
