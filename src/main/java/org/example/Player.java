package org.example;
public class Player {
    private String name;
    private int games;
    private int points;
    private String team;
    public Player(String name, int games, int points, String team) {
        this.name = name;
        this.games = games;
        this.points = points;
        this.team = team; }
    @Override public String toString() {
        return name + ": " + points + " points, " + games + " games, plays in " + team; }
    public String getName() { return name; }
    public int getGames() { return games; }
    public int getPoints() { return points; }
    public String getTeam() { return team; }
}