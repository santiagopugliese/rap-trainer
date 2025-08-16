package com.example.raptrainer.options;

import android.content.Context;
import android.app.AlertDialog;

import java.util.List;

public class ThemeSelector {

    public interface OnThemeSelectedListener {
        void onThemeSelected(String selectedTheme);
    }

    private final Context context;
    private final List<String> themes;

    public ThemeSelector(Context context, List<String> themes) {
        this.context = context;
        this.themes = themes;
    }

    public void show(OnThemeSelectedListener listener) {
        String[] themeArray = themes.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Elegir temática");
        builder.setItems(themeArray, (dialog, which) -> {
            String selectedTheme = themeArray[which];
            listener.onThemeSelected(selectedTheme);
        });

        builder.create().show();
    }
}
