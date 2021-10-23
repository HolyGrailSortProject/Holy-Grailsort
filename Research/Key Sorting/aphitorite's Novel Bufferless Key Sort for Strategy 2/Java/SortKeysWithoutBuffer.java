    private static <T> void groupKeys(T[] array, int left, int right, T medianKey, Comparator<T> cmp) {
        while(left < right && cmp.compare(array[left], medianKey) < 0) left++;
        
        for(int i = left + 1; i < right; i++) {
            if(cmp.compare(array[i], medianKey) < 0) {
                insertBackwards(array, left, i - left);
                left++;
            }
        }
    }

    private static <T> void mergeGroups(T[] array, int left, int middle, int right, T medianKey, Comparator<T> cmp) {
        int leftLen = middle - left;
        int rightLen = right - middle;
        
        int mergeStart = left + binarySearchLeft(array, left, leftLen, medianKey, cmp);
        int mergeLen = binarySearchLeft(array, middle, rightLen, medianKey, cmp);
        
        rotate(array, mergeStart, middle - mergeStart, mergeLen);
    }

    // another novel, elegant, and efficient key sort for Holy Grailsort, specifically for Strategy 2
    // based on lazy stable sorting, this algorithm achieves less than 2n comparisons and O(n log n) moves
    // works based off of the same assumptions as 'sortKeys'
    // designed and implemented by aphitorite
    private static <T> void lazySortKeys(T[] array, int firstKey, int keyCount, T medianKey, Comparator<T> cmp) {
        int runLen = 8;
        int keysEnd = firstKey + keyCount;
        
        int i;
        for(i = firstKey; i + runLen < keysEnd; i += runLen) {
            groupKeys(array, i, i + runLen, medianKey, cmp);
        }
        groupKeys(array, i, keysEnd, medianKey, cmp);
        
        while(runLen < keyCount) {
            int fullMerge = 2 * runLen;
            
            int mergeIndex;
            int mergeEnd = keysEnd - fullMerge;
            
            for(mergeIndex = firstKey; mergeIndex <= mergeEnd; mergeIndex += fullMerge) {
                mergeGroups(array, mergeIndex, mergeIndex + runLen, mergeIndex + fullMerge, medianKey, cmp);
            }
            
            int leftOver = keysEnd - mergeIndex;
            if(leftOver > runLen) {
                mergeGroups(array, mergeIndex, mergeIndex + runLen, keysEnd, medianKey, cmp);
            }
            
            runLen *= 2;
        }
    }