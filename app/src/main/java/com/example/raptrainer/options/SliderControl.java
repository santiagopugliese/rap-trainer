package com.example.raptrainer.options;

import android.widget.SeekBar;
import android.widget.TextView;

import com.example.raptrainer.word_manager.WordManager;

public class SliderControl {
    private final SeekBar slider;
    private final TextView sliderValueText;
    private final WordManager wordManager;

    private static final int MIN_DELAY = 1000; // 1 second
    private static final int MAX_DELAY = 30000; // 30 seconds
    private int currentDelay = MIN_DELAY;

    public SliderControl(SeekBar slider, TextView sliderValueText, WordManager wordManager) {
        this.slider = slider;
        this.sliderValueText = sliderValueText;
        this.wordManager = wordManager;
        initialize();
    }

    private void initialize() {
        final int minSeconds = 1;
        final int maxSeconds = 30;

        slider.setMax(maxSeconds - minSeconds);

        // Convert current delay in ms to seconds for SeekBar
        int currentSeconds = wordManager.getWordDisplayDelay() / 1000;
        slider.setProgress(currentSeconds - minSeconds);
        updateSliderValueText(currentSeconds);

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int selectedSeconds = minSeconds + progress;
                wordManager.setWordDisplayDelay(selectedSeconds * 1000); // convert to ms
                updateSliderValueText(selectedSeconds);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateSliderValueText(int seconds) {
        sliderValueText.setText("Delay: " + seconds + "s");
    }

    public int getCurrentDelay() {
        return currentDelay;
    }
}
