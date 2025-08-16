package com.example.raptrainer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;

import com.example.raptrainer.word_manager.WordManager;
import com.example.raptrainer.options.OptionsBottomSheet;

public class MainActivity extends AppCompatActivity {

    private static final float GUIDELINE_UP_PERCENT = 0.25f;
    private static final float GUIDELINE_CENTER_PERCENT = 0.5f;
    private static final float WORD_CENTER_PERCENT_WITH_BUTTONS = 0.4f;
    private static final float WORD_CENTER_PERCENT_ALONE = 0.5f;

    private TextView wordTextView;
    private Guideline centerGuideline;
    private ConstraintLayout rootLayout;
    private ImageButton pausePlayButton;
    private ImageView repeatButton;
    private Button resetButton;
    private GestureDetector gestureDetector;
    private ImageButton optionsButton;
    private LinearLayout sliderTool;

    private boolean isPaused = false;
    private boolean wasPausedBeforeOptions = false;
    private boolean areButtonsVisible = true;

    private final Integer GrayCode = 0xFF2F2F2F;
    private final WordManager wordManager = new WordManager();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Runnable to display next word - reusable and easy to cancel
    private final Runnable wordLoopRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPaused) {
                displayNextWord();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        rootLayout.setOnClickListener(v -> {});

        applyCustomFont();
        initializeWordManager();
        setupUIListeners();
        setupSlider();
        repeatButton.setColorFilter(GrayCode);
        setWordCentered(false);

        displayNextWord();
    }

    private void bindViews() {
        rootLayout = findViewById(R.id.root_layout);
        centerGuideline = findViewById(R.id.centerGuideline);
        wordTextView = findViewById(R.id.wordTextView);
        pausePlayButton = findViewById(R.id.pausePlayButton);
        repeatButton = findViewById(R.id.repeatButton);
        optionsButton = findViewById(R.id.optionsButton);
        resetButton = findViewById(R.id.resetButton);
        sliderTool = findViewById(R.id.sliderTool);
    }

    private void applyCustomFont() {
        Typeface bebasFont = Typeface.createFromAsset(getAssets(), "fonts/BebasNeue-Regular.ttf");
        wordTextView.setTypeface(bebasFont);
    }

    private void initializeWordManager() {
        wordManager.initialize(this);
    }

    private void setupUIListeners() {
        // Click to open options menu and pause word cycling
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                if (Math.abs(diffY) > Math.abs(diffX)) {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            // Swipe down → hide buttons
                            if (areButtonsVisible) {
                                setButtonsVisible(false);
                                areButtonsVisible = false;
                            }
                            return true;
                        } else {
                            // Swipe up → show buttons
                            if (!areButtonsVisible) {
                                setButtonsVisible(true);
                                areButtonsVisible = true;
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        rootLayout.setOnTouchListener((v, event) -> {
            boolean gestureDetected = gestureDetector.onTouchEvent(event);
            return gestureDetected; // Consume only if swipe detected, otherwise pass through
        });

        pausePlayButton.setOnClickListener(v -> {
            if (wordManager.isEmpty()) {
                Log.d("MainActivity", "Word list is empty. Prompting reset.");

                Toast.makeText(this, "No hay más palabras, presione reset para continuar.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isPaused) {
                resumeWordLoop();
                pausePlayButton.setImageResource(R.drawable.ic_pause);
            } else {
                pauseWordLoop();
                pausePlayButton.setImageResource(R.drawable.ic_play);
            }
        });

        repeatButton.setOnClickListener(v -> toggleRepeat());
        optionsButton.setOnClickListener(v -> showOptionsBottomSheet());

        resetButton.setOnClickListener(v -> {
            wordManager.resetAndShuffle();
            displayNextWord();
        });
    }

    private void showOptionsBottomSheet() {
        wasPausedBeforeOptions = isPaused;
        pauseWordLoop(); // stop word loop, but don't touch the icon yet

        OptionsBottomSheet sheet = new OptionsBottomSheet();
        sheet.setWordManager(wordManager);
        sheet.setOnWordRefresh(this::displayNextWord);
        sheet.setDismissListener(() -> {
            moveWordCenter();
            if (!wasPausedBeforeOptions) {
                resumeWordLoop();
                pausePlayButton.setImageResource(R.drawable.ic_pause); // resume & set pause icon
            } else {
                // just update the icon to reflect the paused state
                pausePlayButton.setImageResource(R.drawable.ic_play);
            }
        });

        moveWordUp();
        sheet.show(getSupportFragmentManager(), sheet.getTag());
    }


    private void setButtonsVisible(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;

        // Fade animation
        View[] buttonViews = { pausePlayButton, repeatButton, resetButton, optionsButton, sliderTool };
        for (View v : buttonViews) {
            v.animate()
                    .alpha(show ? 1f : 0f)
                    .setDuration(250)
                    .withStartAction(() -> {
                        if (show) v.setVisibility(View.VISIBLE);
                    })
                    .withEndAction(() -> {
                        if (!show) v.setVisibility(View.GONE);
                    });
        }

        setWordCentered(!show); // move word to center when buttons are hidden
    }

    private void setupSlider() {
        SeekBar slider = findViewById(R.id.timeSlider);
        TextView label = findViewById(R.id.sliderLabel);

        final int minSeconds = 1;
        final int maxSeconds = 30;

        int currentSeconds = wordManager.getWordDisplayDelay() / 1000;

        slider.setMax(maxSeconds - minSeconds);
        slider.setProgress(currentSeconds - minSeconds);
        label.setText("Tiempo entre palabras: " + currentSeconds + "s");

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int selectedSeconds = minSeconds + progress;
                int delayMillis = selectedSeconds * 1000;

                label.setText("Tiempo entre palabras: " + selectedSeconds + "s");

                wordManager.setWordDisplayDelay(delayMillis);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setWordCentered(boolean centered) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);

        float percent = centered ? WORD_CENTER_PERCENT_ALONE : WORD_CENTER_PERCENT_WITH_BUTTONS;
        constraintSet.setGuidelinePercent(centerGuideline.getId(), percent);

        TransitionManager.beginDelayedTransition(rootLayout);
        constraintSet.applyTo(rootLayout);
    }

    private void displayNextWord() {
        String word = wordManager.getNextWord();
        if (word != null) {
            wordTextView.setText(word);

            // Reset any translation or alignment changes
            resetWordTextViewTransformations();

            scheduleNextWordDisplay();
        } else {
            pauseWordLoop();
            pausePlayButton.setImageResource(R.drawable.ic_play); // 👈 show play icon
        }
    }

    private void resetWordTextViewTransformations() {
        wordTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        wordTextView.setTranslationX(0);
        wordTextView.setTranslationY(0);
    }

    private void scheduleNextWordDisplay() {
        handler.removeCallbacks(wordLoopRunnable);
        handler.postDelayed(wordLoopRunnable, wordManager.getWordDisplayDelay());
    }

    private void pauseWordLoop() {
        isPaused = true;
        handler.removeCallbacks(wordLoopRunnable);
    }

    private void resumeWordLoop() {
        isPaused = false;
        scheduleNextWordDisplay();
    }

    private void toggleRepeat() {
        boolean newRepeatState = !wordManager.isAllowRepeat();
        wordManager.setAllowRepeat(newRepeatState);

        // Update the loop icon color based on state
        int color = newRepeatState ? Color.WHITE : GrayCode;
        repeatButton.setColorFilter(color);

        Toast.makeText(this, newRepeatState ? "Repeat Enabled" : "Repeat Disabled", Toast.LENGTH_SHORT).show();
    }

    private void animateWordPosition(float guidelinePercent) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);
        constraintSet.setGuidelinePercent(centerGuideline.getId(), guidelinePercent);
        TransitionManager.beginDelayedTransition(rootLayout);
        constraintSet.applyTo(rootLayout);
    }

    private void moveWordUp() {
        animateWordPosition(GUIDELINE_UP_PERCENT);
    }

    private void moveWordCenter() {
        animateWordPosition(GUIDELINE_CENTER_PERCENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);  // Prevent leaks
    }
}
