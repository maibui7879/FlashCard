package com.example.flashcard.ui;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import com.example.flashcard.R;
import com.example.flashcard.models.Flashcard;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.models.QuizResult;
import com.example.flashcard.storage.StorageManager;

import java.util.ArrayList;
import java.util.Collections; // Thêm import này cho Collections.shuffle
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudyActivity extends BaseActivity {

    // Khai báo các View từ activity_study.xml
    private TextView tvFrontWord, tvFrontType, tvBackMeaning, tv_get_hint;
    private TextView tvCardProgressUntracked, tvCardProgressTracked, tvRememberedCount;
    private CardView cardFront, cardBack;
    private FrameLayout flashcardContainer;
    private Switch switchTrackProgress;
    private LinearLayout navigationUntracked, navigationTracked;
    // Đã thêm Progress Bar
    private ProgressBar progressBarUntracked;
    private ProgressBar progressBarTracked;
    // Nút điều hướng (ID đã được đồng bộ với XML mới nhất của bạn)
    private ImageButton btnPrevUntracked, btnNextUntracked;
    private ImageButton btnWrongTracked, btnCorrectTracked; // Giữ lại tên cũ để đồng bộ logic track

    // Biến cho animation
    private AnimatorSet flipOutAnimator;
    private AnimatorSet flipInAnimator;
    private boolean isFrontVisible = true;
    private boolean hintShown = false;

    // Biến dữ liệu
    private StorageManager storageManager;
    private FlashcardSet currentSet;
    private List<Flashcard> flashcards;
    private int currentIndex = 0;
    private int totalCards = 0; // Thêm biến tổng số thẻ
    private boolean isTrackingProgress = true;
    private Set<String> rememberedCardIds;

    // Biến cho auto-play
    private Handler playHandler;
    private Runnable playRunnable;
    private boolean isPlaying = false;
    private static final long DELAY_FLIP_TO_BACK = 2000; // 2 giây (thời gian xem mặt trước)
    private static final long DELAY_NEXT_CARD = 1500; // 1.5 giây (thời gian xem mặt sau)

    private ImageButton btnPlayUntracked, btnPlayTracked;
    private ImageButton btnShuffleUntracked, btnShuffleTracked;
    private ImageView ivStar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // BaseActivity đã gọi setContentView, chỉ cần set tiêu đề
        TextView headerTitle = findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText("Study Mode");
        }

        // --- Ánh xạ Views (Tìm các view theo ID) ---
        tvFrontWord = findViewById(R.id.tvFrontWord);
        tvFrontType = findViewById(R.id.tvFrontType);
        tvBackMeaning = findViewById(R.id.tvBackMeaning);
        tv_get_hint = findViewById(R.id.tv_get_hint);
        tvCardProgressUntracked = findViewById(R.id.tv_card_progress_untracked);
        tvCardProgressTracked = findViewById(R.id.tv_card_progress_tracked);
        tvRememberedCount = findViewById(R.id.tvRememberedCount);
        flashcardContainer = findViewById(R.id.flashcard_container);
        cardFront = findViewById(R.id.card_front);
        cardBack = findViewById(R.id.card_back);
        switchTrackProgress = findViewById(R.id.switch_track_progress);
        navigationUntracked = findViewById(R.id.navigation_untracked);
        navigationTracked = findViewById(R.id.navigation_tracked);

        // Ánh xạ Progress Bar
        progressBarUntracked = findViewById(R.id.progress_bar_untracked);
        progressBarTracked = findViewById(R.id.progress_bar_tracked);

        // Ánh xạ Nút điều hướng
        btnPrevUntracked = findViewById(R.id.btn_prev_card_untracked);
        btnNextUntracked = findViewById(R.id.btn_next_card_untracked);
        // ID đã được sửa trong XML lần trước, nhưng giữ lại tên biến logic cũ
        btnWrongTracked = findViewById(R.id.btn_wrong_tracked);
        btnCorrectTracked = findViewById(R.id.btn_correct_tracked);

        btnPlayUntracked = findViewById(R.id.btn_play_untracked);
        btnPlayTracked = findViewById(R.id.btn_play_tracked);
        btnShuffleUntracked = findViewById(R.id.btn_shuffle_untracked);
        btnShuffleTracked = findViewById(R.id.btn_shuffle_tracked);
        ivStar = findViewById(R.id.iv_star);

        // Nút Get a Hint
        if (tv_get_hint != null) {
            tv_get_hint.setOnClickListener(v -> showHint());
        }

        // --- Khởi tạo ---
        storageManager = new StorageManager(this);
        loadAnimations(); // Gọi hàm tải animation

        // --- Lấy dữ liệu thẻ ---
        Intent intent = getIntent();
        String setIdToStudy = intent.getStringExtra("SET_ID_TO_STUDY");

        if (setIdToStudy != null) {
            List<FlashcardSet> allSets = storageManager.getAllSets();
            for (FlashcardSet set : allSets) {
                if (set.getId().equals(setIdToStudy)) {
                    currentSet = set;
                    break;
                }
            }
        }

        // Kiểm tra xem Set và thẻ có tồn tại không
        if (currentSet == null || currentSet.getFlashcards() == null || currentSet.getFlashcards().isEmpty()) {
            Toast.makeText(this, "Lỗi: Bộ thẻ này trống hoặc không tồn tại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        flashcards = currentSet.getFlashcards();
        totalCards = flashcards.size(); // Thiết lập tổng số thẻ

        // --- Tải dữ liệu đã lưu ---
        rememberedCardIds = storageManager.getRememberedCards(currentSet.getId());
        int savedIndex = storageManager.getStudyProgress(currentSet.getId());

        // Kiểm tra xem index có hợp lệ không
        if (savedIndex >= 0 && savedIndex < flashcards.size()) {
            currentIndex = savedIndex; // Cập nhật vị trí bắt đầu
        } else {
            currentIndex = 0; // Bắt đầu từ 0
        }

        // --- Thiết lập ban đầu ---
        isTrackingProgress = switchTrackProgress.isChecked();

        // Thiết lập MAX cho Progress Bar
        progressBarUntracked.setMax(totalCards);
        progressBarTracked.setMax(totalCards);

        updateNavigationUI();
        loadFlashcard(currentIndex); // Tải thẻ đầu tiên

        // --- Gán sự kiện Click ---
        flashcardContainer.setOnClickListener(v -> flipCard());

        // Bật/tắt Track Progress
        switchTrackProgress.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTrackingProgress = isChecked;
            updateNavigationUI();
            updateProgressUI(); // Cập nhật text & Progress Bar
            if (isTrackingProgress) {
                rememberedCardIds.clear(); // Reset bộ đếm khi bật tracking
                storageManager.saveRememberedCards(currentSet.getId(), rememberedCardIds);
            }
        });

        // Nút điều hướng khi KHÔNG track (Prev/Next)
        btnPrevUntracked.setOnClickListener(v -> goToPrevCard());
        btnNextUntracked.setOnClickListener(v -> goToNextCard(false)); // False: không đánh dấu là đúng

        // Nút điều hướng khi CÓ track (Wrong/Correct)
        btnWrongTracked.setOnClickListener(v -> goToNextCard(false)); // Sai thì chỉ cần qua thẻ mới
        btnCorrectTracked.setOnClickListener(v -> {
            rememberedCardIds.add(flashcards.get(currentIndex).getId()); // Đánh dấu đã nhớ
            goToNextCard(true); // Qua thẻ mới
        });

        // Auto-play setup
        playHandler = new Handler(getMainLooper());
        initPlayRunnable();

        View.OnClickListener playClickListener = v -> togglePlayMode();
        btnPlayUntracked.setOnClickListener(playClickListener);
        btnPlayTracked.setOnClickListener(playClickListener);

        View.OnClickListener shuffleClickListener = v -> shuffleCards();
        btnShuffleUntracked.setOnClickListener(shuffleClickListener);
        btnShuffleTracked.setOnClickListener(shuffleClickListener);

        ivStar.setOnClickListener(v -> toggleStarStatus());
    }

    // --- Các hàm xử lý ---

    private void loadAnimations() {
        // ... (Giữ nguyên logic loadAnimations)
        flipOutAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_out);
        flipInAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_in);
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);
    }

    private void loadFlashcard(int index) {
        // ... (Giữ nguyên logic loadFlashcard)
        Flashcard fc = flashcards.get(index);
        tvFrontWord.setText(fc.getName());
        tvFrontType.setText(fc.getType() != null && !fc.getType().isEmpty() ? "[" + fc.getType() + "]" : "");
        tvBackMeaning.setText(fc.getMeaning());

        tv_get_hint.setText("💡 Get a hint");
        hintShown = false;

        updateProgressUI();
        resetCardFlip();
        updateStarIcon();
    }

    private void flipCard() {
        // ... (Giữ nguyên logic flipCard)
        if (isFrontVisible) {
            flipOutAnimator.setTarget(cardFront);
            flipInAnimator.setTarget(cardBack);
            flipOutAnimator.start();
            flipInAnimator.start();
            isFrontVisible = false;
        } else {
            flipOutAnimator.setTarget(cardBack);
            flipInAnimator.setTarget(cardFront);
            flipOutAnimator.start();
            flipInAnimator.start();
            isFrontVisible = true;
        }
    }

    /**
     * Chuyển sang thẻ tiếp theo.
     * @param isCorrected Trong chế độ tracked, thẻ hiện tại có được đánh dấu là đúng không.
     */
    private void goToNextCard(boolean isCorrected) {
        if (isCorrected && isTrackingProgress) {
            // Logic đánh dấu đã được xử lý ở setOnClickListener
        }

        currentIndex++;

        if (currentIndex < flashcards.size()) {
            loadFlashcard(currentIndex);
        } else {
            // Đã hết thẻ
            if (isTrackingProgress) {
                finishStudyAndSave();
            } else {
                showCompletionDialog(false);
            }
        }
        // updateProgressUI() được gọi trong loadFlashcard
    }

    // Quay lại thẻ trước đó (chỉ dùng khi không track)
    private void goToPrevCard() {
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = flashcards.size() - 1; // Quay vòng về thẻ cuối
        }
        loadFlashcard(currentIndex);
        // Khi quay lại thẻ trước, nếu thẻ đó đã được nhớ, loại bỏ khỏi rememberedCardIds
        if (isTrackingProgress && rememberedCardIds.contains(flashcards.get(currentIndex).getId())) {
            rememberedCardIds.remove(flashcards.get(currentIndex).getId());
        }
    }

    private void showHint() {
        // ... (Giữ nguyên logic showHint)
        if (flashcards == null || flashcards.isEmpty() || hintShown) return;

        String meaning = flashcards.get(currentIndex).getMeaning();
        if (meaning != null && meaning.length() >= 2) { // Đảm bảo có ít nhất 2 ký tự
            String hintText = "Hint: " + meaning.charAt(0) + meaning.charAt(1) + "...";
            tv_get_hint.setText(hintText);
            tv_get_hint.setVisibility(View.VISIBLE);
            hintShown = true;
        } else if (meaning != null && meaning.length() == 1) {
            String hintText = "Hint: " + meaning.charAt(0) + "...";
            tv_get_hint.setText(hintText);
            tv_get_hint.setVisibility(View.VISIBLE);
            hintShown = true;
        }
    }

    private void updateNavigationUI() {
        // ... (Giữ nguyên logic updateNavigationUI)
        if (isTrackingProgress) {
            navigationTracked.setVisibility(View.VISIBLE);
            navigationUntracked.setVisibility(View.GONE);
            tvRememberedCount.setVisibility(View.VISIBLE);
        } else {
            navigationTracked.setVisibility(View.GONE);
            navigationUntracked.setVisibility(View.VISIBLE);
            tvRememberedCount.setVisibility(View.GONE);
        }
    }

    /**
     * Cập nhật các TextView và ProgressBar hiển thị tiến trình
     */
    private void updateProgressUI() {
        // Tiền xử lý để tránh chia cho 0 nếu danh sách rỗng (đã được kiểm tra trong onCreate)
        if (totalCards == 0) return;

        // Tiến độ hiện tại (1-based index)
        int currentProgress = currentIndex + 1;

        String progressText = currentProgress + " / " + totalCards;

        // 1. Cập nhật TextView
        tvCardProgressUntracked.setText(progressText);
        tvCardProgressTracked.setText(progressText);

        // 2. Cập nhật ProgressBar (Sử dụng giá trị thực tế của currentIndex + 1)
        if (isTrackingProgress) {
            progressBarTracked.setProgress(currentProgress);
            progressBarUntracked.setProgress(0); // Ẩn thanh kia
            tvRememberedCount.setText("Remembered: " + rememberedCardIds.size());
        } else {
            progressBarUntracked.setProgress(currentProgress);
            progressBarTracked.setProgress(0); // Ẩn thanh kia
            tvRememberedCount.setVisibility(View.GONE);
        }
    }

    private void resetCardFlip() {
        // ... (Giữ nguyên logic resetCardFlip)
        isFrontVisible = true;
        cardFront.setVisibility(View.VISIBLE);
        cardBack.setVisibility(View.VISIBLE);
        cardFront.setAlpha(1f);
        cardFront.setRotationY(0f);
        cardBack.setAlpha(0f);
        cardBack.setRotationY(0f);
    }

    private void shuffleCards() {
        if (isPlaying) {
            stopPlayMode();
        }

        Collections.shuffle(flashcards); // Sử dụng import java.util.Collections
        rememberedCardIds.clear(); // Xóa trạng thái nhớ khi xáo trộn

        currentIndex = 0;
        loadFlashcard(currentIndex);

        Toast.makeText(this, "Đã xáo trộn thẻ!", Toast.LENGTH_SHORT).show();
    }

    private void initPlayRunnable() {
        playRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPlaying) return;

                if (isFrontVisible) {
                    flipCard();
                    playHandler.postDelayed(this, DELAY_NEXT_CARD);
                } else {
                    if (currentIndex < flashcards.size() - 1) {
                        // Chuyển thẻ tiếp theo. False vì auto-play không tự đánh dấu là Correct
                        goToNextCard(false);
                        playHandler.postDelayed(this, DELAY_FLIP_TO_BACK);
                    } else {
                        Toast.makeText(StudyActivity.this, "Đã phát hết thẻ!", Toast.LENGTH_SHORT).show();
                        stopPlayMode();
                        currentIndex = 0;
                        loadFlashcard(currentIndex);
                    }
                }
            }
        };
    }

    private void togglePlayMode() {
        if (isPlaying) {
            stopPlayMode();
        } else {
            // Nếu thẻ đang ở mặt sau, lật lại mặt trước trước khi bắt đầu
            if (!isFrontVisible) {
                flipCard();
            }
            startPlayMode();
        }
    }

    private void startPlayMode() {
        isPlaying = true;
        btnPlayUntracked.setImageResource(R.drawable.ic_stop);
        btnPlayTracked.setImageResource(R.drawable.ic_stop);

        disableControlsForPlay(false);

        playHandler.postDelayed(playRunnable, DELAY_FLIP_TO_BACK);
    }

    private void stopPlayMode() {
        isPlaying = false;
        playHandler.removeCallbacks(playRunnable);
        btnPlayUntracked.setImageResource(R.drawable.ic_play);
        btnPlayTracked.setImageResource(R.drawable.ic_play);

        disableControlsForPlay(true);
    }

    private void disableControlsForPlay(boolean enabled) {
        // ... (Giữ nguyên logic disableControlsForPlay)
        btnPrevUntracked.setEnabled(enabled);
        btnNextUntracked.setEnabled(enabled);
        btnShuffleUntracked.setEnabled(enabled);

        btnWrongTracked.setEnabled(enabled);
        btnCorrectTracked.setEnabled(enabled);
        btnShuffleTracked.setEnabled(enabled);

        flashcardContainer.setEnabled(enabled);
        switchTrackProgress.setEnabled(enabled);
        if(tv_get_hint != null) tv_get_hint.setEnabled(enabled);
    }

    private void finishStudyAndSave() {
        // ... (Giữ nguyên logic finishStudyAndSave)
        int totalCards = flashcards.size();
        int rememberedCount = rememberedCardIds.size();

        QuizResult result = new QuizResult(currentSet.getId(), totalCards, rememberedCount);

        List<QuizResult> results = currentSet.getQuizResults();
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(result);
        currentSet.setQuizResults(results);

        storageManager.updateSet(currentSet);
        storageManager.saveStudyProgress(currentSet.getId(), 0);
        storageManager.saveRememberedCards(currentSet.getId(), new HashSet<>());
        showCompletionDialog(true);
    }

    private void showCompletionDialog(boolean wasTracked) {
        // ... (Giữ nguyên logic showCompletionDialog)
        String message;
        if (wasTracked) {
            message = "Bạn đã học xong.\nKết quả: Đã nhớ " + rememberedCardIds.size() + " / " + flashcards.size() + " thẻ.\nXem thống kê";
        } else {
            message = "Bạn đã xem hết các thẻ trong bộ này.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Hoàn thành!")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isPlaying) {
            stopPlayMode();
        }
    }

    private void toggleStarStatus() {
        // ... (Giữ nguyên logic toggleStarStatus)
        if (flashcards == null || flashcards.isEmpty()) return;

        String currentCardId = flashcards.get(currentIndex).getId();
        boolean isCurrentlyStarred = storageManager.isCardStarred(currentCardId);

        if (isCurrentlyStarred) {
            storageManager.removeStarredCard(currentCardId);
            Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            storageManager.addStarredCard(currentCardId);
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        }

        updateStarIcon();
    }

    private void updateStarIcon() {
        // ... (Giữ nguyên logic updateStarIcon)
        if (flashcards == null || flashcards.isEmpty()) return;

        String currentCardId = flashcards.get(currentIndex).getId();

        // Bạn cần đảm bảo các drawable này tồn tại trong dự án của bạn
        if (storageManager.isCardStarred(currentCardId)) {
            ivStar.setImageResource(R.drawable.ic_star_filled_yellow);
        } else {
            ivStar.setImageResource(R.drawable.ic_star_border_grey);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // ... (Giữ nguyên logic onPause)
        if (isPlaying) {
            stopPlayMode();
        }
        if (currentSet != null) {
            storageManager.saveStudyProgress(currentSet.getId(), currentIndex);
            storageManager.saveRememberedCards(currentSet.getId(), rememberedCardIds);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_study;
    }

    @Override
    protected String getHeaderTitle() {
        return "Study Mode";
    }
}