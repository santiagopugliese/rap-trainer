package com.example.raptrainer.options;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

import com.example.raptrainer.R;
import com.example.raptrainer.word_manager.WordManager;

public class CategorySelectorDialog {
    private final WordManager wordManager;

    public CategorySelectorDialog(WordManager wordManager) {
        this.wordManager = wordManager;
    }

    public void show(Context context, Runnable onCategoriesUpdated) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.select_categories)); // Localized title

        builder.setMultiChoiceItems(
                wordManager.getButtonLabels(),
                wordManager.getSelectedCategories(),
                (dialog, index, isChecked) -> wordManager.getSelectedCategories()[index] = isChecked // Update state directly
        );

        builder.setNeutralButton(context.getString(R.string.select_all), (dialog, which) -> {
            wordManager.selectAllCategories();
            wordManager.updateSelectedCategories(); // Apply changes
            if (onCategoriesUpdated != null) onCategoriesUpdated.run();
        });

        builder.setNegativeButton(context.getString(R.string.deselect_all), (dialog, which) -> {
            wordManager.deselectAllCategories();
            wordManager.updateSelectedCategories(); // Apply changes
            if (onCategoriesUpdated != null) onCategoriesUpdated.run();
        });

        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            wordManager.updateSelectedCategories(); // Apply changes
            if (onCategoriesUpdated != null) onCategoriesUpdated.run();
        });

        builder.create().show();
    }
}
