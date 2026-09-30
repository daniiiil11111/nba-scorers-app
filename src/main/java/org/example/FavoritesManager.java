package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

public class FavoritesManager {
    private static final Path FILE = Paths.get("favorites.txt");
    private static final Set<String> favorites = load();

    private static Set<String> load() {
        Set<String> set = new LinkedHashSet<>();
        if (Files.exists(FILE)) {
            try {
                for (String line : Files.readAllLines(FILE)) {
                    if (!line.isBlank()) set.add(line.trim());
                }
            } catch (IOException ignored) {
            }
        }
        return set;
    }

    private static void save() {
        try {
            Files.write(FILE, favorites);
        } catch (IOException ignored) {
        }
    }

    public static boolean isFavorite(String name) {
        return favorites.contains(name);
    }

    public static void toggle(String name) {
        if (favorites.contains(name)) favorites.remove(name);
        else favorites.add(name);
        save();
    }

    public static Set<String> all() {
        return favorites;
    }
}