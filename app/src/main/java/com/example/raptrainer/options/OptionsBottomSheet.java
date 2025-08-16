package com.example.raptrainer.options;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.raptrainer.R;
import com.example.raptrainer.word_manager.WordManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class OptionsBottomSheet extends BottomSheetDialogFragment {

    private WordManager wordManager;
    private Runnable onWordRefresh;
    private DismissListener dismissListener;
    private DelayChangeListener delayChangeListener;


    public void setWordManager(WordManager manager) {
        this.wordManager = manager;
    }

    public void setOnWordRefresh(Runnable runnable) {
        this.onWordRefresh = runnable;
    }

    public void setDismissListener(DismissListener listener) {
        this.dismissListener = listener;
    }

    public void setDelayChangeListener(DelayChangeListener listener) {
        this.delayChangeListener = listener;
    }


    public interface DismissListener {
        void onDismiss();
    }

    public interface DelayChangeListener {
        void onDelayChanged(int delayMillis);
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onDismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.bottom_sheet_options, container, false);

        setupCategorySelector(view);
        setupThemeSelector(view);

        return view;
    }

    private void setupCategorySelector(View view) {
        // Assuming your category_selector_layout is clickable or you can add a button wrapper
        view.findViewById(R.id.selectCategoriesButton).setOnClickListener(v -> {
            CategorySelectorDialog categoryDialog = new CategorySelectorDialog(wordManager);
            categoryDialog.show(requireActivity(), () -> {
                wordManager.resetAndShuffle();
                if (onWordRefresh != null) onWordRefresh.run();
            });
        });
    }


    private void setupThemeSelector(View view) {
        Button themeButton = view.findViewById(R.id.themeSelector);
        TextView themeText = view.findViewById(R.id.themeTextView);
        ThemeSelector themeSelector = new ThemeSelector(requireContext(), wordManager.getThemesList());

        themeButton.setOnClickListener(v ->
                themeSelector.show(selectedWord -> themeText.setText(selectedWord))
        );
    }
}
