package holygrail;

import java.util.Comparator;

/*
 * MIT License
 * 
 * Copyright (c) 2013 Andrey Astrelin
 * Copyright (c) 2020-2021 The Holy Grail Sort Project
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// HOLY GRAILSORT FOR JAVA - A faster implementation of in-place, stable,
//                          worst-case O(n log n) sorting based on Andrey
//                          Astrelin's Grailsort
//
// ** Written and maintained by The Holy Grail Sort Project
//
// Primary authors: Summer Dragonfly and Anonymous0726, with the incredible aid
// from the rest of the team!
//
// Current status: Completely broken; PLEASE DO NOT USE YET (6/6/21)

/*
 * The Holy Grail Sort Project
 * Project Manager:      Summer Dragonfly
 * Project Contributors: 666666t
 *                       Anonymous0726
 *                       aphitorite
 *                       Control
 *                       dani_dlg
 *                       DeveloperSort
 *                       EilrahcF
 *                       Enver
 *                       Gaming32
 *                       lovebuny
 *                       Morwenn
 *                       MP
 *                       phoenixbound
 *                       Spex_guy
 *                       thatsOven
 *                       _fluffyy
 *
 * Special thanks to "The Studio" Discord community!
 */

enum LocalMerge {
    FORWARDS,
    BACKWARDS;
}

//Credit to phoenixbound for this clever idea
enum Subarray {
    LEFT,
    RIGHT;
}

@SuppressWarnings("hiding")
final public class HolyGrailSort<T> {
    private Comparator<T> cmp;

    final static int STATIC_EXT_BUFFER_LEN = 512;

    private T[] extBuffer;
    private int extBufferLen;

    private int currBlockLen;
    private Subarray currBlockOrigin;
    
    public HolyGrailSort(Comparator<T> cmp) {
        this.cmp = cmp;
    }

    private static <T> void swap(T[] array, int a, int b) {
        T temp   = array[a];
        array[a] = array[b];
        array[b] = temp;
    }
   
    private static <T> void swapBlocksForwards(T[] array, int a, int b, int blockLen) {
        for(int i = 0; i < blockLen; i++) {
            swap(array, a + i, b + i);
        }
    }
    
    private static <T> void swapBlocksBackwards(T[] array, int a, int b, int blockLen) {
        for(int i = blockLen - 1; i >= 0; i--) {
            swap(array, a + i, b + i);
        }
    }
    
    // Shift elements [start + 1, start + length + 1) to the left by 1
    // and paste copied element at start + length - 1.
    private static <T> void shiftForwards(T[] array, int start, int length) {
        T item = array[start];
        System.arraycopy(array, start + 1, array, start, length);
        array[start + length] = item;    
    }
    
    // Shift elements [start, start + length) to the right by 1
    // and paste copied element at start.
    private static <T> void shiftBackwards(T[] array, int start, int length) {
        T item = array[start + length];
        System.arraycopy(array, start, array, start + 1, length);
        array[start] = item;
    }
    
    private static <T> void rotate(T[] array, int start, int leftLen, int rightLen) {
        int minLen = leftLen <= rightLen ? leftLen : rightLen;
        
        while(minLen > 1) {
            if(leftLen <= rightLen) {
                do {
                  swapBlocksForwards(array, start, start + leftLen, leftLen);
                  start    += leftLen;
                  rightLen -= leftLen;
                } while(leftLen <= rightLen);
                
                minLen = rightLen;
            }
            else {
                do {
                  swapBlocksBackwards(array, start + leftLen - rightLen, start + leftLen, rightLen);
                  leftLen -= rightLen;
                } while(leftLen > rightLen);
                
                minLen = leftLen;
            }
        }

        if(minLen == 1) {
            if(leftLen == 1) {
                shiftForwards(array, start, rightLen);
            }
            else {
                shiftBackwards(array, start, leftLen);
            }
        }
    }
    
    // unguarded insertion sort
    // implementation thanks to Control and Scandum!
    private static <T> void insertSort(T[] array, int start, int length, Comparator<T> cmp) {
        for(int item = 1; item < length; item++) {
            T temp = array[start + item];
            int index = start + item;
            
            if(cmp.compare(array[index - 1], temp) <= 0) {
                continue;
            }
            
            if(cmp.compare(array[start], temp) > 0) {
                shiftBackwards(array, start, item);
                continue;
            }

            do {
                array[index] = array[index - 1];
                index--;
            } while(cmp.compare(array[index - 1], temp) > 0);
            
            array[index] = temp;
        }
    }
    
    // Technically a "lower bound" search
    private static <T> int binarySearchLeft(T[] array, int start, int length, T target, Comparator<T> cmp) {
        int  left = 0;
        int right = length;

        while(left < right) {
            // equivalent to (left + right) / 2 with added overflow protection
            int middle = (left + right) >>> 1;
            
            if(cmp.compare(array[start + middle], target) < 0) {
                left = middle + 1;
            }
            else {
                right = middle;
            }
        }
        return left;
    }
    
    // Technically a "upper bound" search
    private static <T> int binarySearchRight(T[] array, int start, int length, T target, Comparator<T> cmp) {
        int  left = 0;
        int right = length;

        while(left < right) {
            // equivalent to (left + right) / 2 with added overflow protection
            int middle = (left + right) >>> 1;
            
            if(cmp.compare(array[start + middle], target) > 0) {
                right = middle;
            }
            else {
                left = middle + 1;
            }
        }
        return right;
    }
    
    // Returns -1 if an equal key is found, cutting off the search early
    // FUTURE TODO: first & last key best-cases
    private static <T> int binarySearchExclusive(T[] array, int start, int length, T target, Comparator<T> cmp) {
        int  left = 0;
        int right = length;

        while(left < right) {
            // equivalent to (left + right) / 2 with added overflow protection
            int middle = (left + right) >>> 1;
            
            int comp = cmp.compare(array[start + middle], target);
            if(comp == 0) {
                return -1;
            }
            else if(comp < 0) {
                left = middle + 1;
            }
            else {
                right = middle;
            }
        }
        return left;
    }
    
    // cost: 2 * length + idealKeys^2 / 2
    private static <T> int collectKeys(T[] array, int start, int length, int idealKeys, Comparator<T> cmp) {
        int keysFound = 1; // by itself, the first item in the array is our first unique key
        int  firstKey = 0; // the first item in the array is at the first position in the array
        int   currKey = 1; // the index used for finding potentially unique items ("keys") in the array

        while(currKey < length && keysFound < idealKeys) {

            // Find the location in the key-buffer where our current key can be inserted in sorted order.
            // If the key at insertPos is equal to currKey, then currKey isn't unique and we move on.
            int insertPos = binarySearchExclusive(array, start + firstKey, keysFound, array[start + currKey], cmp);

            // As long as our exclusive binary search didn't return -1 (a.k.a. found an equal key),
            // we're good to go!
            if(insertPos != -1) {
                // First, rotate the key-buffer over to currKey's immediate left...
                // (this helps save a TON of swaps/writes!!!)
                rotate(array, start + firstKey, keysFound, currKey - (firstKey + keysFound));

                // Update the new position of firstKey...
                firstKey = currKey - keysFound;

                // Then, "insertion sort" currKey to its spot in the key-buffer
                // as long as it needs to be moved!
                if(keysFound != insertPos) {
                    shiftBackwards(array, start + firstKey + insertPos, keysFound - insertPos);
                }
                
                // One step closer to idealKeys.
                keysFound++;
            }
            // Move on and test the next key...
            currKey++;
        }

        // Bring however many keys we found back to the beginning of our array,
        // and return the number of keys collected.
        rotate(array, start, firstKey, keysFound);
        return keysFound;
    }

    // Much thanks to Spex_guy for this beautiful optimization!!
    private static <T> void sortPairsWithKeys(T[] array, int start, int length, Comparator<T> cmp) {
        // first, save the keys to stack memory
        T  firstKey = array[start - 1];
        T secondKey = array[start - 2];
        
        // move all the items down two indices, sorting them simultaneously
        sortPairs(array, start, length, cmp);
        
        // finally, stamp the saved keys (remember: order doesn't matter!)
        // to the end of the array
        array[start + length - 2] =  firstKey;
        array[start + length - 1] = secondKey;
    }
    
    private static <T> void sortPairs(T[] array, int start, int length, Comparator<T> cmp) {
        int index;
        for(index = 1; index < length; index += 2) {
            int  left = start + index - 1;
            int right = start + index;

            if(cmp.compare(array[left], array[right]) > 0) {
                array[ left - 2] = array[right];
                array[right - 2] = array[ left];
            }
            else {
                array[ left - 2] = array[ left];
                array[right - 2] = array[right];
            }
        }

        int left = start + index - 1;
        if(left < start + length) {
            array[left - 2] = array[left];
        }
    }
    
    // array[buffer .. start - 1] <=> "scrolling buffer"
    // 
    // "scrolling buffer" + array[start, middle - 1] + array[middle, end - 1]
    // --> array[buffer, buffer + end - 1] + "scrolling buffer"
    private static <T> void mergeForwards(T[] array, int start, int leftLen, int rightLen,
                                                     int bufferOffset, Comparator<T> cmp) {
        int buffer = start  - bufferOffset;
        int   left = start;
        int middle = start  +  leftLen;
        int  right = middle;
        int    end = middle + rightLen;

        while(right < end) {
            if(left == middle || cmp.compare(array[ left],
                                             array[right]) > 0) {
                swap(array, buffer, right);
                right++;
            }
            else {
                swap(array, buffer,  left);
                left++;
            }
            buffer++;
        }

        if(buffer != left) {
            swapBlocksForwards(array, buffer, left, middle - left);
        }
    }

    // credit to 666666t for thorough bug-checking/fixing
    private static <T> void mergeBackwards(T[] array, int start, int leftLen, int rightLen,
                                                      int bufferOffset, Comparator<T> cmp) {
        int    end = start  -  1;
        int   left = end    +  leftLen;
        int middle = left;
        int  right = middle + rightLen;
        int buffer = right  + bufferOffset;

        while(left > end) {
            if(right == middle || cmp.compare(array[ left],
                                              array[right]) > 0) {
                swap(array, buffer,  left);
                left--;
            }
            else {
                swap(array, buffer, right);
                right--;
            }
            buffer--;
        }

        if(right != buffer) {
            swapBlocksBackwards(array, buffer, right, right - middle);
        }
    }

    // array[buffer .. start - 1] <=> "free space"    
    //
    // "free space" + array[start, middle - 1] + array[middle, end - 1]
    // --> array[buffer, buffer + end - 1] + "free space"
    //
    // FUNCTION RENAMED: More consistent with "out-of-place" being at the end
    private static <T> void mergeForwardsOutOfPlace(T[] array, int start, int leftLen, int rightLen,
                                                               int bufferOffset, Comparator<T> cmp) {
        int buffer = start  - bufferOffset;
        int   left = start;
        int middle = start  +  leftLen;
        int  right = middle;
        int    end = middle + rightLen;

        while(right < end) {
            if(left == middle || cmp.compare(array[ left],
                                             array[right]) > 0) {
                array[buffer] = array[right];
                right++;
            }
            else {
                array[buffer] = array[ left];
                left++;
            }
            buffer++;
        }

        if(buffer != left) {
            while(left < middle) {
                array[buffer] = array[left];
                buffer++;
                left++;
            }
        }
    }
    
    private static <T> void mergeBackwardsOutOfPlace(T[] array, int start, int leftLen, int rightLen,
                                                                int bufferOffset, Comparator<T> cmp) {
        int    end = start  -  1;
        int   left = end    +  leftLen;
        int middle = left;
        int  right = middle + rightLen;
        int buffer = right  + bufferOffset;

        while(left > end) {
            if(right == middle || cmp.compare(array[ left],
                                              array[right]) > 0) {
                array[buffer] = array[ left];
                left--;
            }
            else {
                array[buffer] = array[right];
                right--;
            }
            buffer--;
        }

        if(right != buffer) {
            while(right > middle) {
                array[buffer] = array[right];
                buffer--;
                right--;
            }
        }
    }
    
    private static <T> void buildInPlace(T[] array, int start, int length, int currentLen, int bufferLen, Comparator<T> cmp) {
        for(int mergeLen = currentLen; mergeLen < bufferLen; mergeLen *= 2) {
            int fullMerge = 2 * mergeLen;

            int mergeIndex;
            int mergeEnd = start + length - fullMerge;
            int bufferOffset = mergeLen;

            for(mergeIndex = start; mergeIndex <= mergeEnd; mergeIndex += fullMerge) {
                mergeForwards(array, mergeIndex, mergeLen, mergeLen, bufferOffset, cmp);
            }

            int leftOver = length - (mergeIndex - start);

            if(leftOver > mergeLen) {
                mergeForwards(array, mergeIndex, mergeLen, leftOver - mergeLen, bufferOffset, cmp);
            }
            else {
                rotate(array, mergeIndex - mergeLen, mergeLen, leftOver);
            }

            start -= mergeLen;
        }

        int fullMerge  = 2 * bufferLen; 
        int lastBlock  = length % fullMerge;
        int lastOffset = start + length - lastBlock;

        if(lastBlock <= bufferLen) {
            rotate(array, lastOffset, lastBlock, bufferLen);
        }
        else {
            mergeBackwards(array, lastOffset, bufferLen, lastBlock - bufferLen, bufferLen, cmp);
        }

        for(int mergeIndex = lastOffset - fullMerge; mergeIndex >= start; mergeIndex -= fullMerge) {
            mergeBackwards(array, mergeIndex, bufferLen, bufferLen, bufferLen, cmp);
        }
    }
    
    private void buildOutOfPlace(T[] array, int start, int length, int bufferLen, int extLen, Comparator<T> cmp) {
        System.arraycopy(array, start - extLen, this.extBuffer, 0, extLen);

        sortPairs(array, start, length, cmp);
        start -= 2;

        int mergeLen;
        for(mergeLen = 2; mergeLen < extLen; mergeLen *= 2) {
            int fullMerge = 2 * mergeLen;

            int mergeIndex;
            int mergeEnd = start + length - fullMerge;
            int bufferOffset = mergeLen;

            for(mergeIndex = start; mergeIndex <= mergeEnd; mergeIndex += fullMerge) {
                mergeForwardsOutOfPlace(array, mergeIndex, mergeLen, mergeLen, bufferOffset, cmp);
            }

            int leftOver = length - (mergeIndex - start);

            if(leftOver > mergeLen) {
                mergeForwardsOutOfPlace(array, mergeIndex, mergeLen, leftOver - mergeLen, bufferOffset, cmp);
            }
            else {
                System.arraycopy(array, mergeIndex, array, mergeIndex - mergeLen, leftOver);
            }

            start -= mergeLen;
        }

        if(extLen == bufferLen) {
            int fullMerge  = 2 * bufferLen; 
            int lastBlock  = length % fullMerge;
            int lastOffset = start + length - lastBlock;

            if(lastBlock <= bufferLen) {
                System.arraycopy(array, lastOffset, array, lastOffset + bufferLen, lastBlock);
            }
            else {
                mergeBackwardsOutOfPlace(array, lastOffset, bufferLen, lastBlock - bufferLen, bufferLen, cmp);
            }

            for(int mergeIndex = lastOffset - fullMerge; mergeIndex >= start; mergeIndex -= fullMerge) {
                mergeBackwardsOutOfPlace(array, mergeIndex, bufferLen, bufferLen, bufferLen, cmp);
            }
        }
        else {
            System.arraycopy(this.extBuffer, 0, array, start + length, extLen);
            buildInPlace(array, start, length, mergeLen, bufferLen, cmp);
        }
    }
    
    // build blocks of length 'bufferLen'
    // input: [start - mergeLen, start - 1] elements are buffer
    // output: first 'bufferLen' elements are buffer, blocks (2 * bufferLen) and last subblock sorted
    private void buildBlocks(T[] array, int start, int length, int bufferLen, Comparator<T> cmp) {
        if(this.extBuffer != null) {
            int extLen;

            if(bufferLen < this.extBufferLen) {
                extLen = bufferLen;
            }
            else {
                // max power of 2 -- just in case
                extLen = 1;
                while((extLen * 2) <= this.extBufferLen) {
                    extLen *= 2;
                }
            }

            this.buildOutOfPlace(array, start, length, bufferLen, extLen, cmp);
        }
        else {
            sortPairsWithKeys(array, start, length, cmp);
            buildInPlace(array, start - 2, length, 2, bufferLen, cmp);
        }
    }
    
    // implementation of "smart block selection" sort
    // code inspired by Anonymous0726
    private static <T> void sortBlocks(T[] array, int firstKey, int start, int blockCount,
                                                  int leftBlocks, int blockLen,
                                                  boolean sortByTail, Comparator<T> cmp) {
        // this check might be unnecessary with the new "combine blocks" control flow
		if(blockCount == leftBlocks) return;
        
        int cmpIndex   = sortByTail ? blockLen - 1 : 0;
        
        int blockIndex = start;
        int keyIndex   = firstKey;
        
        int rightBlock = start    + (leftBlocks * blockLen);
        int rightKey   = firstKey +  leftBlocks;
        
        boolean sorted = true;
        
        // phase one: find first index in left subarray where a smaller right block can be swapped;
        //            if no swaps occur, the subarrays are already in order
        do {
            if(cmp.compare(array[rightBlock + cmpIndex], array[blockIndex + cmpIndex]) < 0) {
                swapBlocksForwards(array, blockIndex, rightBlock, blockLen);
                swap(array, keyIndex, rightKey);
                sorted = false;
            }
            blockIndex += blockLen;
            keyIndex++;
        } while(sorted && keyIndex < rightKey);

        if(sorted) return;
        
		// consider anonymous' suggestion
		
        int scrambledIndex = rightKey + 1;
		
        // phase two: replace the entire left subarray with blocks in sorted order from the
        //            scrambled area, keeping track of the rightmost block swapped
        do {
            int selectBlock = rightBlock;
            int selectKey   = rightKey;
            
            int currBlock   = rightBlock + blockLen;
            
            for(int currKey = rightKey + 1; currKey <= scrambledIndex; currKey++, currBlock += blockLen) {
                int compare = cmp.compare(array[currBlock + cmpIndex], array[selectBlock + cmpIndex]);
                if (compare < 0 || (compare == 0 && cmp.compare(array[  currKey],
                                                                array[selectKey]) < 0)) {
                    selectBlock = currBlock;
                    selectKey   = currKey;
                }
            }
            
            swapBlocksForwards(array, blockIndex, selectBlock, blockLen);
            swap(array, keyIndex, selectKey);
            
            if(selectKey == scrambledIndex) scrambledIndex++;
            
            blockIndex += blockLen;
            keyIndex++;
        } while(keyIndex < rightKey);
        
        int lastKey = firstKey + blockCount - 1;
        
        // phase three: after the left subarray has been sorted, keep finding the next block in order
        //              from the scrambled area until either (a) the scrambled area runs out of blocks,
        //              meaning the rest are sorted, or (b) the scrambled area hits the end of the right
        //              subarray
        do {
            int selectBlock = blockIndex;
            int selectKey   = keyIndex;
            
            int currBlock   = blockIndex + blockLen;
            
            for(int currKey = keyIndex + 1; currKey <= scrambledIndex; currKey++, currBlock += blockLen) {
                int compare = cmp.compare(array[currBlock + cmpIndex], array[selectBlock + cmpIndex]);
                if (compare < 0 || (compare == 0 && cmp.compare(array[  currKey],
                                                                array[selectKey]) < 0)) {
                    selectBlock = currBlock;
                    selectKey   = currKey;
                }
            }
            
            if(selectKey != keyIndex) {
                swapBlocksForwards(array, blockIndex, selectBlock, blockLen);
                swap(array, keyIndex, selectKey);
            
                if(selectKey == scrambledIndex) scrambledIndex++;
            }
            
            blockIndex += blockLen;
            keyIndex++;
        } while(keyIndex < scrambledIndex && scrambledIndex < lastKey);
        
        if(keyIndex == scrambledIndex) return;
        
        // phase four: sort the remainder blocks from the scrambled area
        do {
            int selectBlock = blockIndex;
            int selectKey   = keyIndex;
            
            int currBlock   = blockIndex + blockLen;
            
            for(int currKey = keyIndex + 1; currKey <= lastKey; currKey++, currBlock += blockLen) {
                int compare = cmp.compare(array[currBlock + cmpIndex], array[selectBlock + cmpIndex]);
                if (compare < 0 || (compare == 0 && cmp.compare(array[  currKey],
                                                                array[selectKey]) < 0)) {
                    selectBlock = currBlock;
                    selectKey   = currKey;
                }
            }
            
            if(selectKey != keyIndex) {
                swapBlocksForwards(array, blockIndex, selectBlock, blockLen);
                swap(array, keyIndex, selectKey);
            }
            
            blockIndex += blockLen;
            keyIndex++;
        } while(keyIndex < lastKey);
        
        /*
        for(int blockIndex = 0, keyIndex = 0; keyIndex < keyCount; blockIndex += blockLen, keyIndex++) {
            int selectBlock = blockIndex;
            int selectKey   = keyIndex;
            
            for(int currBlock = selectBlock + blockLen, currKey = keyIndex + 1;
                    currKey < keyCount; currBlock += blockLen, currKey++) {
 
                int compare = cmp.compare(array[start + currBlock], array[start + selectBlock]);               
                if (compare < 0 || (compare == 0 && cmp.compare(array[firstKey +   currKey],
                                                                array[firstKey + selectKey]) < 0)) {
                    selectBlock = currBlock;
                    selectKey   = currKey;
                }
            }
            
            if(selectKey != keyIndex) {
                swapBlocksForwards(array, start + blockIndex, start + selectBlock, blockLen);
                swap(array, firstKey + keyIndex, firstKey + selectKey);
            }
        }
        */
    }
    
    
    
    // TODO: Rewrite these f*cking abominations.
    private static <T> void rewindBuffer(T[] array, int start, int leftBlock, int buffer) {
        while(leftBlock >= start) {
            swap(array, buffer, leftBlock);
            leftBlock--;
            buffer--;
        }
    }

    private static <T> void rewindOutOfPlace(T[] array, int start, int leftBlock, int buffer) {
        while(leftBlock >= start) {
            array[buffer] = array[leftBlock];
            leftBlock--;
            buffer--;
        }
    }
    
    private static <T> void fastForwardBuffer(T[] array, int buffer, int rightBlock, int end) {
        while(rightBlock <= end) {
            swap(array, rightBlock, buffer);
            rightBlock++;
            buffer++;
        }
    }
    
    private static <T> void fastForwardOutOfPlace(T[] array, int buffer, int rightBlock, int end) {
        while(rightBlock <= end) {
            array[buffer] = array[rightBlock];
            rightBlock++;
            buffer++;
        }
    }
    
    // Swaps Grailsort's "scrolling buffer" from the right side of the array all the way back to 'start'.
    // Costs O(n) swaps.
    private static <T> void resetBuffer(T[] array, int start, int length, int bufferOffset) {
        int buffer =  start + length - 1;
        int  index = buffer - bufferOffset;
        
        while(buffer >= start) {
            swap(array, index, buffer);
            buffer--;
            index--;
        }
    }
    
    private static <T> Subarray getSubarray(T[] array, int currentKey, T medianKey, Comparator<T> cmp) {
        if(cmp.compare(array[currentKey], medianKey) < 0) {
            return Subarray.LEFT;
        }
        else {
            return Subarray.RIGHT;
        }
    }
    
    // FUNCTION RE-RENAMED: last/final left blocks are used to calculate the length of the final merge
    private static <T> int countLastMergeBlocks(T[] array, int offset, int blockCount, int blockLen,
                                                           Comparator<T> cmp) {
        int blocksToMerge = 0;

        int lastRightFrag = offset + (blockCount * blockLen);
        int prevLeftBlock = lastRightFrag - blockLen;

        while(blocksToMerge < blockCount && cmp.compare(array[lastRightFrag],
                                                        array[prevLeftBlock]) < 0) {
            blocksToMerge++;
            prevLeftBlock -= blockLen;
        }

        return blocksToMerge;
    }
    
    private void localMergeForwards(T[] array, int start, int leftLen, Subarray leftOrigin,
                                                          int rightLen, int bufferOffset,
                                                          Comparator<T> cmp) {
        int buffer = start  - bufferOffset;
        int   left = start;
        int middle = start  +  leftLen;
        int  right = middle;
        int    end = middle + rightLen;

        if(leftOrigin == Subarray.LEFT) {
            while(left < middle && right < end) {
                if(cmp.compare(array[left], array[right]) <= 0) {
                    swap(array, buffer, left);
                    left++;
                }
                else {
                    swap(array, buffer, right);
                    right++;
                }
                buffer++;
            }
        }
        else {
            while(left < middle && right < end) {
                if(cmp.compare(array[left], array[right]) <  0) {
                    swap(array, buffer, left);
                    left++;
                }
                else {
                    swap(array, buffer, right);
                    right++;
                }
                buffer++;
            }            
        }

        if(left < middle) {
            this.currBlockLen = middle - left;
            rewindBuffer(array, left, middle - 1, end - 1);
        }
        else {
            this.currBlockLen = end - right;
            if(leftOrigin == Subarray.LEFT) {
                this.currBlockOrigin = Subarray.RIGHT;
            }
            else {
                this.currBlockOrigin = Subarray.LEFT;
            }
        }
    }

    private void localMergeBackwards(T[] array, int start, int leftLen, int rightLen, Subarray rightOrigin,
                                                           int bufferOffset, Comparator<T> cmp) {
        int    end = start  -  1;
        int   left = end    +  leftLen;
        int middle = left;
        int  right = middle + rightLen;
        int buffer = right  + bufferOffset;
        
        if(rightOrigin == Subarray.RIGHT) {
            while(left > end && right > middle) {
                if(cmp.compare(array[left], array[right]) >  0) {
                    swap(array, buffer, left);
                    left--;
                }
                else {
                    swap(array, buffer, right);
                    right--;
                }
                buffer--;
            }
        }
        else {
            while(left > end && right > middle) {
                if(cmp.compare(array[left], array[right]) >= 0) {
                    swap(array, buffer, left);
                    left--;
                }
                else {
                    swap(array, buffer, right);
                    right--;
                }
                buffer--;
            }
        }
        
        if(right > middle) {
            this.currBlockLen = right - middle;
            fastForwardBuffer(array, end + 1, middle + 1, right);
        }
        else {
            this.currBlockLen = left - end;
            if(rightOrigin == Subarray.RIGHT) {
                this.currBlockOrigin = Subarray.LEFT;
            }
            else {
                this.currBlockOrigin = Subarray.RIGHT;
            }
        }
    }
    
    private void localLazyMerge(T[] array, int start, int leftLen, Subarray leftOrigin, int rightLen, Comparator<T> cmp) {
        int middle = start + leftLen;
        
        if(leftOrigin == Subarray.LEFT) {
            if(cmp.compare(array[middle - 1], array[middle]) >  0) {
                while(leftLen != 0) {
                    int mergeLen = binarySearchLeft(array, middle, rightLen, array[start], cmp);

                    if(mergeLen != 0) {
                        rotate(array, start, leftLen, mergeLen);
                        
                        start    += mergeLen;
                        middle   += mergeLen;
                        rightLen -= mergeLen;
                    }
                    
                    if(rightLen == 0) {
                        this.currBlockLen = leftLen;
                        return;
                    }
                    else {
                        do {
                            start++;
                            leftLen--;
                        } while(leftLen != 0 && cmp.compare(array[start ],
                                                            array[middle]) <= 0);
                    }
                }
            }
        }
        else {
            if(cmp.compare(array[middle - 1], array[middle]) >= 0) {
                while(leftLen != 0) {
                    int mergeLen = binarySearchRight(array, middle, rightLen, array[start], cmp);

                    if(mergeLen != 0) {
                        rotate(array, start, leftLen, mergeLen);
                        
                        start    += mergeLen;
                        middle   += mergeLen;
                        rightLen -= mergeLen;
                    }
                    
                    if(rightLen == 0) {
                        this.currBlockLen = leftLen;
                        return;
                    }
                    else {
                        do {
                            start++;
                            leftLen--;
                        } while(leftLen != 0 && cmp.compare(array[start ],
                                                            array[middle]) < 0);
                    }
                }
            }
        }

        this.currBlockLen = rightLen;
        if(leftOrigin == Subarray.LEFT) {
            this.currBlockOrigin = Subarray.RIGHT;
        }
        else {
            this.currBlockOrigin = Subarray.LEFT;
        }
    }
    
    // FUNCTION RENAMED: more consistent with other "out-of-place" merges
    private void localMergeForwardsOutOfPlace(T[] array, int start, int leftLen, Subarray leftOrigin,
                                                                    int rightLen, int bufferOffset,
                                                                    Comparator<T> cmp) {
        int buffer = start  - bufferOffset;
        int   left = start;
        int middle = start  +  leftLen;
        int  right = middle;
        int    end = middle + rightLen;

        if(leftOrigin == Subarray.LEFT) {
            while(left < middle && right < end) {
                if(cmp.compare(array[left], array[right]) <= 0) {
                    array[buffer] = array[left];
                    left++;
                }
                else {
                    array[buffer] = array[right];
                    right++;
                }
                buffer++;
            }
        }
        else {
            while(left < middle && right < end) {
                if(cmp.compare(array[left], array[right]) <  0) {
                    array[buffer] = array[left];
                    left++;
                }
                else {
                    array[buffer] = array[right];
                    right++;
                }
                buffer++;
            }            
        }

        if(left < middle) {
            this.currBlockLen = middle - left;
            rewindOutOfPlace(array, left, middle - 1, end - 1);
        }
        else {
            this.currBlockLen = end - right;
            if(leftOrigin == Subarray.LEFT) {
                this.currBlockOrigin = Subarray.RIGHT;
            }
            else {
                this.currBlockOrigin = Subarray.LEFT;
            }
        }
    }
    
    private void localMergeBackwardsOutOfPlace(T[] array, int start, int leftLen, int rightLen,
                                                                     Subarray rightOrigin,
                                                                     int bufferOffset,
                                                                     Comparator<T> cmp) {
        int    end = start  -  1;
        int   left = end    +  leftLen;
        int middle = left;
        int  right = middle + rightLen;
        int buffer = right  + bufferOffset;

        if(rightOrigin == Subarray.RIGHT) {
            while(left > end && right > middle) {
                if(cmp.compare(array[left], array[right]) >  0) {
                    array[buffer] = array[left];
                    left--;
                }
                else {
                    array[buffer] = array[right];
                    right--;
                }
                buffer--;
            }
        }
        else {
            while(left > end && right > middle) {
                if(cmp.compare(array[left], array[right]) >= 0) {
                    array[buffer] = array[left];
                    left--;
                }
                else {
                    array[buffer] = array[right];
                    right--;
                }
                buffer--;
            }
        }

        if(right > middle) {
            this.currBlockLen = right - middle;
            fastForwardOutOfPlace(array, end + 1, middle + 1, right);
        }
        else {
            this.currBlockLen = left - end;
            if(rightOrigin == Subarray.RIGHT) {
                this.currBlockOrigin = Subarray.LEFT;
            }
            else {
                this.currBlockOrigin = Subarray.RIGHT;
            }
        }
    }

    private void mergeBlocksForwards(T[] array, int firstKey, T medianKey, int start,
                                                int blockCount, int blockLen, int lastMergeBlocks,
                                                int lastLen, Comparator<T> cmp) {
        int buffer;

        int currBlock;
        int nextBlock = start + blockLen;

        this.currBlockLen    = blockLen;
        this.currBlockOrigin = getSubarray(array, firstKey, medianKey, cmp);

        for(int keyIndex = 1; keyIndex < blockCount; keyIndex++, nextBlock += blockLen) {
            Subarray nextBlockOrigin;

            currBlock       = nextBlock - this.currBlockLen;
            nextBlockOrigin = getSubarray(array, firstKey + keyIndex, medianKey, cmp);

            if(nextBlockOrigin != this.currBlockOrigin) {
                this.localMergeForwards(array, currBlock, this.currBlockLen, this.currBlockOrigin,
                                        blockLen, blockLen, cmp);   
            }
            else {
                buffer = currBlock - blockLen;
                swapBlocksForwards(array, buffer, currBlock, this.currBlockLen);
                this.currBlockLen = blockLen;
            }
        }

        currBlock = nextBlock - this.currBlockLen;
        buffer    = currBlock - blockLen;

        if(lastLen != 0) {
            if(this.currBlockOrigin == Subarray.RIGHT) {
                swapBlocksForwards(array, buffer, currBlock, this.currBlockLen);

                currBlock            = nextBlock;
                this.currBlockLen    = blockLen * lastMergeBlocks;
                this.currBlockOrigin = Subarray.LEFT;
            }
            else {
                this.currBlockLen += blockLen * lastMergeBlocks;
            }

            mergeForwards(array, currBlock, this.currBlockLen, lastLen, blockLen, cmp);
        }
        else {
            swapBlocksForwards(array, buffer, currBlock, this.currBlockLen);
        }
    }

    private void lazyMergeBlocks(T[] array, int firstKey, T medianKey, int start,
                                            int blockCount, int blockLen, int lastMergeBlocks,
                                            int lastLen, Comparator<T> cmp) {
        int currBlock;
        int nextBlock = start + blockLen;

        this.currBlockLen    = blockLen;
        this.currBlockOrigin = getSubarray(array, firstKey, medianKey, cmp);

        for(int keyIndex = 1; keyIndex < blockCount; keyIndex++, nextBlock += blockLen) {
            Subarray nextBlockOrigin;

            currBlock       = nextBlock - this.currBlockLen;
            nextBlockOrigin = getSubarray(array, firstKey + keyIndex, medianKey, cmp);

            if(nextBlockOrigin != this.currBlockOrigin) {
                this.localLazyMerge(array, currBlock, this.currBlockLen, this.currBlockOrigin,
                                    blockLen, cmp);
            }
            else {
                this.currBlockLen = blockLen;
            }
        }

        currBlock = nextBlock - this.currBlockLen;

        if(lastLen != 0) {
            if(this.currBlockOrigin == Subarray.RIGHT) {
                currBlock            = nextBlock;
                this.currBlockLen    = blockLen * lastMergeBlocks;
                this.currBlockOrigin = Subarray.LEFT;
            }
            else {
                this.currBlockLen += blockLen * lastMergeBlocks;
            }

            // TODO: double-check direction
            lazyMergeBackwards(array, currBlock, this.currBlockLen, lastLen, cmp);
        }
    }

    private void mergeBlocksBackwards(T[] array, int firstKey, T medianKey, int start,
                                      int blockCount, int blockLen, int lastLen, Comparator<T> cmp) {
        
        int nextBlock = start + (blockCount * blockLen) - 1;
        int currBlock;
        
        int buffer    = nextBlock + lastLen + blockLen;
        
        // The last fragment (lastLen) came from the right subarray,
        // although it may be empty (lastLen == 0)
        this.currBlockLen    = lastLen;
        this.currBlockOrigin = Subarray.RIGHT;
        
        for(int keyIndex = blockCount - 1; keyIndex >= 0; keyIndex--, nextBlock -= blockLen) {
            Subarray nextBlockOrigin;
            
            currBlock       = nextBlock + this.currBlockLen;
            nextBlockOrigin = getSubarray(array, firstKey + keyIndex, medianKey, cmp);
            
            if(nextBlockOrigin != this.currBlockOrigin) {
                // TODO: buffer length *should* always be equivalent to:
                // right block length -  forwards merge blocks
                //  left block length - backwards merge blocks
                this.localMergeBackwards(array, currBlock, blockLen, this.currBlockLen, this.currBlockOrigin,
                                         blockLen, cmp);
            }
            else {
                buffer = currBlock + blockLen;
                swapBlocksBackwards(array, currBlock, buffer, this.currBlockLen);
                this.currBlockLen = blockLen;
            }
        }
    }
    
    private void mergeBlocksForwardsOutOfPlace(T[] array, int firstKey, T medianKey, int start,
                                                          int blockCount, int blockLen, int lastMergeBlocks,
                                                          int lastLen, Comparator<T> cmp) {
        int buffer;

        int currBlock;
        int nextBlock = start + blockLen;

        this.currBlockLen    = blockLen;
        this.currBlockOrigin = getSubarray(array, firstKey, medianKey, cmp);

        for(int keyIndex = 1; keyIndex < blockCount; keyIndex++, nextBlock += blockLen) {
            Subarray nextBlockOrigin;

            currBlock       = nextBlock - this.currBlockLen;  
            nextBlockOrigin = getSubarray(array, firstKey + keyIndex, medianKey, cmp);

            if(nextBlockOrigin != this.currBlockOrigin) {
                this.localMergeForwardsOutOfPlace(array, currBlock, this.currBlockLen, this.currBlockOrigin,
                                                  blockLen, blockLen, cmp);                
            }
            else {
                buffer = currBlock - blockLen;

                System.arraycopy(array, currBlock, array, buffer, this.currBlockLen);
                this.currBlockLen = blockLen;

            }
        }

        currBlock = nextBlock - this.currBlockLen;
        buffer    = currBlock - blockLen;

        if(lastLen != 0) {
            if(this.currBlockOrigin == Subarray.RIGHT) {
                System.arraycopy(array, currBlock, array, buffer, this.currBlockLen);

                currBlock            = nextBlock;
                this.currBlockLen    = blockLen * lastMergeBlocks;
                this.currBlockOrigin = Subarray.LEFT;
            }
            else {
                this.currBlockLen += blockLen * lastMergeBlocks;
            }

            mergeForwardsOutOfPlace(array, currBlock, this.currBlockLen, lastLen, blockLen, cmp);
        }
        else {
            System.arraycopy(array, currBlock, array, buffer, this.currBlockLen);
        }
    }
    
    private void mergeBlocksBackwardsOutOfPlace(T[] array, int firstKey, T medianKey, int start,
                                                int blockCount, int blockLen, int lastLen, Comparator<T> cmp) {
        int nextBlock = start + (blockCount * blockLen) - 1;
        int currBlock;
        
        int buffer    = nextBlock + lastLen + blockLen;
        
        // The last fragment (lastLen) came from the right subarray,
        // although it may be empty (lastLen == 0)
        this.currBlockLen    = lastLen;
        this.currBlockOrigin = Subarray.RIGHT;
        
        for(int keyIndex = blockCount - 1; keyIndex >= 0; keyIndex--, nextBlock -= blockLen) {
            Subarray nextBlockOrigin;
            
            currBlock       = nextBlock + this.currBlockLen;
            nextBlockOrigin = getSubarray(array, firstKey + keyIndex, medianKey, cmp);
            
            if(nextBlockOrigin != this.currBlockOrigin) {
                this.localMergeBackwardsOutOfPlace(array, currBlock, blockLen, this.currBlockLen, this.currBlockOrigin,
                                                   blockLen, cmp);
            }
            else {
                buffer = currBlock + blockLen;
                System.arraycopy(array, currBlock, array, buffer, this.currBlockLen);
                this.currBlockLen = blockLen;
            }
        }
    }
    
    private void combineForwards(T[] array, int firstKey, int start, int length, int subarrayLen, int blockLen) {
        Comparator<T> cmp = this.cmp; // local variable for performance à la Timsort

        // TODO: Double-check names and change all other functions to match
        int      mergeLen = 2 * subarrayLen;
        int    fullMerges = length / mergeLen;      
        int    blockCount = mergeLen / blockLen;
        int lastSubarrays = length - (mergeLen * fullMerges);
        
        int fastForwardLen = 0;
        if(lastSubarrays <= subarrayLen) {
            if(fullMerges % 2 != 0) {
                fastForwardLen = lastSubarrays;
            }
            length -= lastSubarrays;
            lastSubarrays = 0;
        }
        
        int leftBlocks = subarrayLen / blockLen;
        T   medianKey  = array[firstKey + leftBlocks];
        
        for(int mergeIndex = 0; mergeIndex < fullMerges; mergeIndex++) {
            int offset = start + (mergeIndex * mergeLen);
            
            sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, false, cmp);
            this.mergeBlocksForwards(array, firstKey, medianKey, offset, blockCount, blockLen, 0, 0, cmp);
            
            // TODO: Replace with Control's key sort
            insertSort(array, firstKey, blockCount, cmp);
        }

        int offset = start + (fullMerges * mergeLen);
        
        if(lastSubarrays != 0) {
            blockCount = lastSubarrays / blockLen;
            
            sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, false, cmp);

            int lastFragment = lastSubarrays - (blockCount * blockLen);
            int lastMergeBlocks = 0;
            if (lastFragment != 0) {
                lastMergeBlocks = countLastMergeBlocks(array, offset, blockCount, blockLen, cmp);
            }

            int smartMerges = blockCount - lastMergeBlocks;

            if(smartMerges == 0) {
                int leftLen = lastMergeBlocks * blockLen;
                mergeForwards(array, offset, leftLen, lastFragment, blockLen, cmp);
            }
            else {
                this.mergeBlocksForwards(array, firstKey, medianKey, offset, smartMerges, blockLen,
                                         lastMergeBlocks, lastFragment, cmp);
            }

            //TODO: Why is this 'blockCount + 1'???
            insertSort(array, firstKey, blockCount, cmp);
            
            if(fullMerges % 2 == 0) {
                lastSubarrays--;
                rewindBuffer(array, offset - blockLen, offset + lastSubarrays - blockLen, offset + lastSubarrays);
            }
        }
        else {
            if(fastForwardLen == 0) {
                if(fullMerges % 2 != 0 && fullMerges != 1) {
                    // TODO: check arguments
                    rewindBuffer(array, offset - mergeLen - blockLen, offset - blockLen - 1, offset - 1);
                }
            }
            else {
                fastForwardBuffer(array, offset - blockLen, offset, offset + fastForwardLen);
            }
        }
        
        /*
        // ceil division of length over subarrayLen mod 4
        int mergeCount    = ((length - 1) / subarrayLen) + 1;
        int bufferControl = mergeCount % 4;
        
        if(bufferControl == 1) {
            
        }
        else if(bufferControl == 2) {
            
        }
        */
    }
    
    private void lazyCombine(T[] array, int firstKey, int start, int length, int subarrayLen, int blockLen) {
        Comparator<T> cmp = this.cmp; // local variable for performance à la Timsort

        int     fullMerge = 2 * subarrayLen;
        int    mergeCount = length / fullMerge;
        int    blockCount = fullMerge / blockLen;
        int lastSubarrays = length - (fullMerge * mergeCount);
        
        if(lastSubarrays <= subarrayLen) {
            length -= lastSubarrays;
            lastSubarrays = 0;
        }
        
        int leftBlocks = subarrayLen / blockLen;
        T   medianKey  = array[firstKey + leftBlocks];
        
        for(int mergeIndex = 0; mergeIndex < mergeCount; mergeIndex++) {
            int offset = start + (mergeIndex * fullMerge);
            
            sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, false, cmp);
            this.lazyMergeBlocks(array, firstKey, medianKey, offset, blockCount, blockLen, 0, 0, cmp);
            
            // TODO: Replace with Anon's key sort
            insertSort(array, firstKey, blockCount, cmp);
        }

        int offset = start + (mergeCount * fullMerge);
        
        if(lastSubarrays != 0) {
            blockCount = lastSubarrays / blockLen;

            sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, false, cmp);

            int lastFragment = lastSubarrays - (blockCount * blockLen);
            int lastMergeBlocks = 0;
            if (lastFragment != 0) {
                lastMergeBlocks = countLastMergeBlocks(array, offset, blockCount, blockLen, cmp);
            }

            int smartMerges = blockCount - lastMergeBlocks;

            if(smartMerges == 0) {
                int leftLen = lastMergeBlocks * blockLen;
                // TODO: double-check direction
                lazyMergeBackwards(array, offset, leftLen, lastFragment, cmp);
            }
            else {
                this.lazyMergeBlocks(array, firstKey, medianKey, offset, smartMerges, blockLen,
                                     lastMergeBlocks, lastFragment, cmp);
            }

            //TODO: Why is this 'blockCount + 1'???
            insertSort(array, firstKey, blockCount, cmp);
        }
    }
    
    private void combineBackwards(T[] array, int firstKey, int start, int length, int subarrayLen, int blockLen) {
        Comparator<T> cmp = this.cmp; // local variable for performance à la Timsort

        int      mergeLen = 2 * subarrayLen;
        int    fullMerges = length / mergeLen;
        int lastSubarrays = length - (mergeLen * fullMerges);
        
        if(lastSubarrays <= subarrayLen) {
            length -= lastSubarrays;
            lastSubarrays = 0;
        }
        
        int blockCount = lastSubarrays / blockLen;
        int leftBlocks = subarrayLen / blockLen;
        T   medianKey  = array[firstKey + leftBlocks];
        
        if(lastSubarrays != 0) {
            int offset = start + (fullMerges * mergeLen);
            
            if(lastSubarrays - subarrayLen <= blockLen) {
                mergeBackwards(array, offset, subarrayLen, lastSubarrays - subarrayLen, blockLen, cmp);
            }
            else {
                sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, true, cmp);

                int lastFragment = lastSubarrays - (blockCount * blockLen);

                this.mergeBlocksBackwards(array, firstKey, medianKey, offset, blockCount, blockLen,
                                          lastFragment, cmp);

                //TODO: Why is this 'blockCount + 1'???
                // We believe this '+ 1' is unnecessary and
                // possibly has a *hilarious* origin story
                insertSort(array, firstKey, blockCount, cmp);
            }
        }
        
        blockCount = mergeLen / blockLen;
        
        for(int mergeIndex = fullMerges - 1; mergeIndex >= 0; mergeIndex--) {
            int offset = start + (mergeIndex * mergeLen);
            
            sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, true, cmp);
            this.mergeBlocksBackwards(array, firstKey, medianKey, offset, blockCount, blockLen, 0, cmp);
            
            insertSort(array, firstKey, blockCount, cmp);
        }
    }
    
    private void combineForwardsOutOfPlace(T[] array, int firstKey, int start, int length, int subarrayLen, int blockLen) {
        Comparator<T> cmp = this.cmp; // local variable for performance à la Timsort

        int     fullMerge = 2 * subarrayLen;
        int    mergeCount = length / fullMerge;
        int    blockCount = fullMerge / blockLen;
        int lastSubarrays = length - (fullMerge * mergeCount);
        
        int resetLength = 0;
        if(lastSubarrays <= subarrayLen) {
            if(mergeCount % 2 != 0) {
                resetLength = lastSubarrays;
            }
            length -= lastSubarrays;
            lastSubarrays = 0;
        }
        
        int leftBlocks = subarrayLen / blockLen;
        T   medianKey  = array[firstKey + leftBlocks];
        
        for(int mergeIndex = 0; mergeIndex < mergeCount; mergeIndex++) {
            int offset = start + (mergeIndex * fullMerge);
            
            sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, false, cmp);
            this.mergeBlocksForwardsOutOfPlace(array, firstKey, medianKey, offset, blockCount, blockLen, 0, 0, cmp);
            
            insertSort(array, firstKey, blockCount, cmp);
        }

        int offset = start + (mergeCount * fullMerge);
        
        if(lastSubarrays != 0) {
            blockCount = lastSubarrays / blockLen;

            sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, false, cmp);

            int lastFragment = lastSubarrays - (blockCount * blockLen);
            int lastMergeBlocks = 0;
            if (lastFragment != 0) {
                lastMergeBlocks = countLastMergeBlocks(array, offset, blockCount, blockLen, cmp);
            }

            int smartMerges = blockCount - lastMergeBlocks;

            if(smartMerges == 0) {
                int leftLen = lastMergeBlocks * blockLen;
                mergeForwardsOutOfPlace(array, offset, leftLen, lastFragment, blockLen, cmp);
            }
            else {
                this.mergeBlocksForwardsOutOfPlace(array, firstKey, medianKey, offset, smartMerges, blockLen,
                                                   lastMergeBlocks, lastFragment, cmp);
            }

            //TODO: Why is this 'blockCount + 1'???
            insertSort(array, firstKey, blockCount, cmp);
            
            if(mergeCount % 2 == 0) {
                lastSubarrays--;
                rewindOutOfPlace(array, offset - blockLen, offset + lastSubarrays - blockLen, offset + lastSubarrays);
            }
        }
        else {
            if(resetLength == 0) {
                if(mergeCount % 2 != 0 && mergeCount != 1) {
                    // TODO: check arguments
                    rewindOutOfPlace(array, offset - fullMerge - blockLen, offset - blockLen - 1, offset - 1);
                }
            }
            else {
                fastForwardOutOfPlace(array, offset - blockLen, offset, offset + resetLength);
            }
        }
    }
    
    private void combineBackwardsOutOfPlace(T[] array, int firstKey, int start, int length, int subarrayLen, int blockLen) {
        Comparator<T> cmp = this.cmp; // local variable for performance à la Timsort

        int      mergeLen = 2 * subarrayLen;
        int    fullMerges = length / mergeLen;
        int lastSubarrays = length - (mergeLen * fullMerges);
        
        if(lastSubarrays <= subarrayLen) {
            length -= lastSubarrays;
            lastSubarrays = 0;
        }
        
        int blockCount = lastSubarrays / blockLen;
        int leftBlocks = subarrayLen / blockLen;
        T   medianKey  = array[firstKey + leftBlocks];
        
        if(lastSubarrays != 0) {
            int offset = start + (fullMerges * mergeLen);
            
            if(lastSubarrays - subarrayLen <= blockLen) {
                mergeBackwards(array, offset, subarrayLen, lastSubarrays - subarrayLen, blockLen, cmp);
            }
            else {
                sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, true, cmp);

                int lastFragment = lastSubarrays - (blockCount * blockLen);

                this.mergeBlocksBackwardsOutOfPlace(array, firstKey, medianKey, offset, blockCount, blockLen,
                                                    lastFragment, cmp);

                //TODO: Why is this 'blockCount + 1'???
                // We believe this '+ 1' is unnecessary and
                // possibly has a *hilarious* origin story
                insertSort(array, firstKey, blockCount, cmp);
            }
        }
        
        blockCount = mergeLen / blockLen;
        
        for(int mergeIndex = fullMerges - 1; mergeIndex >= 0; mergeIndex--) {
            int offset = start + (mergeIndex * mergeLen);
            
            sortBlocks(array, firstKey, offset, blockCount, leftBlocks, blockLen, true, cmp);
            this.mergeBlocksBackwardsOutOfPlace(array, firstKey, medianKey, offset, blockCount, blockLen, 0, cmp);
            
            insertSort(array, firstKey, blockCount, cmp);
        }
    }
    
    // 'keys' are on the left side of array. Blocks of length 'subarrayLen' combined. We'll combine them in pairs
    // 'subarrayLen' is a power of 2. (2 * subarrayLen / blockLen) keys are guaranteed
    private LocalMerge combineBlocks(T[] array, int start, int length, int bufferLen, int subarrayLen,
                                                int blockLen, int keyLen, boolean idealBuffer) {
        LocalMerge direction = LocalMerge.FORWARDS;
        subarrayLen *= 2;
        
        T[] extBuffer = this.extBuffer;
        
        if(idealBuffer) {
            if(extBuffer == null) {
                while((length - bufferLen) > subarrayLen) {
                    if(direction == LocalMerge.FORWARDS) {
                        this.combineForwards(array, start, start + bufferLen, length - bufferLen,
                                                    subarrayLen, blockLen);
                        direction = LocalMerge.BACKWARDS;
                    }
                    else {
                        this.combineBackwards(array, start, start + bufferLen, length - bufferLen,
                                                     subarrayLen, blockLen);
                        direction = LocalMerge.FORWARDS;
                    }
                    subarrayLen *= 2;
                }
            }
            else {
                while((length - bufferLen) > subarrayLen) {
                    if(direction == LocalMerge.FORWARDS) {
                        this.combineForwardsOutOfPlace(array, start, start + bufferLen, length - bufferLen,
                                                       subarrayLen, blockLen);
                        direction = LocalMerge.BACKWARDS;
                    }
                    else {
                        this.combineBackwardsOutOfPlace(array, start, start + bufferLen, length - bufferLen,
                                                        subarrayLen, blockLen);
                        direction = LocalMerge.FORWARDS;
                    }
                    subarrayLen *= 2;
                }
            }
        }
        else {
            int keyBuffer = keyLen / 2;
            
            if(extBuffer == null) {
                while(keyBuffer >= ((2 * subarrayLen) / keyBuffer)) {
                    if(direction == LocalMerge.FORWARDS) {
                        this.combineForwards(array, start, start + bufferLen, length - bufferLen,
                                                    subarrayLen, keyBuffer);
                        direction = LocalMerge.BACKWARDS;
                    }
                    else {
                        this.combineBackwards(array, start, start + bufferLen, length - bufferLen,
                                                     subarrayLen, keyBuffer);
                        direction = LocalMerge.FORWARDS;
                    }
                    subarrayLen *= 2;
                }
            }
            else {
                while(keyBuffer >= ((2 * subarrayLen) / keyBuffer)) {
                    if(direction == LocalMerge.FORWARDS) {
                        this.combineForwardsOutOfPlace(array, start, start + bufferLen, length - bufferLen,
                                                       subarrayLen, keyBuffer);
                        direction = LocalMerge.BACKWARDS;
                    }
                    else {
                        this.combineBackwardsOutOfPlace(array, start, start + bufferLen, length - bufferLen,
                                                        subarrayLen, keyBuffer);
                        direction = LocalMerge.FORWARDS;
                    }
                    subarrayLen *= 2;
                }
            }
            
            if(direction == LocalMerge.BACKWARDS) {
                resetBuffer(array, start + keyBuffer, length - keyBuffer, keyBuffer);
                direction = LocalMerge.FORWARDS;
            }
            
            while((length - bufferLen) > subarrayLen) {
                this.lazyCombine(array, start, start + bufferLen, length - bufferLen,
                                 subarrayLen, (2 * subarrayLen) / keyLen);
                subarrayLen *= 2;
            }
        }
        
        return direction;
    }
    
    // "Classic" in-place merge sort using binary searches and rotations
    // Forwards rotates the leftLen into the rightLen
    // cost: leftLen^2 + rightLen
    private static <T> void lazyMergeForwards(T[] array, int start, int leftLen, int rightLen, Comparator<T> cmp) {
        int middle = start + leftLen;

        while(leftLen != 0) {
            int mergeLen = binarySearchLeft(array, middle, rightLen, array[start], cmp);

            if(mergeLen != 0) {
                rotate(array, start, leftLen, mergeLen);

                start    += mergeLen;
                middle   += mergeLen;
                rightLen -= mergeLen;
            }

            if(rightLen == 0) {
                break;
            }
            else {
                do {
                    start++;
                    leftLen--;
                } while(leftLen != 0 && cmp.compare(array[start ],
                                                    array[middle]) <= 0);
            }
        }
    }
    
    // "Classic" in-place merge sort using binary searches and rotations
    // Backwards rotates the rigthLen into the leftLen
    // cost: rightLen^2 + leftLen
    private static <T> void lazyMergeBackwards(T[] array, int start, int leftLen, int rightLen, Comparator<T> cmp) {
        int end = start + leftLen + rightLen - 1;

        while(rightLen != 0) {            
            int mergeLen = binarySearchRight(array, start, leftLen, array[end], cmp);

            if(mergeLen != leftLen) {
                rotate(array, start + mergeLen, leftLen - mergeLen, rightLen);

                end     -=  leftLen - mergeLen;
                leftLen  = mergeLen;
            }

            if(leftLen == 0) {
                break;
            }
            else {
                int middle = start + leftLen;
                // TODO: Replace with galloping search
                do {
                    rightLen--;
                    end--;
                } while(rightLen != 0 && cmp.compare(array[middle - 1],
                                                     array[end       ]) <= 0);
            }
        }
    }
    
    private static <T> void lazyStableSort(T[] array, int start, int length, Comparator<T> cmp) {
        int i;
        for(i = 0; i <= length - 16; i += 16) {
            insertSort(array, i, 16, cmp);
        }
        insertSort(array, i, length - i, cmp);

        for(int mergeLen = 16; mergeLen < length; mergeLen *= 2) {
            int fullMerge = 2 * mergeLen;

            int mergeIndex;
            int mergeEnd = length - fullMerge;

            for(mergeIndex = 0; mergeIndex <= mergeEnd; mergeIndex += fullMerge) {
                lazyMergeBackwards(array, start + mergeIndex, mergeLen, mergeLen, cmp);
            }

            int leftOver = length - mergeIndex;
            if(leftOver > mergeLen) {
                lazyMergeBackwards(array, start + mergeIndex, mergeLen, leftOver - mergeLen, cmp);
            }
        }
    }
    
    void commonSort(T[] array, int start, int length, T[] extBuffer, int extBufferLen) {
        if(length < 16) {
            insertSort(array, start, length, this.cmp);
            return;
        }
        
        // smallest possible O(sqrt n) block length that
        // doesn't include arrays sorted by Insertion Sort
        int blockLen = 4;
        
        // find the smallest power of two greater than or
        // equal to the square root of the input's length
        while((blockLen * blockLen) < length) {
            blockLen *= 2;
        }
        
        // '((a - 1) / b) + 1' is actually a clever and very efficient
        // formula for the ceiling of (a / b)
        //
        // credit to Anonymous0726 for figuring this out!
        
        // TODO: WE DON'T NEED THIS CEILING?!?!?!?!?!?!?!?!?!?!?!?!?!
        int keyLen = ((length - 1) / blockLen) + 1;
        
        // Holy Grail is hoping to find '~2 sqrt n' unique items
        // throughout the array
        int idealKeys = keyLen + blockLen;
        
        int keysFound = collectKeys(array, start, length, idealKeys, this.cmp);
        
        boolean idealBuffer;
        if(keysFound < idealKeys) {
            
            // HOLY GRAIL STRATEGY 3
            // No block swaps or scrolling buffer; resort to Lazy Stable Sort
            if(keysFound < 4) {
            
                // if all items in the array equal each other,
                // then they're already sorted. done!
                if(keysFound == 1) return;
                
                lazyStableSort(array, start, length, this.cmp);
                return;
            }
            else {
                // HOLY GRAIL STRATEGY 2
                // Block swaps with small scrolling buffer and/or lazy merges
                keyLen = blockLen;
                blockLen = 0;
                idealBuffer = false;

                while(keyLen > keysFound) {
                    keyLen /= 2;
                }
            }
        }
        else {
            // HOLY GRAIL STRATEGY 1
            // Block swaps with scrolling buffer
            idealBuffer = true;
        }
        
        int bufferLen = blockLen + keyLen;
        int subarrayLen;
        if(idealBuffer) {
            subarrayLen = blockLen;
        }
        else {
            subarrayLen = keyLen;
        }
        
        if(extBuffer != null) {
            // GRAILSORT + EXTRA SPACE
            this.extBuffer    = extBuffer;
            this.extBufferLen = extBufferLen;
        }
        
        this.buildBlocks(array, start + bufferLen, length - bufferLen, subarrayLen, this.cmp);
        
        // TODO: Handle case where external buffer is not large enough for combine blocks
        
        LocalMerge direction = this.combineBlocks(array, start, length, bufferLen, subarrayLen,
                                                  blockLen, keyLen, idealBuffer);
        
        // TODO: Paste external buffer back into array
        
        // This 'if' case will always run during Strategy 2
        if(direction == LocalMerge.FORWARDS) {
            insertSort(array, start, bufferLen, this.cmp);
            lazyMergeForwards(array, start, bufferLen, length - bufferLen, this.cmp);
        }
        else {
            insertSort(array, start, keyLen, this.cmp);
            lazyMergeForwards(array, start, keyLen, length - keyLen, this.cmp);
            
            insertSort(array, start + length - blockLen, blockLen, this.cmp);
            lazyMergeBackwards(array, start, length - blockLen, blockLen, this.cmp);
        }
    }
}