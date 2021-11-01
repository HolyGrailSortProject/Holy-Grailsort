static inline void FUNC(holyGrailInsertBackwards)(VAR* start, VAR* index) {
    VAR item = *index;
    memmove(start + 1, start, (index - start) * sizeof(VAR));
    *start = item;
}

static inline void FUNC(holyGrailInsertForwards)(VAR* start, VAR* index) {
    VAR item = *start;
    memmove(start, start + 1, (index - start) * sizeof(VAR));
    *index = item;
}

static inline void FUNC(holyGrailSwap)(VAR* a, VAR* b) {
    VAR temp = *a;
    *a = *b;
    *b = temp;
}

static inline void FUNC(holyGrailSwapBlocksBackwards)(VAR* a, VAR* b, size_t blockLen) {
    for (size_t i = blockLen - 1; i != SIZE_MAX; i--) {
        FUNC(holyGrailSwap)(a + i, b + i);
    }
}

static inline void FUNC(holyGrailSwapBlocksForwards)(VAR* a, VAR* b, size_t blockLen) {
    for (size_t i = 0; i < blockLen; i++) {
        FUNC(holyGrailSwap)(a + i, b + i);
    }
}

static inline void FUNC(holyGrailRotate)(VAR* start, size_t leftLen, size_t rightLen) {
    size_t minLen = leftLen < rightLen ? leftLen : rightLen;

    while (minLen > 1) {
        if (leftLen > rightLen) {
            do {
                FUNC(holyGrailSwapBlocksBackwards)(start + leftLen - rightLen, start + leftLen, rightLen);
                leftLen -= rightLen;
            } while (leftLen > rightLen);

            minLen = leftLen;
        } else {
            do {
                FUNC(holyGrailSwapBlocksForwards)(start, start + leftLen, leftLen);
                start += leftLen;
                rightLen -= leftLen;
            } while (leftLen <= rightLen);

            minLen = rightLen;
        }
    }

    if (minLen == 1) {
        if (leftLen == 1) {
            FUNC(holyGrailInsertForwards)(start, start + rightLen);
        } else {
            FUNC(holyGrailInsertBackwards)(start, start + leftLen);
        }
    }
}

static size_t FUNC(holyGrailBinarySearchRight)(VAR* start, VAR* end, VAR* target, HOLY_GRAIL_CMP cmp) {
    VAR* left = start;
    VAR* right = end;

    while (left < right) {
        VAR* middle = left + ((right - left) >> 1);

        if (cmp(middle, target) > 0) {
            right = middle;
        } else {
            left = middle + 1;
        }
    }
    return right - start;
}

static void FUNC(holyGrailInsertSort)(VAR* start, VAR *end, HOLY_GRAIL_CMP cmp) {
    VAR temp, *i, *i1;
    for (VAR* item = start + 1; item < end; item++) {
        temp = *item;
        i = item;
        i1 = item - 1;

        if (cmp(i1, &temp) <= 0) {
            continue;
        }

        if (cmp(start, &temp) > 0) {
            FUNC(holyGrailInsertBackwards)(start, item);
            continue;
        }

        do {
            *i-- = *i1--;
        } while (cmp(i1, &temp) > 0);

        *i = temp;
    }
}

static size_t FUNC(holyGrailBinarySearchExclusive)(VAR* start, VAR* end, VAR* target, HOLY_GRAIL_CMP cmp) {
    VAR* left = start;
    VAR* right = end;

    while (left < right) {
        VAR* middle = left + ((right - left) >> 1);

        int comp = cmp(middle, target);
        if (comp == 0) {
            return SIZE_MAX;
        } else if (comp < 0) {
            left = middle + 1;
        } else {
            right = middle;
        }
    }
    return left - start;
}

static size_t FUNC(holyGrailCollectKeys)(VAR* start, VAR* end, size_t idealKeys, HOLY_GRAIL_CMP cmp) {
    size_t keysFound = 1;
    VAR* firstKey = start;
    VAR* currKey = start + 1;

    while (currKey < end && keysFound < idealKeys) {
        size_t insertPos = FUNC(holyGrailBinarySearchExclusive)(firstKey, firstKey + keysFound, currKey, cmp);

        if (insertPos != SIZE_MAX) {
            FUNC(holyGrailRotate)(firstKey, keysFound, currKey - (firstKey + keysFound));

            firstKey = currKey - keysFound;

            if (keysFound != insertPos) {
                FUNC(holyGrailInsertBackwards)(firstKey + insertPos, firstKey + keysFound);
            }

            keysFound++;
        }
        currKey++;
    }

    FUNC(holyGrailRotate)(start, firstKey - start, keysFound);
    return keysFound;
}

static void FUNC(holyGrailLazyMergeBackwards)(VAR* start, size_t leftLen, size_t rightLen, HOLY_GRAIL_CMP cmp) {
    VAR* end = start + leftLen + rightLen - 1;

    while (rightLen != 0) {
        size_t mergeLen = FUNC(holyGrailBinarySearchRight)(start, start + leftLen, end, cmp);

        if (mergeLen != leftLen) {
            FUNC(holyGrailRotate)(start + mergeLen, leftLen - mergeLen, rightLen);

            end -= leftLen - mergeLen;
            leftLen = mergeLen;
        }

        if (leftLen == 0) {
            break;
        } else {
            VAR* middle = start + leftLen;
            // TODO: Replace with galloping search
            do {
                rightLen--;
                end--;
            } while (rightLen != 0 && cmp(middle - 1, end) <= 0);
        }
    }
}

static void FUNC(holyGrailLazyStableSort)(VAR* start, VAR* end, size_t length, HOLY_GRAIL_CMP cmp) {
    VAR* i;
    for (i = start; i <= end - 16; i += 16) {
        FUNC(holyGrailInsertSort)(i, i + 16, cmp);
    }
    FUNC(holyGrailInsertSort)(i, end, cmp);

    size_t fullMerge;
    for (size_t mergeLen = 16; mergeLen < length; mergeLen = fullMerge) {
        fullMerge = 2 * mergeLen;

        VAR* mergeIndex;
        VAR* mergeEnd = end - fullMerge;

        for (mergeIndex = start; mergeIndex <= mergeEnd; mergeIndex += fullMerge) {
            FUNC(holyGrailLazyMergeBackwards)(mergeIndex, mergeLen, mergeLen, cmp);
        }

        size_t leftOver = end - mergeIndex;
        if (leftOver > mergeLen) {
            FUNC(holyGrailLazyMergeBackwards)(mergeIndex, mergeLen, leftOver - mergeLen, cmp);
        }
    }
}

static void FUNC(holyGrailCommonSort)(VAR* start, size_t length, VAR* extBuffer, size_t extBufferLen, HOLY_GRAIL_CMP cmp) {
    VAR *end = start + length;
    if (length < 16) {
        FUNC(holyGrailInsertSort)(start, end, cmp);
        return;
    }

    size_t blockLen = 4;

    while (blockLen * blockLen < length) {
        blockLen *= 2;
    }

    // TODO: We don't need this ceiling???
    size_t keyLen = ((length - 1) / blockLen) + 1;

    size_t idealKeys = keyLen + blockLen;

    size_t keysFound = FUNC(holyGrailCollectKeys)(start, end, idealKeys, cmp);

    bool idealBuffer;
    if (keysFound < idealKeys) {
        if (keysFound < 4) {
            if (keysFound == 1) return;

            FUNC(holyGrailLazyStableSort)(start, end, length, cmp);
            return;
        }
    }
}
