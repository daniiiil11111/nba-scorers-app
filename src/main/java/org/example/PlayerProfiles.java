package org.example;

public class PlayerProfiles {

    public static String slugFor(Player player) {
        return player.getName()
                .toLowerCase()
                .replace(".", "")
                .replace("'", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    public static String imagePath(Player player) {
        return "/players/" + slugFor(player) + ".png";

    }
}