package org.example;

import java.util.*;

public class Features {
    private static final ArrayList<Player> players = FileScanner.loadPlayersFromFile();

    public static List<Player> getFullList() {
        return players;
    }

    public static List<Player> getTop5() {
        return players.subList(0, Math.min(5, players.size()));
    }

    public static List<Player> searchForPlayer(String name) {
        List<Player> result = new ArrayList<>();
        for (Player p : players) {
            if (p.getName().toLowerCase().contains(name.toLowerCase())) {
                result.add(p);
            }
        }
        return result;
    }

    public static List<Player> getRankedPlayer(int rank) {
        int index = rank - 1;
        if (index < 0 || index >= players.size()) {
            return Collections.emptyList();
        }
        return List.of(players.get(index));
    }

    public static List<Player> getFavorites(Set<String> names) {
        List<Player> result = new ArrayList<>();
        for (Player p : players) {
            if (names.contains(p.getName())) result.add(p);
        }
        return result;
    }

    public static List<String> getDistinctTeams() {
        List<String> teams = new ArrayList<>();
        for (Player p : players) {
            if (!teams.contains(p.getTeam())) teams.add(p.getTeam());
        }
        Collections.sort(teams);
        return teams;
    }

    public static int getRankOf(Player player) {
        return players.indexOf(player) + 1;
    }
}