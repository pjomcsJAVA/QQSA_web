package logic;

public class Item implements Comparable<Item>{

    int index;

    String value;

    public Item(int index, String value) {
        this.index = index;
        this.value = value;
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

    @Override
    public int compareTo(Item o) {

        return this.index - o.getIndex();
    }
}
