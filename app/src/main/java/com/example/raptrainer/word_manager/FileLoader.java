package com.example.raptrainer.word_manager;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileLoader {

    public static Map<String, List<String>> readWordsGroupedByFile(Context context) {
        Map<String, List<String>> wordsByFile = new HashMap<>();
        try {
            List<String> csvPaths = getAllCsvFilePaths(context.getAssets(), CategoryConstants.BASE_PATH);
            System.out.println("CSV paths: " + csvPaths);

            for (String path : csvPaths) {
                List<String> words = readWordsFromAssetPath(context, path);

                // Use filename without ".csv" as the key
                String fileKey = path.substring(path.lastIndexOf('/') + 1).replace(".csv", "");
                wordsByFile.put(fileKey, words);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wordsByFile;
    }


    /** Reads words from a single CSV file path */
    public static List<String> readWordsFromAssetPath(Context context, String assetPath) {
        List<String> result = new ArrayList<>();
        try (InputStream inputStream = context.getAssets().open(assetPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            System.out.println("Reading file: " + assetPath);
            reader.lines()
                    .flatMap(line -> Arrays.stream(line.split(",\\s*")))
                    .map(String::trim)
                    .filter(word -> !word.isEmpty())
                    .forEach(result::add);
        } catch (IOException e) {
            System.err.println("Error reading file: " + assetPath);
            e.printStackTrace();
        }

        return result;
    }

    /** Recursive helper to find all CSV file paths under a folder */
    private static List<String> getAllCsvFilePaths(AssetManager assetManager, String path) throws IOException {
        List<String> filePaths = new ArrayList<>();
        String[] list = assetManager.list(path);
        if (list == null) return filePaths;
        System.out.println("path: "+path);

        for (String name : list) {
            String fullPath = path.isEmpty() ? name : path + "/" + name;
            String[] subList = assetManager.list(fullPath);

            if (subList != null && subList.length > 0) {
                // It's a folder — recurse
                filePaths.addAll(getAllCsvFilePaths(assetManager, fullPath));
            } else if (name.endsWith(".csv")) {
                System.out.println("Found CSV: " + fullPath);
                filePaths.add(fullPath);
            }
        }
        return filePaths;
    }
}
