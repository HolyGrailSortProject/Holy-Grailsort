package holygrail;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

public class Tester {
    final static class RewrittenGrailsort<K> {
        public static final Class<?> RGS_CLASS;
        private static final Constructor<?> RGS_CONSTRUCTOR;
        private static final Method RGS_COMMON_SORT, RGS_SORT_IN_PLACE, RGS_SORT_STATIC_OOP, RGS_SORT_DYNAMIC_OOP;
        private final Object instance;

        static {
            System.out.print("Checking for Rewritten Grail Sort class... ");
            Class<?> rgsClass = null;
            Constructor<?> rgsConstructor = null;
            Method rgsCommonSort = null,
                   rgsSortInPlace = null,
                   rgsSortStaticOOP = null,
                   rgsSortDynamicOOP = null;
            try {
                rgsClass = Class.forName("sort.GrailSort");
                rgsConstructor = rgsClass.getDeclaredConstructor(Comparator.class);
                rgsCommonSort = rgsClass.getDeclaredMethod("grailCommonSort", Array.class, int.class, int.class, Array.class, int.class);
                rgsSortInPlace = rgsClass.getDeclaredMethod("grailSortInPlace", Array.class, int.class, int.class);
                rgsSortStaticOOP = rgsClass.getDeclaredMethod("grailSortStaticOOP", Array.class, int.class, int.class);
                rgsSortDynamicOOP = rgsClass.getDeclaredMethod("grailSortDynamicOOP", Array.class, int.class, int.class);
                System.out.println("found " + rgsClass);
            } catch (ReflectiveOperationException e) {
                System.out.println("not found");
            }
            RGS_CLASS = rgsClass;
            RGS_CONSTRUCTOR = rgsConstructor;
            RGS_COMMON_SORT = rgsCommonSort;
            RGS_SORT_IN_PLACE = rgsSortInPlace;
            RGS_SORT_STATIC_OOP = rgsSortStaticOOP;
            RGS_SORT_DYNAMIC_OOP = rgsSortDynamicOOP;
        }

        public RewrittenGrailsort(Comparator<K> cmp) throws ReflectiveOperationException {
            if (RGS_CLASS == null) {
                throw new ClassNotFoundException("sort.GrailSort");
            }
            this.instance = RGS_CONSTRUCTOR.newInstance(cmp);
        }

        public void grailCommonSort(K[] array, int start, int length, K[] extBuffer, int extBufferLen) throws ReflectiveOperationException {
            RGS_COMMON_SORT.invoke(this.instance, array, start, length, extBuffer, extBufferLen);
        }

        public void grailSortInPlace(K[] array, int start, int length) throws ReflectiveOperationException {
            RGS_SORT_IN_PLACE.invoke(this.instance, array, start, length);
        }

        public void grailSortStaticOOP(K[] array, int start, int length) throws ReflectiveOperationException {
            RGS_SORT_STATIC_OOP.invoke(this.instance, array, start, length);
        }

        public void grailSortDynamicOOP(K[] array, int start, int length) throws ReflectiveOperationException {
            RGS_SORT_DYNAMIC_OOP.invoke(this.instance, array, start, length);
        }
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
    private int maxLength, maxKeyCount;

    private GrailPair[] keyArray;
    private GrailPair[] referenceArray;
    private Integer[]   valueArray;

    private String failReason;
    private int count, successes, failures;

    public Tester(int maxLength, int maxKeyCount) {
        this.seed        = 100000001;
        this.maxLength   = maxLength;
        this.maxKeyCount = maxKeyCount;
        initArrays();
    }

    private void initArrays() {
        this.keyArray  = new GrailPair[this.maxLength];
        this.valueArray = new Integer[this.maxKeyCount];
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

    private void checkAlgorithm(int start, int length, int keyCount, boolean grailSort, int grailBufferType, String grailStrategy, GrailComparator test) {
        try {
            checkAlgorithm0(start, length, keyCount, grailSort, grailBufferType, grailStrategy, test);
        } catch (OutOfMemoryError e) {
            System.err.println("Warning: tester ran out of memory");
            e.printStackTrace();
            System.err.println("Purging data...");
            keyArray = null;
            referenceArray = null;
            valueArray = null;
            System.gc();
            System.err.println("Re-initializing arrays...");
            initArrays();
            System.err.println("Re-running check...");
            checkAlgorithm(start, length, keyCount, grailSort, grailBufferType, grailStrategy, test);
        }
        catch (Exception e) {
            System.out.println("Sort failed with exception:");
            e.printStackTrace();
            this.failures++;
            this.count++;
        }
    }

    private void checkAlgorithm0(int start, int length, int keyCount, boolean grailSort, int grailBufferType, String grailStrategy, GrailComparator test) {
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
            System.out.println("\n* Holy Grail Sort " + grailType + ", " + grailStrategy + " \n* start = " + start + ", length = " + length + ", unique items = " + keyCount);
        }
        else {
            System.out.println("\n* Arrays.sort (Tim Sort)  \n* start = " + start + ", length = " + length + ", unique items = " + keyCount);
        }

        long begin;
        long time;

        if(grailSort) {
            HolyGrailSort<GrailPair> grail = new HolyGrailSort<>(test);

            GrailPair[] buffer = null;
            int bufferLen = 0;

            // Holy Grail Sort with static buffer
            if(grailBufferType == 1) {
                buffer    = (GrailPair[]) Array.newInstance(this.keyArray.getClass().getComponentType(), HolyGrailSort.STATIC_EXT_BUFFER_LEN);
                bufferLen = HolyGrailSort.STATIC_EXT_BUFFER_LEN;
            }
            // Holy Grail Sort with dynamic buffer
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
            this.successes++;
        }
        else {
            System.out.print(" but the sort was NOT successful!!\nReason: " + this.failReason);
            this.failures++;
        }
        this.count++;

        // Sometimes the garbage collector wasn't cooperating.
        Arrays.fill(this.keyArray,       null);
        Arrays.fill(this.valueArray,     null);
        Arrays.fill(this.referenceArray, null);
        System.gc();
    }

    private void checkBoth(int start, int length, int keyCount, String grailStrategy, GrailComparator test) {
        int tempSeed = this.seed;
        if(!grailStrategy.equals("Opti.Gnome")) {
            for(int i = 0; i < 1; i++) {
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

        Tester tester = new Tester(maxLength, maxKeyCount);
        GrailComparator testCompare = new GrailComparator();

        System.out.println("Warming-up the JVM...");

        for(int u = 5; u <= (maxLength / 100); u *= 10) {
            for(int v = 2; v <= u && v <= (maxKeyCount / 100); v *= 2) {
                for(int i = 0; i < 1; i++) {
                    tester.checkAlgorithm(0, u, v - 1, true, i, "All Strategies", testCompare);
                }
            }
        }

        System.out.println("\n*** Testing Holy Grail Sort against Tim Sort ***");

        tester.checkBoth(       0,       15,        4, "Opti.Gnome", testCompare);
        tester.checkBoth(       0,       15,        8, "Opti.Gnome", testCompare);
        tester.checkBoth(       7,        8,        4, "Opti.Gnome", testCompare);

        tester.checkBoth(       0,  1000000,        3, "Strategy 3", testCompare);
        tester.checkBoth(       0,  1000000,     1023, "Strategy 2", testCompare);
        tester.checkBoth(       0,  1000000,   500000, "Strategy 1", testCompare);
        tester.checkBoth(  500000,   500000,        3, "Strategy 3", testCompare);
        tester.checkBoth(  500000,   500000,      511, "Strategy 2", testCompare);
        tester.checkBoth(  500000,   500000,   250000, "Strategy 1", testCompare);

        tester.checkBoth(       0, 10000000,        3, "Strategy 3", testCompare);
        tester.checkBoth(       0, 10000000,     4095, "Strategy 2", testCompare);
        tester.checkBoth(       0, 10000000,  5000000, "Strategy 1", testCompare);
        tester.checkBoth( 5000000,  5000000,        3, "Strategy 3", testCompare);
        tester.checkBoth( 5000000,  5000000,     2047, "Strategy 2", testCompare);
        tester.checkBoth( 5000000,  5000000,  2500000, "Strategy 1", testCompare);

        tester.checkBoth(       0, 50000000,        3, "Strategy 3", testCompare);
        tester.checkBoth(       0, 50000000,    16383, "Strategy 2", testCompare);
        tester.checkBoth(       0, 50000000, 25000000, "Strategy 1", testCompare);
        tester.checkBoth(25000000, 25000000,        3, "Strategy 3", testCompare);
        tester.checkBoth(25000000, 25000000,     8191, "Strategy 2", testCompare);
        tester.checkBoth(25000000, 25000000, 12500000, "Strategy 1", testCompare);

        System.out.println("Ran " + tester.count + " tests with " + tester.successes + " success(es) and " + tester.failures + " failure(s).");
        System.exit(tester.failures);
    }
}
