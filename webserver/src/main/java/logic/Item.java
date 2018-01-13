package logic;

public class Item implements Comparable<Item>{

    private int index;
    private String value;
    private boolean correct;

    public Item(int index, String value, boolean correct) {
        this.index = index;
        this.value = value;
        this.correct= correct;
    }

    public int getIndex() {
        return index;
    }

    public String getValue() {
        return value;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setValue(String value) {
        this.value = value;

    }

    public boolean isCorrect() {
        return correct;
    }
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    @Override
    public int compareTo(Item o) {
        return this.index - o.getIndex();
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder("Index: "+index);
        sb.append(" value: "+value);
        sb.append(" corect: "+correct);
        sb.append("\n");
        return sb.toString();
    } 
}
