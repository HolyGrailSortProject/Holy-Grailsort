// implementation of "smart block selection" sort
// code inspired by Anonymous0726
// TODO: possibly fixed???
private static <T> void sortBlocks(T[] array, int firstKey, int start, int blockCount, int leftBlocks, int blockLen, boolean sortByTail, Comparator<T> cmp) {
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
	
	int lastKey = firstKey + blockCount - 1;
	int scrambledEnd = rightKey < lastKey ? rightKey + 1 : rightKey;

	// phase two: replace the entire left subarray with blocks in sorted order from the
	//            scrambled area, keeping track of the rightmost block swapped
	while(keyIndex < rightKey) {
		int selectBlock = rightBlock;
		int selectKey   = rightKey;

		int currBlock   = rightBlock + blockLen;

		for(int currKey = rightKey + 1; currKey <= scrambledEnd; currKey++, currBlock += blockLen) {
			int compare = cmp.compare(array[currBlock + cmpIndex], array[selectBlock + cmpIndex]);
			if (compare < 0 || (compare == 0 && cmp.compare(array[  currKey],
															array[selectKey]) < 0)) {
				selectBlock = currBlock;
				selectKey   = currKey;
			}
		}

		swapBlocksForwards(array, blockIndex, selectBlock, blockLen);
		swap(array, keyIndex, selectKey);

		if(selectKey == scrambledEnd && scrambledEnd < lastKey) scrambledEnd++;

		blockIndex += blockLen;
		keyIndex++;
	}
	
	// phase three: after the left subarray has been sorted, keep finding the next block in order
	//              from the scrambled area until either (a) the scrambled area runs out of blocks,
	//              meaning the rest are sorted, or (b) the scrambled area hits the end of the right
	//              subarray
	while(scrambledEnd < lastKey) {
		int selectBlock = blockIndex;
		int selectKey   = keyIndex;

		int currBlock   = blockIndex + blockLen;

		for(int currKey = keyIndex + 1; currKey <= scrambledEnd; currKey++, currBlock += blockLen) {
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

			if(selectKey == scrambledEnd) scrambledEnd++;
		}

		blockIndex += blockLen;
		keyIndex++;
		
		if(keyIndex == scrambledEnd) return;
	}
	
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
}