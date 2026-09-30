package org.example;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
public class FileScanner {
    private static ArrayList<Player> players = new ArrayList<>();
    public static ArrayList<Player> loadPlayersFromFile() {
        try (Scanner scanner = new Scanner(Paths.get("stats.txt"))) {
            while (scanner.hasNextLine()) { String line = scanner.nextLine();
                String[] parts = line.split(",");
                players.add(new Player(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), parts[3])); }
        } catch (Exception e) { // silently ignored, matching original behavior
            } return players; } }