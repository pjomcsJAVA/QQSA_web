package logic;

public class Item implements Comparable<Item>{

    private int index;
    private String value;
    private boolean correct;
    private int count;
    private int totalCount;
    private boolean show;



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
    
    
    public int getCount() {
        return count;
    }
    
    public String getCountStr() {
        if(!show){
            return "";
        }
        double perc = 1.0*count*100/totalCount;
        perc = round(perc, 1);
        return String.valueOf(perc);
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    public boolean isShow() {
    return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
    
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public void incrementCount(){
        this.count++;
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
    
    private double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
