// implementation of "block selection" sort
// based on andrey astrelin's original Grailsort implementation
private static <T> void sortBlocks(T[] array, int firstKey, int start, int keyCount, int blockLen, boolean sortByTail, Comparator<T> cmp) {
	int cmpIndex = sortByTail ? blockLen - 1 : 0;
	
	for(int blockIndex = 0, keyIndex = 0; keyIndex < keyCount; blockIndex += blockLen, keyIndex++) {
		int selectBlock = blockIndex;
		int selectKey   = keyIndex;
		
		for(int currBlock = selectBlock + blockLen, currKey = keyIndex + 1;
				currKey < keyCount; currBlock += blockLen, currKey++) {

			int compare = cmp.compare(array[start + currBlock + cmpIndex], array[start + selectBlock + cmpIndex]);               
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
}