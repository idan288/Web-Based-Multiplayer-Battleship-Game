package servlets;


public class PlayerInfo {
    private String name;
    private int score;
    private int playerIndex;

    public PlayerInfo(String name, int score, int playerIndex) {
        this.name = name;
        this.score = score;
        this.playerIndex = playerIndex;
    }

    public int getScore() {
        return score;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public String getName() {
        return name;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
