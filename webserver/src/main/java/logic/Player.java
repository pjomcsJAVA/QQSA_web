package logic;

public class Player implements Comparable<Player>{

    private String name;

    private int score;

    public Player(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public Player(String name) {
        this.name = name;
    }

    public Player() {
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void incrementScore(){
        score++;
    }


    @Override
    public int compareTo(Player o) {
        int compareScore = o.getScore();
        return compareScore - this.score;
    }
}
