package com.example.flashcard.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcard.R;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.models.QuizResult;
import com.example.flashcard.storage.StorageManager;
import com.example.flashcard.storage.QuizRepository; // Thêm import cho QuizRepository

import java.util.List;

public class MainSetAdapter extends RecyclerView.Adapter<MainSetAdapter.SetViewHolder> {

    private final List<FlashcardSet> setList;
    private final String mode; // "LEARN" hoặc "QUIZ"
    private final OnItemClickListener listener;
    private final StorageManager storageManager;
    private final QuizRepository quizRepository; // Khai báo QuizRepository

    public interface OnItemClickListener {
        void onItemClick(FlashcardSet set, String mode);
    }

    public MainSetAdapter(Context context, List<FlashcardSet> setList, String mode, OnItemClickListener listener) {
        this.setList = setList;
        this.mode = mode;
        this.listener = listener;
        this.storageManager = new StorageManager(context);
        this.quizRepository = new QuizRepository(context); // Khởi tạo QuizRepository
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_main_set_progress, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {
        FlashcardSet set = setList.get(position);
        holder.tvSetName.setText(set.getName());

        int totalCards = set.getFlashcards() != null ? set.getFlashcards().size() : 0;

        // Luôn set max cho Progress Bar là tổng số thẻ (cả 2 chế độ)
        holder.progressBarSet.setMax(totalCards > 0 ? totalCards : 1); // Tránh max = 0

        String detailText;
        int progressValue = 0; // Giá trị tiến trình / kỷ lục

        if (mode.equals("LEARN")) {
            // --- CHẾ ĐỘ HỌC: Lấy vị trí thẻ hiện tại ---

            // Lấy vị trí thẻ đang học (index 0-based, nên là progressValue)
            int currentProgressIndex = storageManager.getStudyProgress(set.getId());
            // Vì progress là vị trí thẻ tiếp theo cần học, nên nó chính là giá trị progress.
            progressValue = currentProgressIndex;

            int percentage = totalCards > 0 ? (progressValue * 100) / totalCards : 0;

            holder.imgModeIcon.setImageResource(R.drawable.ic_book_white);
            holder.imgModeIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.blue_primary));

            detailText = "Tiến độ: " + progressValue + "/" + totalCards + " thẻ (" + percentage + "%)";

        } else { // mode.equals("QUIZ")
            // --- CHẾ ĐỘ QUIZ: Lấy kỷ lục điểm cao nhất ---

            List<QuizResult> results = quizRepository.getResults(set.getId());
            QuizResult bestResult = getBestQuizResult(results);

            // Cần đảm bảo có các drawable này:
            holder.imgModeIcon.setImageResource(R.drawable.ic_test);
            holder.imgModeIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.orange)); // Màu cam cho Quiz

            if (bestResult != null && totalCards > 0) {
                progressValue = bestResult.getCorrectAnswers();
                int percentage = (progressValue * 100) / totalCards;
                detailText = "Kỷ lục: " + progressValue + "/" + totalCards + " đúng (" + percentage + "%)";
            } else {
                detailText = "Chưa có kỷ lục.";
                progressValue = 0;
            }
        }

        holder.tvProgressDetail.setText(detailText);
        holder.progressBarSet.setProgress(progressValue);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(set, mode));
    }

    /**
     * Tìm kết quả Quiz tốt nhất (số câu trả lời đúng cao nhất)
     */
    private QuizResult getBestQuizResult(List<QuizResult> results) {
        if (results == null || results.isEmpty()) return null;

        QuizResult best = null;
        int maxScore = -1;

        for (QuizResult result : results) {
            if (result.getCorrectAnswers() > maxScore) {
                maxScore = result.getCorrectAnswers();
                best = result;
            }
        }
        return best;
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    public static class SetViewHolder extends RecyclerView.ViewHolder {
        TextView tvSetName, tvProgressDetail;
        ProgressBar progressBarSet;
        ImageView imgModeIcon;

        public SetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSetName = itemView.findViewById(R.id.tvSetName);
            tvProgressDetail = itemView.findViewById(R.id.tvProgressDetail);
            progressBarSet = itemView.findViewById(R.id.progressBarSet);
            imgModeIcon = itemView.findViewById(R.id.img_mode_icon);
        }
    }
}