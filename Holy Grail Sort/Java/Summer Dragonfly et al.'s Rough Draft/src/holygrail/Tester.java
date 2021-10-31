package holygrail;

import java.util.Comparator;

interface IntegerPair {
    public Integer getKey();
    public Integer getValue();
}

class GrailPair implements IntegerPair {
    private Integer key;
    private Integer value;
    
    public GrailPair(Integer key, Integer value) {
        this.key = key;
        this.value = value;
    }
    @Override
    public Integer getKey() {
        return this.key;
    }
    @Override
    public Integer getValue() {
        return this.value;
    }
}

class GrailComparator implements Comparator<GrailPair> {
    @Override
    public int compare(GrailPair o1, GrailPair o2) {
        if     (o1.getKey() < o2.getKey()) return -1;
        else if(o1.getKey() > o2.getKey()) return  1;
        else                               return  0;
    }
}

public class Tester {
    public Tester() {
        new HolyGrailSort<GrailPair>(new GrailComparator());
    }

    public static void main(String[] args) {
        new Tester();
    }
}
