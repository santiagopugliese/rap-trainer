package com.example.raptrainer.word_manager;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages word categories, caching words from CSV files at startup,
 * and handling word selection logic for display.
 */
public class WordManager {
    private static final int DEFAULT_WORD_DISPLAY_DELAY = 5000;

    private final List<String> themesList = new ArrayList<>();
    private final List<String> activeCategoryFiles = new ArrayList<>();
    private final List<String> wordList = new ArrayList<>();
    private final List<String> currentWordQueue = new ArrayList<>();

    // Static cache: category folder -> list of words
    private static final Map<String, List<String>> wordsCache = new HashMap<>();

    private String[] buttonLabels;
    private boolean[] selectedCategories;
    private int currentWordIndex = 0;
    private int wordDisplayDelay = DEFAULT_WORD_DISPLAY_DELAY;
    private boolean allowRepeat = false;

    /** Initializes category setup and loads theme words. */
    public void initialize(Context context) {
        preloadWords(context);
        initializeCategories();
        shuffleWords();

        loadThemes(context);
    }

    /**
     * Loads all category folders into cache.
     * Call this once at app startup or before usage.
     */
    public static void preloadWords(Context context) {
        wordsCache.clear();
        Map<String, List<String>> words = FileLoader.readWordsGroupedByFile(context);
        wordsCache.putAll(words);
        System.out.println("wordsCache: " + wordsCache);
    }

    public void initializeCategories() {
        List<String> cachedCategories = new ArrayList<>(wordsCache.keySet());

        selectedCategories = new boolean[cachedCategories.size()];
        selectedCategories[0] = true;

        activeCategoryFiles.clear();
        activeCategoryFiles.add(cachedCategories.get(0));

        buttonLabels = cachedCategories.stream()
                .map(this::extractButtonLabel)
                .toArray(String[]::new);

        loadCategoryWordsFromCache();
        shuffleWords();
    }

    /**
     * Updates selected category files and reloads from cached words.
     */
    public void updateSelectedCategories() {
        List<String> cachedCategories = new ArrayList<>(wordsCache.keySet());

        activeCategoryFiles.clear();
        for (int i = 0; i < selectedCategories.length; i++) {
            if (selectedCategories[i]) {
                activeCategoryFiles.add(cachedCategories.get(i));
            }
        }

        loadCategoryWordsFromCache();
        shuffleWords();
    }

    /** Returns the next word in the shuffled queue, reshuffling if needed. */
    public String getNextWord() {
        if (currentWordIndex < currentWordQueue.size()) {
            return currentWordQueue.get(currentWordIndex++);
        }
        if (allowRepeat) {
            resetAndShuffle();
            return getNextWord();
        }
        return null;
    }

    /** Resets the word index and reshuffles the queue. */
    public void resetAndShuffle() {
        currentWordIndex = 0;
        shuffleWords();
    }

    public void setWordDisplayDelay(int delay) {
        this.wordDisplayDelay = delay;
    }

    public int getWordDisplayDelay() {
        return wordDisplayDelay;
    }

    public boolean isEmpty() {
        return currentWordIndex >= currentWordQueue.size();
    }

    public boolean isAllowRepeat() {
        return allowRepeat;
    }

    public void setAllowRepeat(boolean allowRepeat) {
        this.allowRepeat = allowRepeat;
    }

    public String[] getButtonLabels() {
        return buttonLabels;
    }

    public boolean[] getSelectedCategories() {
        return selectedCategories;
    }

    public void selectAllCategories() {
        Arrays.fill(selectedCategories, true);
    }

    public void deselectAllCategories() {
        Arrays.fill(selectedCategories, false);
    }

    public List<String> getThemesList() {
        return new ArrayList<>(themesList);
    }

    // ===================== Internal Logic =====================

    /** Loads all words for the selected categories from the cache. */
    private void loadCategoryWordsFromCache() {
        wordList.clear();
        for (String category : activeCategoryFiles) {
            List<String> cachedWords = wordsCache.get(category);
            if (cachedWords != null) {
                wordList.addAll(cachedWords);
            }
        }
    }

    /** Loads theme-related words from the theme CSV file. */
    private void loadThemes(Context context) {
        themesList.clear();
        themesList.addAll(FileLoader.readWordsFromAssetPath(context, CategoryConstants.THEME_CSV_FILE));
    }

    /** Shuffles the selected word list and resets the queue. */
    private void shuffleWords() {
        currentWordQueue.clear();
        currentWordQueue.addAll(wordList);
        Collections.shuffle(currentWordQueue);
        currentWordIndex = 0;
    }

    /** Extracts a UI-friendly label from a file name. */
    private String extractButtonLabel(String fileName) {
        String[] parts = fileName.split("_");
        if (parts.length > 2) {
            String categoryPart = parts[2];
            String formattedCategory = categoryPart.replace("1", "/");
            String response = parts[1] + " (" + formattedCategory + ") ";
            if (parts.length == 4) {
                response += parts[3];
            }
            return response;
        }
        return parts[1];
    }
}
