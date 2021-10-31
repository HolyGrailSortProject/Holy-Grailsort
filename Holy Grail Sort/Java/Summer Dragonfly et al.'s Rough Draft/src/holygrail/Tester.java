package holygrail;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

public class Tester {
    static class SortFailedException extends Exception {
    }

    static interface IntegerPair {
        public Integer getKey();
        public Integer getValue();
    }

    static class GrailPair implements IntegerPair {
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

    static class GrailComparator implements Comparator<GrailPair> {
        @Override
        public int compare(GrailPair o1, GrailPair o2) {
            if     (o1.getKey() < o2.getKey()) return -1;
            else if(o1.getKey() > o2.getKey()) return  1;
            else                               return  0;
        }
    }

    private int seed;

    private GrailPair[] keyArray;
    private GrailPair[] referenceArray;
    private Integer[]   valueArray;

    private String failReason;

    public Tester(int maxLength, int maxKeyCount) {
        this.seed       = 100000001;
        this.keyArray  = new GrailPair[maxLength];
        this.valueArray = new Integer[maxKeyCount];
    }

    private int getRandomNumber(int key) {
        this.seed = (this.seed * 1234565) + 1;
        return (int) (((long) (this.seed & 0x7fffffff) * key) >> 31);
    }

    private void generateTestArray(int start, int length, int keyCount) {
        for(int i = 0; i < keyCount; i++) {
            this.valueArray[i] = 0;
        }

        for(int i = start; i < start + length; i++) {
            if(keyCount != 0) {
                int key = this.getRandomNumber(keyCount);
                this.keyArray[i] = new GrailPair(key, this.valueArray[key]);
                this.valueArray[key]++;
            }
            else {
                this.keyArray[i] = new GrailPair(this.getRandomNumber(1000000000), 0);
            }
        }
    }

    private boolean testArray(int start, int length, GrailComparator test) {
        for(int i = start + 1; i < start + length; i++) {
            int compare = test.compare(this.keyArray[i - 1],
                                       this.keyArray[i    ]);
            if(compare > 0) {
                this.failReason = "testArray[" + (i - 1) + "] and testArray[" + i + "] are out-of-order\n";
                return false;
            }
            else if(compare == 0 && this.keyArray[i - 1].getValue() > this.keyArray[i].getValue()) {
                this.failReason = "testArray[" + (i - 1) + "] and testArray[" + i + "] are unstable\n";
                return false;
            }
            else if(!this.keyArray[i - 1].equals(this.referenceArray[i - 1])) {
                this.failReason = "testArray[" + (i - 1) + "] does not match the reference array\n";
                return false;
            }
        }
        return true;
    }

    private void checkAlgorithm(int start, int length, int keyCount, boolean grailSort, int grailBufferType, String grailStrategy, GrailComparator test) throws SortFailedException {
        this.generateTestArray(start, length, keyCount);
        this.referenceArray = Arrays.copyOf(this.keyArray, start + length);

        String grailType = "w/o External Buffer";
        if(grailBufferType == 1) {
            grailType = "w/ O(1) Buffer     ";
        }
        else if(grailBufferType == 2) {
            grailType = "w/ O(sqrt n) Buffer";
        }

        if(grailSort) {
            System.out.println("\n* Grailsort " + grailType + ", " + grailStrategy + " \n* start = " + start + ", length = " + length + ", unique items = " + keyCount);
        }
        else {
            System.out.println("\n* Arrays.sort (Timsort)  \n* start = " + start + ", length = " + length + ", unique items = " + keyCount);
        }

        long begin;
        long time;

        if(grailSort) {
            HolyGrailSort<GrailPair> grail = new HolyGrailSort<>(test);

            GrailPair[] buffer = null;
            int bufferLen = 0;

            // Grailsort with static buffer
            if(grailBufferType == 1) {
                buffer    = (GrailPair[]) Array.newInstance(this.keyArray.getClass().getComponentType(), HolyGrailSort.STATIC_EXT_BUFFER_LEN);
                bufferLen = HolyGrailSort.STATIC_EXT_BUFFER_LEN;
            }
            // Grailsort with dynamic buffer
            else if(grailBufferType == 2) {
                bufferLen = 1;
                while((bufferLen * bufferLen) < length) {
                    bufferLen *= 2;
                }
                buffer = (GrailPair[]) Array.newInstance(this.keyArray.getClass().getComponentType(), bufferLen);
            }

            begin = System.nanoTime();
            grail.commonSort(this.keyArray, start, length, buffer, bufferLen);
            time = System.nanoTime() - begin;
        }
        else {
            begin = System.nanoTime();
            Arrays.sort(this.keyArray, start, start + length, test);
            time = System.nanoTime() - begin;
        }

        System.out.print("- Sorted in " + time * 1e-6d + "ms...");
        Arrays.sort(this.referenceArray, start, start + length, test);

        boolean success = this.testArray(start, length, test);
        if(success) {
            System.out.print(" and the sort was successful!\n");
        }
        else {
            System.out.print(" but the sort was NOT successful!!\nReason: " + this.failReason);
            throw new SortFailedException();
        }

        // Sometimes the garbage collector wasn't cooperating.
        Arrays.fill(this.keyArray,       null);
        Arrays.fill(this.valueArray,     null);
        Arrays.fill(this.referenceArray, null);
        System.gc();
    }

    private void checkBoth(int start, int length, int keyCount, String grailStrategy, GrailComparator test) throws SortFailedException {
        int tempSeed = this.seed;
        if(!grailStrategy.equals("Opti.Gnome")) {
            for(int i = 0; i < 3; i++) {
                this.checkAlgorithm(start, length, keyCount, true, i, grailStrategy, test);
                this.seed = tempSeed;
            }
        }
        else {
            this.checkAlgorithm(start, length, keyCount, true, 0, grailStrategy, test);
            this.seed = tempSeed;
        }

        this.checkAlgorithm(start, length, keyCount, false, 0, null, test);
    }

    public static void main(String[] args) {
        int maxLength   = 50000000;
        int maxKeyCount = 25000000;

        Tester testClass = new Tester(maxLength, maxKeyCount);
        GrailComparator testCompare = new GrailComparator();

        System.out.println("Warming-up the JVM...");

        try {
            for(int u = 5; u <= (maxLength / 100); u *= 10) {
                for(int v = 2; v <= u && v <= (maxKeyCount / 100); v *= 2) {
                    for(int i = 0; i < 3; i++) {
                        testClass.checkAlgorithm(0, u, v - 1, true, i, "All Strategies", testCompare);
                    }
                }
            }

            System.out.println("\n*** Testing Grailsort against Timsort ***");

            testClass.checkBoth(       0,       15,        4, "Opti.Gnome", testCompare);
            testClass.checkBoth(       0,       15,        8, "Opti.Gnome", testCompare);
            testClass.checkBoth(       7,        8,        4, "Opti.Gnome", testCompare);

            testClass.checkBoth(       0,  1000000,        3, "Strategy 3", testCompare);
            testClass.checkBoth(       0,  1000000,     1023, "Strategy 2", testCompare);
            testClass.checkBoth(       0,  1000000,   500000, "Strategy 1", testCompare);
            testClass.checkBoth(  500000,   500000,        3, "Strategy 3", testCompare);
            testClass.checkBoth(  500000,   500000,      511, "Strategy 2", testCompare);
            testClass.checkBoth(  500000,   500000,   250000, "Strategy 1", testCompare);

            testClass.checkBoth(       0, 10000000,        3, "Strategy 3", testCompare);
            testClass.checkBoth(       0, 10000000,     4095, "Strategy 2", testCompare);
            testClass.checkBoth(       0, 10000000,  5000000, "Strategy 1", testCompare);
            testClass.checkBoth( 5000000,  5000000,        3, "Strategy 3", testCompare);
            testClass.checkBoth( 5000000,  5000000,     2047, "Strategy 2", testCompare);
            testClass.checkBoth( 5000000,  5000000,  2500000, "Strategy 1", testCompare);

            testClass.checkBoth(       0, 50000000,        3, "Strategy 3", testCompare);
            testClass.checkBoth(       0, 50000000,    16383, "Strategy 2", testCompare);
            testClass.checkBoth(       0, 50000000, 25000000, "Strategy 1", testCompare);
            testClass.checkBoth(25000000, 25000000,        3, "Strategy 3", testCompare);
            testClass.checkBoth(25000000, 25000000,     8191, "Strategy 2", testCompare);
            testClass.checkBoth(25000000, 25000000, 12500000, "Strategy 1", testCompare);


            testClass.checkBoth(25000000, 25000000, 12500000, "Strategy 1", testCompare);
            testClass.checkBoth(25000000, 25000000,     8191, "Strategy 2", testCompare);
            testClass.checkBoth(25000000, 25000000,        3, "Strategy 3", testCompare);
            testClass.checkBoth(       0, 50000000, 25000000, "Strategy 1", testCompare);
            testClass.checkBoth(       0, 50000000,    16383, "Strategy 2", testCompare);
            testClass.checkBoth(       0, 50000000,        3, "Strategy 3", testCompare);

            testClass.checkBoth( 5000000,  5000000,  2500000, "Strategy 1", testCompare);
            testClass.checkBoth( 5000000,  5000000,     2047, "Strategy 2", testCompare);
            testClass.checkBoth( 5000000,  5000000,        3, "Strategy 3", testCompare);
            testClass.checkBoth(       0, 10000000,  5000000, "Strategy 1", testCompare);
            testClass.checkBoth(       0, 10000000,     4095, "Strategy 2", testCompare);
            testClass.checkBoth(       0, 10000000,        3, "Strategy 3", testCompare);

            testClass.checkBoth(  500000,   500000,   250000, "Strategy 1", testCompare);
            testClass.checkBoth(  500000,   500000,      511, "Strategy 2", testCompare);
            testClass.checkBoth(  500000,   500000,        3, "Strategy 3", testCompare);
            testClass.checkBoth(       0,  1000000,   500000, "Strategy 1", testCompare);
            testClass.checkBoth(       0,  1000000,     1023, "Strategy 2", testCompare);
            testClass.checkBoth(       0,  1000000,        3, "Strategy 3", testCompare);

            testClass.checkBoth(       7,        8,        4, "Opti.Gnome", testCompare);
            testClass.checkBoth(       0,       15,        8, "Opti.Gnome", testCompare);
            testClass.checkBoth(       0,       15,        4, "Opti.Gnome", testCompare);

            System.out.println("\nAll tests passed successfully!!");
        } catch (SortFailedException e) {
            System.out.println("\nTesting failed!!\n");
            System.exit(1);
        }
    }
}
