static inline void FUNC(holyGrailSwap)(VAR* a, VAR* b) {
    VAR tmp = *a;
    *a = *b;
    *b = tmp;
}

static inline void FUNC(holyGrailBlockSwap)(VAR* a, VAR* b, size_t blockLen) {
    VAR tmp;
    for (size_t i = 0; i < blockLen; i++) {
        tmp = *a;
        *a++ = *b;
        *b++ = tmp;
    }
}

static inline void FUNC(holyGrailRotate)(VAR* start, size_t leftLen, size_t rightLen) {
    while (leftLen > 0 && rightLen > 0) {
        if (leftLen <= rightLen) {
            FUNC(holyGrailBlockSwap)(start, start + leftLen, leftLen);
            start    += leftLen;
            rightLen -= leftLen;
        } else {
            FUNC(holyGrailBlockSwap)(start + leftLen - rightLen, start + leftLen, rightLen);
            leftLen -= rightLen;
        }
    }
}

static void FUNC(holyGrailInsertSort)(VAR* start, VAR* end, GRAILCMP cmp) {
    for (VAR* item = start + 1; item < end; item++) {
        VAR* left = item - 1;
        VAR* right = item;

        while (left >= start && cmp(left, right) > 0) {
            VAR tmp = *left;
            *left-- = *right;
            *right-- = tmp;
        }
    }
}

static size_t FUNC(holyGrailBinarySearchLeft)(VAR* start, size_t length, VAR* target, GRAILCMP cmp) {
    size_t  left = 0;
    size_t right = length;

    while (left < right) {
        size_t middle = left + ((right - left) / 2);

        if (cmp(start + middle, target) < 0) {
            left = middle + 1;
        } else {
            right = middle;
        }
    }
    return left;
}

static size_t FUNC(holyGrailBinarySearchRight)(VAR* start, size_t length, VAR* target, GRAILCMP cmp) {
    size_t  left = 0;
    size_t right = length;

    while (left < right) {
        size_t middle = left + ((right - left) / 2);

        if (cmp(start + middle, target) > 0) {
            right = middle;
        } else {
            left = middle + 1;
        }
    }

    return right;
}

static size_t FUNC(holyGrailCollectKeys)(VAR* start, VAR* end, size_t idealKeys, GRAILCMP cmp) {
    size_t keysFound = 1;
    VAR*    firstKey = start;
    VAR*     currKey = start + 1;

    while (currKey < end && keysFound < idealKeys) {
        size_t insertPos = FUNC(holyGrailBinarySearchLeft)(firstKey, keysFound, currKey, cmp);

        if (insertPos == keysFound || cmp(currKey, firstKey + insertPos) != 0) {
            FUNC(holyGrailRotate)(firstKey, keysFound, currKey - (firstKey + keysFound));

            firstKey = currKey - keysFound;

            VAR tmp = firstKey[keysFound];
            memmove(firstKey + insertPos + 1, firstKey + insertPos, sizeof(VAR) * (keysFound - insertPos));
            firstKey[insertPos] = tmp;

            keysFound++;
        }

        currKey++;
    }

    FUNC(holyGrailRotate)(start, firstKey - start, keysFound);
    return keysFound;
}

static void FUNC(holyGrailPairwiseSwaps)(VAR* start, size_t length, GRAILCMP cmp) {
    VAR tmp;
    size_t index;
    for (index = 1; index < length; index += 2) {
        VAR*  left = start + index - 1;
        VAR* right = start + index;

        if (cmp(left, right) > 0) {
            tmp = left[-2];
            left[-2] = *right;
            *right = tmp;

            tmp = right[-2];
            right[-2] = *left;
            *left = tmp;
        } else {
            tmp = left[-2];
            left[-2] = *left;
            *left = tmp;

            tmp = right[-2];
            right[-2] = *right;
            *right = tmp;
        }
    }

    VAR* left = start + index - 1;
    if (left < start + length) {
        tmp = left[-2];
        left[-2] = *left;
        *left = tmp;
    }
}

static void FUNC(holyGrailPairwiseWrites)(VAR* start, size_t length, GRAILCMP cmp) {
    size_t index;
    for (index = 1; index < length; index += 2) {
        VAR*  left = start + index - 1;
        VAR* right = start + index;

        if (cmp(left, right) > 0) {
            left[ -2] = *right;
            right[-2] = *left;
        } else {
            left[ -2] = *left;
            right[-2] = *right;
        }
    }

    VAR* left = start + index - 1;
    if (left < start + length) {
        left[-2] = *left;
    }
}

static void FUNC(holyGrailMergeForwards)(VAR* start, size_t leftLen, size_t rightLen, size_t bufferOffset, GRAILCMP cmp) {
    VAR* buffer = start  - bufferOffset;
    VAR*   left = start;
    VAR* middle = start  + leftLen;
    VAR*  right = middle;
    VAR*    end = middle + rightLen;

    VAR tmp;
    while (right < end) {
        if (left == middle || cmp(left, right) > 0) {
            tmp = *buffer;
            *buffer++ = *right;
            *right++ = tmp;
        } else {
            tmp = *buffer;
            *buffer++ = *left;
            *left++ = tmp;
        }
    }

    if (buffer != left) {
        FUNC(holyGrailBlockSwap)(buffer, left, middle - left);
    }
}

static void FUNC(holyGrailMergeBackwards)(VAR* start, size_t leftLen, size_t rightLen, size_t bufferOffset, GRAILCMP cmp) {
    VAR*    end = start - 1;
    VAR*   left = end   + leftLen;
    VAR* middle = left;
    VAR*  right = middle + rightLen;
    VAR* buffer = right + bufferOffset;

    VAR tmp;
    while (left > end) {
        if (right == middle || cmp(left, right) > 0) {
            tmp = *buffer;
            *buffer-- = *left;
            *left-- = tmp;
        } else {
            tmp = *buffer;
            *buffer-- = *right;
            *right-- = tmp;
        }
    }

    if (right != buffer) {
        while (right > middle) {
            tmp = *buffer;
            *buffer-- = *right;
            *right-- = tmp;
        }
    }
}

static void FUNC(holyGrailMergeOutOfPlace)(VAR* start, size_t leftLen, size_t rightLen, size_t bufferOffset, GRAILCMP cmp) {
    VAR* buffer = start  - bufferOffset;
    VAR*   left = start;
    VAR* middle = start  + leftLen;
    VAR* right = middle;
    VAR*   end = middle + rightLen;

    while (right < end) {
        if (left == middle || cmp(left, right) > 0) {
            *buffer++ = *right++;
        } else {
            *buffer++ = *left++;
        }
    }

    if (buffer != left) {
        while (left < middle) {
            *buffer++ = *left++;
        }
    }
}

static void FUNC(holyGrailBuildInPlace)(VAR* start, size_t length, size_t currentLen, size_t bufferLen, GRAILCMP cmp) {
    for (size_t mergeLen = currentLen; mergeLen < bufferLen; mergeLen *= 2) {
        size_t fullMerge = 2 * mergeLen;

        VAR* mergeIndex;
        VAR* mergeEnd = start + length - fullMerge;
        size_t bufferOffset = mergeLen;

        for (mergeIndex = start; mergeIndex <= mergeEnd; mergeIndex += fullMerge) {
            FUNC(holyGrailMergeForwards)(mergeIndex, mergeLen, mergeLen, bufferOffset, cmp);
        }

        size_t leftOver = length - (mergeIndex - start);

        if (leftOver > mergeLen) {
            FUNC(holyGrailMergeForwards)(mergeIndex, mergeLen, leftOver - mergeLen, bufferOffset, cmp);
        } else {
            FUNC(holyGrailRotate)(mergeIndex - mergeLen, mergeLen, leftOver);
        }

        start -= mergeLen;
    }

    size_t fullMerge  = 2 * bufferLen;
    size_t lastBlock  = length & (fullMerge - 1); // equivalent to modulo, but faster (only works if fullMerge is a power of 2, which it is)
    VAR*   lastOffset = start + length - lastBlock;

    if (lastBlock <= bufferLen) {
        FUNC(holyGrailRotate)(lastOffset, lastBlock, bufferLen);
    } else {
        FUNC(holyGrailMergeBackwards)(lastOffset, bufferLen, lastBlock - bufferLen, bufferLen, cmp);
    }

    for (VAR* mergeIndex = lastOffset - fullMerge; mergeIndex >= start; mergeIndex -= fullMerge) {
        FUNC(holyGrailMergeBackwards)(mergeIndex, bufferLen, bufferLen, bufferLen, cmp);
    }
}

static void FUNC(holyGrailBuildOutOfPlace)(VAR* start, size_t length, size_t bufferLen, VAR* extBuffer, size_t extLen, GRAILCMP cmp) {
    memcpy(extBuffer, start - extLen, sizeof(VAR) * extLen);

    FUNC(holyGrailPairwiseWrites)(start, length, cmp);
    start -= 2;

    size_t mergeLen;
    for (mergeLen = 2; mergeLen < extLen; mergeLen *= 2) {
        size_t fullMerge = 2 * mergeLen;

        VAR* mergeIndex;
        VAR* mergeEnd = start + length - fullMerge;
        size_t bufferOffset = mergeLen;

        for (mergeIndex = start; mergeIndex <= mergeEnd; mergeIndex += fullMerge) {
            FUNC(holyGrailMergeOutOfPlace)(mergeIndex, mergeLen, mergeLen, bufferOffset, cmp);
        }

        size_t leftOver = length - (mergeIndex - start);

        if (leftOver > mergeLen) {
            FUNC(holyGrailMergeOutOfPlace)(mergeIndex, mergeLen, leftOver - mergeLen, bufferOffset, cmp);
        }
        else {
            memmove(mergeIndex - mergeLen, mergeIndex, sizeof(VAR) * leftOver);
        }

        start -= mergeLen;
    }

    memcpy(start + length, extBuffer, sizeof(VAR) * extLen);
    FUNC(holyGrailBuildInPlace)(start, length, mergeLen, bufferLen, cmp);
}

static void FUNC(holyGrailBuildBlocks)(VAR* start, size_t length, size_t bufferLen, VAR* extBuffer, size_t extBufferLen, GRAILCMP cmp) {
    if (extBuffer != NULL) {
        size_t extLen;

        if (bufferLen < extBufferLen) {
            extLen = bufferLen;
        } else {
            extLen = 1;
            while ((extLen * 2) <= extBufferLen) {
                extLen *= 2;
            }
        }

        FUNC(holyGrailBuildOutOfPlace)(start, length, bufferLen, extBuffer, extLen, cmp);
    } else {
        FUNC(holyGrailPairwiseSwaps)(start, length, cmp);
        FUNC(holyGrailBuildInPlace)(start - 2, length, 2, bufferLen, cmp);
    }
}

static size_t FUNC(holyGrailBlockSelectSort)(VAR* firstKey, VAR* start, size_t medianKey, size_t blockCount, size_t blockLen, GRAILCMP cmp) {
    for (size_t firstBlock = 0; firstBlock < blockCount; firstBlock++) {
        size_t selectBlock = firstBlock;

        for (size_t currBlock = firstBlock + 1; currBlock < blockCount; currBlock++) {
            int compare = cmp(start + (currBlock * blockLen), start + (selectBlock * blockLen));

            if (compare < 0 || (compare == 0 && cmp(firstKey + currBlock, firstKey + selectBlock) < 0)) {
                selectBlock = currBlock;
            }
        }

        if (selectBlock != firstBlock) {
            FUNC(holyGrailBlockSwap)(start + (firstBlock * blockLen), start + (selectBlock * blockLen), blockLen);

            FUNC(holyGrailSwap)(firstKey + firstBlock, firstKey + selectBlock);

            if (medianKey == firstBlock) {
                medianKey = selectBlock;
            } else if (medianKey == selectBlock) {
                medianKey = firstBlock;
            }
        }
    }

    return medianKey;
}

static inline void FUNC(holyGrailInPlaceBufferReset)(VAR* start, size_t length, size_t bufferOffset) {
    VAR* buffer =  start + length - 1;
    VAR*  index = buffer - bufferOffset;

    VAR tmp;
    while (buffer >= start) {
        tmp = *buffer;
        *buffer-- = *index;
        *index-- = tmp;
    }
}

static inline void FUNC(holyGrailOutOfPlaceBufferReset)(VAR* start, size_t length, size_t bufferOffset) {
    VAR* buffer =  start + length - 1;
    VAR*  index = buffer - bufferOffset;

    while (buffer >= start) {
        *buffer-- = *index--;
    }
}

static inline void FUNC(holyGrailInPlaceBufferRewind)(VAR* start, VAR* leftBlock, VAR* buffer) {
    VAR tmp;
    while (leftBlock >= start) {
        tmp = *buffer;
        *buffer-- = *leftBlock;
        *leftBlock-- = tmp;
    }
}

static inline void FUNC(holyGrailOutOfPlaceBufferRewind)(VAR* start, VAR* leftBlock, VAR* buffer) {
    while (leftBlock >= start) {
        *buffer-- = *leftBlock--;
    }
}

static inline HolyGrailSubarray FUNC(holyGrailGetSubarray)(VAR* currentKey, VAR* medianKey, GRAILCMP cmp) {
    if (cmp(currentKey, medianKey) < 0) {
        return HOLY_GRAIL_SUBARRAY_LEFT;
    } else {
        return HOLY_GRAIL_SUBARRAY_RIGHT;
    }
}

static size_t FUNC(holyGrailCountLastMergeBlocks)(VAR* offset, size_t blockCount, size_t blockLen, GRAILCMP cmp) {
    size_t blocksToMerge = 0;

    VAR* lastRightFrag = offset + (blockCount * blockLen);
    VAR* prevLeftBlock = lastRightFrag - blockLen;

    while (blocksToMerge < blockCount && cmp(lastRightFrag, prevLeftBlock) < 0) {
        blocksToMerge++;
        prevLeftBlock -= blockLen;
    }

    return blocksToMerge;
}

static void FUNC(holyGrailSmartLazyMerge)(VAR* start, size_t leftLen, HolyGrailSubarray leftOrigin, size_t rightLen, HolyGrailState* state, GRAILCMP cmp) {
    VAR* middle = start + leftLen;

    if (leftOrigin == HOLY_GRAIL_SUBARRAY_LEFT) {
        if (cmp(middle - 1, middle) > 0) {
            while (leftLen != 0) {
                size_t mergeLen = FUNC(holyGrailBinarySearchLeft)(middle, rightLen, start, cmp);

                if (mergeLen != 0) {
                    FUNC(holyGrailRotate)(start, leftLen, mergeLen);

                    start    += mergeLen;
                    middle   += mergeLen;
                    rightLen -= mergeLen;
                }

                if (rightLen == 0) {
                    state->currBlockLen = leftLen;
                    return;
                } else {
                    do {
                        start++;
                        leftLen--;
                    } while (leftLen != 0 && cmp(start, middle) <= 0);
                }
            }
        }
    } else {
        if (cmp(middle - 1, middle) >= 0) {
            while (leftLen != 0) {
                size_t mergeLen = FUNC(holyGrailBinarySearchRight)(middle, rightLen, start, cmp);

                if (mergeLen != 0) {
                    FUNC(holyGrailRotate)(start, leftLen, mergeLen);

                    start    += mergeLen;
                    middle   += mergeLen;
                    rightLen -= mergeLen;
                }

                if (rightLen == 0) {
                    state->currBlockLen = leftLen;
                    return;
                } else {
                    do {
                        start++;
                        leftLen--;
                    } while (leftLen != 0 && cmp(start, middle) < 0);
                }
            }
        }
    }

    HolyGrailState s = *state;
    s.currBlockLen = rightLen;
    if (leftOrigin == HOLY_GRAIL_SUBARRAY_LEFT) {
        s.currBlockOrigin = HOLY_GRAIL_SUBARRAY_RIGHT;
    } else {
        s.currBlockOrigin = HOLY_GRAIL_SUBARRAY_LEFT;
    }
    *state = s;
}

static void FUNC(holyGrailLazyMerge)(VAR* start, size_t leftLen, size_t rightLen, GRAILCMP cmp) {
    if (leftLen < rightLen) {
        VAR* middle = start + leftLen;

        while (leftLen != 0) {
            size_t mergeLen = FUNC(holyGrailBinarySearchLeft)(middle, rightLen, start, cmp);

            if (mergeLen != 0) {
                FUNC(holyGrailRotate)(start, leftLen, mergeLen);

                start    += mergeLen;
                middle   += mergeLen;
                rightLen -= mergeLen;
            }

            if (rightLen == 0) {
                break;
            } else {
                do {
                    start++;
                    leftLen--;
                } while (leftLen != 0 && cmp(start, middle) <= 0);
            }
        }
    } else {
        VAR* end = start + leftLen + rightLen - 1;

        while (rightLen != 0) {
            size_t mergeLen = FUNC(holyGrailBinarySearchRight)(start, leftLen, end, cmp);

            if (mergeLen != leftLen) {
                FUNC(holyGrailRotate)(start + mergeLen, leftLen - mergeLen, rightLen);

                end    -= leftLen - mergeLen;
                leftLen = mergeLen;
            }

            if (leftLen == 0) {
                break;
            } else {
                VAR* middle = start + leftLen;
                do {
                    rightLen--;
                    end--;
                } while (rightLen != 0 && cmp(middle - 1, end) <= 0);
            }
        }
    }
}

static void FUNC(holyGrailSmartMerge)(VAR* start, size_t leftLen, HolyGrailSubarray leftOrigin, size_t rightLen, size_t bufferOffset, HolyGrailState* state, GRAILCMP cmp) {
    VAR* buffer = start  - bufferOffset;
    VAR*   left = start;
    VAR* middle = start  + leftLen;
    VAR*  right = middle;
    VAR*    end = middle + rightLen;

    VAR tmp;
    if (leftOrigin == HOLY_GRAIL_SUBARRAY_LEFT) {
        while (left < middle && right < end) {
            if (cmp(left, right) <= 0) {
                tmp = *buffer;
                *buffer++ = *left;
                *left++ = tmp;
            } else {
                tmp = *buffer;
                *buffer++ = *right;
                *right++ = tmp;
            }
        }
    } else {
        while (left < middle && right < end) {
            if (cmp(left, right) <  0) {
                tmp = *buffer;
                *buffer++ = *left;
                *left++ = tmp;
            } else {
                tmp = *buffer;
                *buffer++ = *right;
                *right++ = tmp;
            }
        }
    }

    if (left < middle) {
        state->currBlockLen = middle - left;
        FUNC(holyGrailInPlaceBufferRewind)(left, middle - 1, end - 1);
    } else {
        HolyGrailState s = *state;
        s.currBlockLen = end - right;
        if (leftOrigin == HOLY_GRAIL_SUBARRAY_LEFT) {
            s.currBlockOrigin = HOLY_GRAIL_SUBARRAY_RIGHT;
        } else {
            s.currBlockOrigin = HOLY_GRAIL_SUBARRAY_LEFT;
        }
        *state = s;
    }
}

static void FUNC(holyGrailSmartMergeOutOfPlace)(VAR* start, size_t leftLen, HolyGrailSubarray leftOrigin, size_t rightLen, size_t bufferOffset, HolyGrailState* state, GRAILCMP cmp) {
    VAR* buffer = start  - bufferOffset;
    VAR*   left = start;
    VAR* middle = start  + leftLen;
    VAR*  right = middle;
    VAR*    end = middle + rightLen;

    if (leftOrigin == HOLY_GRAIL_SUBARRAY_LEFT) {
        while (left < middle && right < end) {
            if (cmp(left, right) <= 0) {
                *buffer++ = *left++;
            } else {
                *buffer++ = *right++;
            }
        }
    } else {
        while (left < middle && right < end) {
            if (cmp(left, right) <  0) {
                *buffer++ = *left++;
            } else {
                *buffer++ = *right++;
            }
        }
    }

    if (left < middle) {
        state->currBlockLen = middle - left;
        FUNC(holyGrailOutOfPlaceBufferRewind)(left, middle - 1, end - 1);
    } else {
        HolyGrailState s = *state;
        s.currBlockLen = end - right;
        if (leftOrigin == HOLY_GRAIL_SUBARRAY_LEFT) {
            s.currBlockOrigin = HOLY_GRAIL_SUBARRAY_RIGHT;
        } else {
            s.currBlockOrigin = HOLY_GRAIL_SUBARRAY_LEFT;
        }
        *state = s;
    }
}

static void FUNC(holyGrailMergeBlocks)(VAR* firstKey, VAR* medianKey, VAR* start, size_t blockCount, size_t blockLen, size_t lastMergeBlocks, size_t lastLen, HolyGrailState* state, GRAILCMP cmp) {
    VAR* buffer;

    VAR* currBlock;
    VAR* nextBlock = start + blockLen;

    HolyGrailState s = *state; // copy here for speed
    s.currBlockLen    = blockLen;
    s.currBlockOrigin = FUNC(holyGrailGetSubarray)(firstKey, medianKey, cmp);

    for (size_t keyIndex = 1; keyIndex < blockCount; keyIndex++, nextBlock += blockLen) {
        HolyGrailSubarray nextBlockOrigin;

        currBlock = nextBlock - s.currBlockLen;
        nextBlockOrigin = FUNC(holyGrailGetSubarray)(firstKey + keyIndex, medianKey, cmp);

        if (nextBlockOrigin == s.currBlockOrigin) {
            buffer = currBlock - blockLen;

            FUNC(holyGrailBlockSwap)(buffer, currBlock, s.currBlockLen);
            s.currBlockLen = blockLen;
        } else {
            FUNC(holyGrailSmartMerge)(currBlock, s.currBlockLen, s.currBlockOrigin, blockLen, blockLen, &s, cmp);
        }
    }

    currBlock = nextBlock - s.currBlockLen;
    buffer    = currBlock - blockLen;

    if (lastLen != 0) {
        if (s.currBlockOrigin == HOLY_GRAIL_SUBARRAY_RIGHT) {
            FUNC(holyGrailBlockSwap)(buffer, currBlock, s.currBlockLen);

            currBlock         = nextBlock;
            s.currBlockLen    = blockLen * lastMergeBlocks;
            s.currBlockOrigin = HOLY_GRAIL_SUBARRAY_LEFT;
        } else {
            s.currBlockLen += blockLen * lastMergeBlocks;
        }

        FUNC(holyGrailMergeForwards)(currBlock, s.currBlockLen, lastLen, blockLen, cmp);
    } else {
        FUNC(holyGrailBlockSwap)(buffer, currBlock, s.currBlockLen);
    }

    *state = s;
}

static void FUNC(holyGrailLazyMergeBlocks)(VAR* firstKey, VAR* medianKey, VAR* start, size_t blockCount, size_t blockLen, size_t lastMergeBlocks, size_t lastLen, HolyGrailState* state, GRAILCMP cmp) {
    VAR* currBlock;
    VAR* nextBlock = start + blockLen;

    HolyGrailState s = *state; // copy here for speed
    s.currBlockLen    = blockLen;
    s.currBlockOrigin = FUNC(holyGrailGetSubarray)(firstKey, medianKey, cmp);

    for (size_t keyIndex = 1; keyIndex < blockCount; keyIndex++, nextBlock += blockLen) {
        HolyGrailSubarray nextBlockOrigin;

        currBlock = nextBlock - s.currBlockLen;
        nextBlockOrigin = FUNC(holyGrailGetSubarray)(firstKey + keyIndex, medianKey, cmp);

        if (nextBlockOrigin == s.currBlockOrigin) {
            s.currBlockLen = blockLen;
        } else {
            if (blockLen != 0 && s.currBlockLen != 0) {
                FUNC(holyGrailSmartLazyMerge)(currBlock, s.currBlockLen, s.currBlockOrigin, blockLen, &s, cmp);
            }
        }
    }

    currBlock = nextBlock - s.currBlockLen;

    if (lastLen != 0) {
        if (s.currBlockOrigin == HOLY_GRAIL_SUBARRAY_RIGHT) {
            currBlock         = nextBlock;
            s.currBlockLen    = blockLen * lastMergeBlocks;
            s.currBlockOrigin = HOLY_GRAIL_SUBARRAY_LEFT;
        } else {
            s.currBlockLen += blockLen * lastMergeBlocks;
        }

        FUNC(holyGrailLazyMerge)(currBlock, s.currBlockLen, lastLen, cmp);
    }

    *state = s;
}

static void FUNC(holyGrailMergeBlocksOutOfPlace)(VAR* firstKey, VAR* medianKey, VAR* start, size_t blockCount, size_t blockLen, size_t lastMergeBlocks, size_t lastLen, HolyGrailState* state, GRAILCMP cmp) {
    VAR* buffer;

    VAR* currBlock;
    VAR* nextBlock = start + blockLen;

    HolyGrailState s = *state; // copy here for speed
    s.currBlockLen    = blockLen;
    s.currBlockOrigin = FUNC(holyGrailGetSubarray)(firstKey, medianKey, cmp);

    for (size_t keyIndex = 1; keyIndex < blockCount; keyIndex++, nextBlock += blockLen) {
        HolyGrailSubarray nextBlockOrigin;

        currBlock       = nextBlock - s.currBlockLen;
        nextBlockOrigin = FUNC(holyGrailGetSubarray)(firstKey + keyIndex, medianKey, cmp);

        if (nextBlockOrigin == s.currBlockOrigin) {
            buffer = currBlock - blockLen;

            memmove(buffer, currBlock, sizeof(VAR) * s.currBlockLen);
            s.currBlockLen = blockLen;
        } else {
            FUNC(holyGrailSmartMergeOutOfPlace)(currBlock, s.currBlockLen, s.currBlockOrigin, blockLen, blockLen, &s, cmp);
        }
    }

    currBlock = nextBlock - s.currBlockLen;
    buffer    = currBlock - blockLen;

    if (lastLen != 0) {
        if (s.currBlockOrigin == HOLY_GRAIL_SUBARRAY_RIGHT) {
            memmove(buffer, currBlock, sizeof(VAR) * s.currBlockLen);

            currBlock         = nextBlock;
            s.currBlockLen    = blockLen * lastMergeBlocks;
            s.currBlockOrigin = HOLY_GRAIL_SUBARRAY_LEFT;
        } else {
            s.currBlockLen += blockLen * lastMergeBlocks;
        }

        FUNC(holyGrailMergeOutOfPlace)(currBlock, s.currBlockLen, lastLen, blockLen, cmp);
    } else {
        memmove(buffer, currBlock, sizeof(VAR) * s.currBlockLen);
    }

    *state = s;
}

static void FUNC(holyGrailCombineInPlace)(VAR* firstKey, VAR* start, size_t length, size_t subarrayLen, size_t blockLen, size_t mergeCount, size_t lastSubarrays, bool buffer, HolyGrailState* state, GRAILCMP cmp) {
    size_t fullMerge  = 2 * subarrayLen;
    size_t blockCount = fullMerge / blockLen;

    for (size_t mergeIndex = 0; mergeIndex < mergeCount; mergeIndex++) {
        VAR* offset = start + (mergeIndex * fullMerge);

        FUNC(holyGrailInsertSort)(firstKey, firstKey + blockCount, cmp);

        size_t medianKey = subarrayLen / blockLen;
        medianKey = FUNC(holyGrailBlockSelectSort)(firstKey, offset, medianKey, blockCount, blockLen, cmp);

        if (buffer) {
            FUNC(holyGrailMergeBlocks)(firstKey, firstKey + medianKey, offset, blockCount, blockLen, 0, 0, state, cmp);
        } else {
            FUNC(holyGrailLazyMergeBlocks)(firstKey, firstKey + medianKey, offset, blockCount, blockLen, 0, 0, state, cmp);
        }
    }

    if (lastSubarrays != 0) {
        VAR* offset = start + (mergeCount * fullMerge);
        blockCount = lastSubarrays / blockLen;

        FUNC(holyGrailInsertSort)(firstKey, firstKey + blockCount + 1, cmp);

        size_t medianKey = subarrayLen / blockLen;
        medianKey = FUNC(holyGrailBlockSelectSort)(firstKey, offset, medianKey, blockCount, blockLen, cmp);

        size_t lastFragment = lastSubarrays - (blockCount * blockLen);
        size_t lastMergeBlocks;
        if (lastFragment != 0) {
            lastMergeBlocks = FUNC(holyGrailCountLastMergeBlocks)(offset, blockCount, blockLen, cmp);
        } else {
            lastMergeBlocks = 0;
        }

        size_t smartMerges = blockCount - lastMergeBlocks;

        if (smartMerges == 0) {
            size_t leftLen = lastMergeBlocks * blockLen;

            if (buffer) {
                FUNC(holyGrailMergeForwards)(offset, leftLen, lastFragment, blockLen, cmp);
            } else {
                FUNC(holyGrailLazyMerge)(offset, leftLen, lastFragment, cmp);
            }
        } else {
            if (buffer) {
                FUNC(holyGrailMergeBlocks)(firstKey, firstKey + medianKey, offset, smartMerges, blockLen, lastMergeBlocks, lastFragment, state, cmp);
            } else {
                FUNC(holyGrailLazyMergeBlocks)(firstKey, firstKey + medianKey, offset, smartMerges, blockLen, lastMergeBlocks, lastFragment, state, cmp);
            }
        }
    }

    if (buffer) {
        FUNC(holyGrailInPlaceBufferReset)(start, length, blockLen);
    }
}

static void FUNC(holyGrailCombineOutOfPlace)(VAR* firstKey, VAR* start, size_t length, size_t subarrayLen, size_t blockLen, size_t mergeCount, size_t lastSubarrays, VAR* extBuffer, HolyGrailState* state, GRAILCMP cmp) {
    memcpy(extBuffer, start - blockLen, sizeof(VAR) * blockLen);

    size_t fullMerge  = 2 * subarrayLen;
    size_t blockCount = fullMerge / blockLen;

    for (size_t mergeIndex = 0; mergeIndex < mergeCount; mergeIndex++) {
        VAR* offset = start + (mergeIndex * fullMerge);

        FUNC(holyGrailInsertSort)(firstKey, firstKey + blockCount, cmp);

        size_t medianKey = subarrayLen / blockLen;
        medianKey = FUNC(holyGrailBlockSelectSort)(firstKey, offset, medianKey, blockCount, blockLen, cmp);

        FUNC(holyGrailMergeBlocksOutOfPlace)(firstKey, firstKey + medianKey, offset, blockCount, blockLen, 0, 0, state, cmp);
    }

    if (lastSubarrays != 0) {
        VAR* offset = start + (mergeCount * fullMerge);
        blockCount = lastSubarrays / blockLen;

        FUNC(holyGrailInsertSort)(firstKey, firstKey + blockCount + 1, cmp);

        size_t medianKey = subarrayLen / blockLen;
        medianKey = FUNC(holyGrailBlockSelectSort)(firstKey, offset, medianKey, blockCount, blockLen, cmp);

        size_t lastFragment = lastSubarrays - (blockCount * blockLen);
        size_t lastMergeBlocks;
        if (lastFragment != 0) {
            lastMergeBlocks = FUNC(holyGrailCountLastMergeBlocks)(offset, blockCount, blockLen, cmp);
        } else {
            lastMergeBlocks = 0;
        }

        size_t smartMerges = blockCount - lastMergeBlocks;

        if (smartMerges == 0) {
            size_t leftLen = lastMergeBlocks * blockLen;

            FUNC(holyGrailMergeOutOfPlace)(offset, leftLen, lastFragment, blockLen, cmp);
        } else {
            FUNC(holyGrailMergeBlocksOutOfPlace)(firstKey, firstKey + medianKey, offset, smartMerges, blockLen, lastMergeBlocks, lastFragment, state, cmp);
        }
    }

    FUNC(holyGrailOutOfPlaceBufferReset)(start, length, blockLen);
    memcpy(start - blockLen, extBuffer, sizeof(VAR) * blockLen);
}

static void FUNC(holyGrailCombineBlocks)(VAR* firstKey, VAR* start, size_t length, size_t subarrayLen, size_t blockLen, bool buffer, VAR* extBuffer, size_t extBufferLen, HolyGrailState* state, GRAILCMP cmp) {
    size_t     fullMerge = 2 * subarrayLen;
    size_t    mergeCount = length /  fullMerge;
    size_t lastSubarrays = length - (fullMerge * mergeCount);

    if (lastSubarrays <= subarrayLen) {
        length -= lastSubarrays;
        lastSubarrays = 0;
    }

    if (buffer && blockLen <= extBufferLen) {
        FUNC(holyGrailCombineOutOfPlace)(firstKey, start, length, subarrayLen, blockLen, mergeCount, lastSubarrays, extBuffer, state, cmp);
    } else {
        FUNC(holyGrailCombineInPlace)(firstKey, start, length, subarrayLen, blockLen, mergeCount, lastSubarrays, buffer, state, cmp);
    }
}

static void FUNC(holyGrailLazyStableSort)(VAR* start, size_t length, GRAILCMP cmp) {
    VAR* end = start + length;
    for (VAR *left = start, *right = start + 1; right < end; left += 2, right += 2) {
        if (cmp(left, right) > 0) {
            VAR tmp = *left;
            *left = *right;
            *right = tmp;
        }
    }
    for (size_t mergeLen = 2; mergeLen < length; mergeLen *= 2) {
        size_t fullMerge = 2 * mergeLen;

        size_t mergeIndex;
        size_t mergeEnd = length - fullMerge + 1;

        for (mergeIndex = 0; mergeIndex <= mergeEnd; mergeIndex += fullMerge) {
            FUNC(holyGrailLazyMerge)(start + mergeIndex, mergeLen, mergeLen, cmp);
        }

        size_t leftOver = length - mergeIndex;
        if (leftOver > mergeLen) {
            FUNC(holyGrailLazyMerge)(start + mergeIndex, mergeLen, leftOver - mergeLen, cmp);
        }
    }
}

static void FUNC(holyGrailCommonSort)(VAR* start, size_t length, VAR* extBuffer, size_t extBufferLen, GRAILCMP cmp) {
    VAR* end = start + length;
    if (length < 16) {
        FUNC(holyGrailInsertSort)(start, end, cmp);
        return;
    }

    size_t blockLen = 1;

    while ((blockLen * blockLen) < length) {
        blockLen *= 2;
    }

    size_t keyLen = ((length - 1) / blockLen) + 1;

    size_t idealKeys = keyLen + blockLen;

    size_t keysFound = FUNC(holyGrailCollectKeys)(start, end, idealKeys, cmp);

    bool idealBuffer;
    if (keysFound < idealKeys) {
        if (keysFound < 4) {
            FUNC(holyGrailLazyStableSort)(start, length, cmp);
            return;
        } else {
            keyLen = blockLen;
            blockLen = 0;
            idealBuffer = false;

            while (keyLen > keysFound) {
                keyLen /= 2;
            }
        }
    } else {
        idealBuffer = true;
    }

    size_t bufferEnd = blockLen + keyLen;
    size_t subarrayLen;
    if (idealBuffer) {
        subarrayLen = blockLen;
    } else {
        subarrayLen = keyLen;
    }

    FUNC(holyGrailBuildBlocks)(start + bufferEnd, length - bufferEnd, subarrayLen, extBuffer, extBufferLen, cmp);

    HolyGrailState state;
    while ((length - bufferEnd) > (2 * subarrayLen)) {
        subarrayLen *= 2;

        size_t currentBlockLen = blockLen;
        bool scrollingBuffer = idealBuffer;

        if (!idealBuffer) {
            size_t keyBuffer = keyLen / 2;

            if (keyBuffer >= ((2 * subarrayLen) / keyBuffer)) {
                currentBlockLen = keyBuffer;
                scrollingBuffer = true;
            }
            else {
                currentBlockLen = (2 * subarrayLen) / keyLen;
            }
        }

        FUNC(holyGrailCombineBlocks)(start, start + bufferEnd, length - bufferEnd, subarrayLen, currentBlockLen, scrollingBuffer, extBuffer, extBufferLen, &state, cmp);
    }

    FUNC(holyGrailInsertSort)(start, start + bufferEnd, cmp);
    FUNC(holyGrailLazyMerge)(start, bufferEnd, length - bufferEnd, cmp);
}
