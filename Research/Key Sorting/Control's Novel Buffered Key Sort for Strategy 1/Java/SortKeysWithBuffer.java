// a novel, elegant, and incredibly efficient 1.5 sqrt n key sort, courtesy of Control
// only works if a) median key is known and b) keys were permutated by "sortBlocks"
// (keys < medianKey and >= medianKey are each in relative sorted order, resembling a final radix sort pass)
private static <T> void sortKeys(T[] array, int firstKey, T medianKey, int keyCount, int buffer, Comparator<T> cmp) {
	int currKey     = firstKey;
	int bufferSwaps = 0;
	
	while(currKey < firstKey + keyCount) {
		if(cmp.compare(array[currKey], medianKey) < 0) {
			if(bufferSwaps != 0) {
				swap(array, currKey, currKey - bufferSwaps);
			}
		}
		else {
			swap(array, currKey, buffer + bufferSwaps);
			bufferSwaps++;
		}
		currKey++;
	}
	
	swapBlocksBackwards(array, currKey - bufferSwaps, buffer, bufferSwaps);
}