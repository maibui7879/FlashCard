package com.example.flashcard.ui; // Đảm bảo đúng package

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.flashcard.R;
import com.example.flashcard.models.Flashcard;
import com.example.flashcard.models.FlashcardSet;
import com.example.flashcard.models.QuizResult;
import com.example.flashcard.storage.StorageManager;

import java.util.ArrayList; // Thêm import này
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudyActivity extends AppCompatActivity {

    // Khai báo các View từ activity_study.xml
    private TextView tvFrontWord, tvFrontType, tvBackMeaning, tv_get_hint;
    private TextView tvCardProgressUntracked, tvCardProgressTracked, tvRememberedCount;
    private CardView cardFront, cardBack;
    private FrameLayout flashcardContainer;
    private Switch switchTrackProgress;
    private LinearLayout navigationUntracked, navigationTracked;

    // Nút điều hướng
    private Button btnPrevUntracked, btnNextUntracked;
    private Button btnWrongTracked, btnCorrectTracked;

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
    private boolean isTrackingProgress = true;
    private Set<String> rememberedCardIds;

    // Biến cho auto-play
    private Handler playHandler;
    private Runnable playRunnable;
    private boolean isPlaying = false;
    private static final long DELAY_FLIP_TO_BACK = 2000; // 2 giây (thời gian xem mặt trước)
    private static final long DELAY_NEXT_CARD = 1500; // 1.5 giây (thời gian xem mặt sau)

    private Button btnPlayUntracked, btnPlayTracked;
    private Button btnShuffleUntracked, btnShuffleTracked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study); // Load layout bạn đã tạo

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
        btnPrevUntracked = findViewById(R.id.btn_prev_card_untracked);
        btnNextUntracked = findViewById(R.id.btn_next_card_untracked);
        btnWrongTracked = findViewById(R.id.btn_wrong_tracked);
        btnCorrectTracked = findViewById(R.id.btn_correct_tracked);
        btnPlayUntracked = findViewById(R.id.btn_play_untracked);
        btnPlayTracked = findViewById(R.id.btn_play_tracked);
        btnShuffleUntracked = findViewById(R.id.btn_shuffle_untracked);
        btnShuffleTracked = findViewById(R.id.btn_shuffle_tracked);

        // Nút Get a Hint
        findViewById(R.id.tv_get_hint).setOnClickListener(v -> showHint());

        // --- Khởi tạo ---
        storageManager = new StorageManager(this); //
        rememberedCardIds = new HashSet<>();
        loadAnimations(); // Gọi hàm tải animation

        // --- Lấy dữ liệu thẻ ---
        Intent intent = getIntent();
        String setIdToStudy = intent.getStringExtra("SET_ID_TO_STUDY"); // Nhận ID từ SetActivity

        if (setIdToStudy != null) {
            // Dùng StorageManager để lấy Set dựa trên ID
            List<FlashcardSet> allSets = storageManager.getAllSets(); //
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
            finish(); // Đóng activity nếu lỗi
            return;
        }
        flashcards = currentSet.getFlashcards(); //

        // --- Thiết lập ban đầu ---
        isTrackingProgress = switchTrackProgress.isChecked(); // Lấy trạng thái ban đầu
        updateNavigationUI(); // Hiển thị bộ nút điều khiển phù hợp
        loadFlashcard(currentIndex); // Hiển thị thẻ đầu tiên

        // --- Gán sự kiện Click ---
        flashcardContainer.setOnClickListener(v -> flipCard()); // Lật thẻ khi nhấn vào

        // Bật/tắt Track Progress
        switchTrackProgress.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTrackingProgress = isChecked;
            updateNavigationUI(); // Đổi bộ nút
            updateProgressUI(); // Cập nhật text tiến trình
            if (isTrackingProgress) {
                rememberedCardIds.clear(); // Reset bộ đếm khi bật tracking
            }
        });

        //Nút điều hướng khi KHÔNG track
        btnPrevUntracked.setOnClickListener(v -> goToPrevCard());
        btnNextUntracked.setOnClickListener(v -> goToNextCard());

        // Nút điều hướng khi CÓ track
        btnWrongTracked.setOnClickListener(v -> goToNextCard()); // Sai thì chỉ cần qua thẻ mới
        btnCorrectTracked.setOnClickListener(v -> {
            rememberedCardIds.add(flashcards.get(currentIndex).getId()); // Đánh dấu đã nhớ
            goToNextCard(); // Qua thẻ mới
        });

//        // (Các nút khác như Play, Shuffle... hiện chỉ để làm cảnh)
//        findViewById(R.id.btn_play_untracked).setOnClickListener(v -> showToast("Play (Untracked)"));
//        findViewById(R.id.btn_shuffle_untracked).setOnClickListener(v -> showToast("Shuffle (Untracked)"));
//        // ... (tương tự cho các nút _tracked)
// Khởi tạo Handler và gán sự kiện
        playHandler = new Handler(getMainLooper());
        initPlayRunnable(); // Gọi hàm này để tạo Runnable

        // Gán sự kiện cho 2 nút Play
        View.OnClickListener playClickListener = v -> togglePlayMode();
        btnPlayUntracked.setOnClickListener(playClickListener);
        btnPlayTracked.setOnClickListener(playClickListener);

        // Gán sự kiện cho 2 nút Shuffle
        View.OnClickListener shuffleClickListener = v -> shuffleCards();
        btnShuffleUntracked.setOnClickListener(shuffleClickListener);
        btnShuffleTracked.setOnClickListener(shuffleClickListener);
    }

    // --- Các hàm xử lý ---

    // Tải animation từ file animator
    private void loadAnimations() {
        flipOutAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_out);
        flipInAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_flip_in);
        // Thiết lập camera distance để hiệu ứng 3D đẹp hơn
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);
    }

    // Hiển thị dữ liệu của thẻ lên giao diện
    private void loadFlashcard(int index) {
        Flashcard fc = flashcards.get(index); //
        tvFrontWord.setText(fc.getName());
        tvFrontType.setText(fc.getType() != null && !fc.getType().isEmpty() ? "[" + fc.getType() + "]" : "");
        tvBackMeaning.setText(fc.getMeaning());

        tv_get_hint.setText("Get a hint");
        hintShown = false; // Reset trạng thái hint

        updateProgressUI(); // Cập nhật text số thẻ (ví dụ: 1 / 10)
        resetCardFlip(); // Luôn quay về mặt trước
    }

    // Thực hiện animation lật thẻ
    private void flipCard() {
        //tv_get_hint.setVisibility(View.GONE); // Ẩn hint khi lật

        if (isFrontVisible) { // Đang thấy mặt trước -> Lật ra sau
            flipOutAnimator.setTarget(cardFront);
            flipInAnimator.setTarget(cardBack);
            flipOutAnimator.start();
            flipInAnimator.start();
            isFrontVisible = false;
        } else { // Đang thấy mặt sau -> Lật ra trước
            flipOutAnimator.setTarget(cardBack);
            flipInAnimator.setTarget(cardFront);
            flipOutAnimator.start();
            flipInAnimator.start();
            isFrontVisible = true;
        }
    }

    // Chuyển sang thẻ tiếp theo
    private void goToNextCard() {
        currentIndex++; // Tăng chỉ số thẻ
        updateProgressUI(); // Cập nhật số thẻ (ví dụ: 2 / 10)

        if (currentIndex < flashcards.size()) { // Nếu chưa hết thẻ
            loadFlashcard(currentIndex); // Tải thẻ tiếp theo
        } else { // Nếu đã hết thẻ
            if (isTrackingProgress) {
                finishStudyAndSave(); // Lưu kết quả nếu đang track
            } else {
                showCompletionDialog(false); // Chỉ thông báo nếu không track
            }
        }
    }

    // Quay lại thẻ trước đó (chỉ dùng khi không track)
    private void goToPrevCard() {
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = flashcards.size() - 1; // Quay vòng về thẻ cuối
        }
        loadFlashcard(currentIndex); // Tải lại thẻ
    }

    // Hiển thị gợi ý (lấy ký tự đầu của nghĩa)
    private void showHint() {
        if (flashcards == null || flashcards.isEmpty() || hintShown) return;

        String meaning = flashcards.get(currentIndex).getMeaning();
        if (meaning != null && !meaning.isEmpty()) {
            String hintText = "Hint: " + meaning.charAt(0) + meaning.charAt(1)+"...";
            tv_get_hint.setText(hintText);
            tv_get_hint.setVisibility(View.VISIBLE); // Hiện TextView hint
            hintShown = true;
        }
    }

    // Đổi bộ nút điều khiển dựa vào Switch
    private void updateNavigationUI() {
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

    // Cập nhật các TextView hiển thị tiến trình
    private void updateProgressUI() {
        String progressText = (currentIndex + 1) + " / " + flashcards.size();
        tvCardProgressUntracked.setText(progressText);
        tvCardProgressTracked.setText(progressText);

        if (isTrackingProgress) {
            tvRememberedCount.setText("Remembered: " + rememberedCardIds.size());
        }
    }

    private void resetCardFlip() {
        isFrontVisible = true;

        // Luôn để cả 2 thẻ ở trạng thái VISIBLE
        cardFront.setVisibility(View.VISIBLE);
        cardBack.setVisibility(View.VISIBLE);

        // Dùng ALPHA (độ mờ) để ẩn/hiện
        cardFront.setAlpha(1f); // 1f = Hiện rõ
        cardFront.setRotationY(0f);

        cardBack.setAlpha(0f);  // 0f = Ẩn (trong suốt)
        cardBack.setRotationY(0f);
    }

    //Xáo trộn danh sách thẻ hiện tại và quay về thẻ đầu tiên
    private void shuffleCards() {
        if (isPlaying) {
            stopPlayMode(); // Dừng auto-play nếu đang chạy
        }

        // Dùng Collections để xáo trộn danh sách
        java.util.Collections.shuffle(flashcards);

        currentIndex = 0; // Quay về thẻ đầu
        loadFlashcard(currentIndex); // Tải lại thẻ

        Toast.makeText(this, "Đã xáo trộn thẻ!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Khởi tạo vòng lặp (Runnable) cho chế độ tự động phát.
     */
    private void initPlayRunnable() {
        playRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPlaying) return; // Đã bị dừng thủ công

                if (isFrontVisible) {
                    // --- ĐANG Ở MẶT TRƯỚC ---
                    // Lật ra mặt sau
                    flipCard();
                    // Hẹn giờ chạy lại Runnable này sau [DELAY_NEXT_CARD] (để xem mặt sau)
                    playHandler.postDelayed(this, DELAY_NEXT_CARD);
                } else {
                    // --- ĐANG Ở MẶT SAU ---
                    // Kiểm tra xem đây có phải thẻ cuối cùng không
                    if (currentIndex < flashcards.size() - 1) {
                        // Nếu chưa phải thẻ cuối, chuyển thẻ tiếp theo
                        goToNextCard(); // Hàm này sẽ tự động reset về mặt trước
                        // Hẹn giờ chạy lại Runnable này sau [DELAY_FLIP_TO_BACK] (để xem mặt trước)
                        playHandler.postDelayed(this, DELAY_FLIP_TO_BACK);
                    } else {
                        // Nếu ĐÂY LÀ THẺ CUỐI CÙNG
                        Toast.makeText(StudyActivity.this, "Đã phát hết thẻ!", Toast.LENGTH_SHORT).show();
                        stopPlayMode();
                        // Tùy chọn: Quay về thẻ đầu tiên sau khi kết thúc
                        currentIndex = 0;
                        loadFlashcard(currentIndex);
                    }
                }
            }
        };
    }

    //Bật hoặc Tắt chế độ tự động phát
    private void togglePlayMode() {
        if (isPlaying) {
            stopPlayMode();
        } else {
            startPlayMode();
        }
    }

    //Bắt đầu chế độ tự động phát
    private void startPlayMode() {
        isPlaying = true;
        btnPlayUntracked.setText("■"); // Biểu tượng Stop
        btnPlayTracked.setText("■"); // Biểu tượng Stop

        // Khóa các nút điều khiển khác khi đang auto-play
        disableControlsForPlay(false);

        // Bắt đầu vòng lặp (hiển thị mặt trước trong [DELAY_FLIP_TO_BACK] giây)
        playHandler.postDelayed(playRunnable, DELAY_FLIP_TO_BACK);
    }

    //Dừng chế độ tự động phát
    private void stopPlayMode() {
        isPlaying = false;
        playHandler.removeCallbacks(playRunnable); // Hủy mọi lịch hẹn chạy
        btnPlayUntracked.setText("▶"); // Biểu tượng Play
        btnPlayTracked.setText("▶"); // Biểu tượng Play

        // Mở lại các nút điều khiển
        disableControlsForPlay(true);
    }

    //Hàm tiện ích để khóa/mở các nút điều khiển khi đang auto-play
    private void disableControlsForPlay(boolean enabled) {
        // (Untracked)
        btnPrevUntracked.setEnabled(enabled);
        btnNextUntracked.setEnabled(enabled);
        btnShuffleUntracked.setEnabled(enabled);
        // (Tracked)
        btnWrongTracked.setEnabled(enabled);
        btnCorrectTracked.setEnabled(enabled);
        btnShuffleTracked.setEnabled(enabled);
        // Chung
        flashcardContainer.setEnabled(enabled);
        switchTrackProgress.setEnabled(enabled);
        tv_get_hint.setEnabled(enabled);
    }

    // Kết thúc học và LƯU KẾT QUẢ (chỉ khi tracking)
    private void finishStudyAndSave() {
        int totalCards = flashcards.size();
        int rememberedCount = rememberedCardIds.size();

        // Tạo đối tượng kết quả
        QuizResult result = new QuizResult(currentSet.getId(), totalCards, rememberedCount);

        // Lấy danh sách kết quả cũ (nếu có) và thêm kết quả mới
        List<QuizResult> results = currentSet.getQuizResults();
        if (results == null) {
            results = new ArrayList<>(); // Khởi tạo nếu chưa có
        }
        results.add(result);
        currentSet.setQuizResults(results); //

        // Lưu lại Set đã cập nhật vào SharedPreferences
        storageManager.updateSet(currentSet);

        showCompletionDialog(true); // Hiển thị thông báo có kết quả
    }

    // Hiển thị Dialog báo cáo kết quả
    private void showCompletionDialog(boolean wasTracked) {
        String message;
        if (wasTracked) {
            message = "Bạn đã học xong.\nKết quả: Đã nhớ " + rememberedCardIds.size() + " / " + flashcards.size() + " thẻ.";
        } else {
            message = "Bạn đã xem hết các thẻ trong bộ này.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Hoàn thành!")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish()) // Đóng StudyActivity
                .setCancelable(false)
                .show();
    }

    // Hàm tạm để test (xóa sau)
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Luôn dừng auto-play khi thoát khỏi màn hình
        if (isPlaying) {
            stopPlayMode();
        }
    }
}