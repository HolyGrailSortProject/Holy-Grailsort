#ifndef HOLY_GRAIL_SORT_H
#define HOLY_GRAIL_SORT_H

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include <assert.h>
#include <errno.h>

#ifdef __INTELLISENSE__
    #pragma diag_suppress 28 // This suppresses the "expression must have a constant value" on line 181
#endif

#define HOLY_GRAIL_STATIC_EXT_BUFFER_LEN 512

typedef int GRAILCMP(const void *a, const void *b);

typedef enum {
	HOLY_GRAIL_SUBARRAY_LEFT,
	HOLY_GRAIL_SUBARRAY_RIGHT
} HolyGrailSubarray;

typedef struct {
    size_t currBlockLen;
	HolyGrailSubarray currBlockOrigin;
} HolyGrailState;

//////////////////////////////////////////////////////////
//┌────────────────────────────────────────────────────┐//
//│                █████┐    ██████┐ ██████┐████████┐  │//
//│               ██┌──██┐   ██┌──██┐└─██┌─┘└──██┌──┘  │//
//│               └█████┌┘   ██████┌┘  ██│     ██│     │//
//│               ██┌──██┐   ██┌──██┐  ██│     ██│     │//
//│               └█████┌┘   ██████┌┘██████┐   ██│     │//
//│                └────┘    └─────┘ └─────┘   └─┘     │//
//└────────────────────────────────────────────────────┘//
//////////////////////////////////////////////////////////

#undef VAR
#undef FUNC
#undef STRUCT

#define VAR char
#define FUNC(NAME) NAME##8
#define STRUCT(NAME) struct NAME##8

#include "holy_grail_sort.c"

//////////////////////////////////////////////////////////
//┌────────────────────────────────────────────────────┐//
//│           ▄██┐   █████┐    ██████┐ ██████┐████████┐│//
//│          ████│  ██┌───┘    ██┌──██┐└─██┌─┘└──██┌──┘│//
//│          └─██│  ██████┐    ██████┌┘  ██│     ██│   │//
//│            ██│  ██┌──██┐   ██┌──██┐  ██│     ██│   │//
//│          ██████┐└█████┌┘   ██████┌┘██████┐   ██│   │//
//│          └─────┘ └────┘    └─────┘ └─────┘   └─┘   │//
//└────────────────────────────────────────────────────┘//
//////////////////////////////////////////////////////////

#undef VAR
#undef FUNC
#undef STRUCT

#define VAR short
#define FUNC(NAME) NAME##16
#define STRUCT(NAME) struct NAME##16

#include "holy_grail_sort.c"

//////////////////////////////////////////////////////////
// ┌───────────────────────────────────────────────────┐//
// │       ██████┐ ██████┐    ██████┐ ██████┐████████┐ │//
// │       └────██┐└────██┐   ██┌──██┐└─██┌─┘└──██┌──┘ │//
// │        █████┌┘ █████┌┘   ██████┌┘  ██│     ██│    │//
// │        └───██┐██┌───┘    ██┌──██┐  ██│     ██│    │//
// │       ██████┌┘███████┐   ██████┌┘██████┐   ██│    │//
// │       └─────┘ └──────┘   └─────┘ └─────┘   └─┘    │//
// └───────────────────────────────────────────────────┘//
//////////////////////////////////////////////////////////

#undef VAR
#undef FUNC
#undef STRUCT

#define VAR int
#define FUNC(NAME) NAME##32
#define STRUCT(NAME) struct NAME##32

#include "holy_grail_sort.c"

//////////////////////////////////////////////////////////
// ┌───────────────────────────────────────────────────┐//
// │        █████┐ ██┐  ██┐   ██████┐ ██████┐████████┐ │//
// │       ██┌───┘ ██│  ██│   ██┌──██┐└─██┌─┘└──██┌──┘ │//
// │       ██████┐ ███████│   ██████┌┘  ██│     ██│    │//
// │       ██┌──██┐└────██│   ██┌──██┐  ██│     ██│    │//
// │       └█████┌┘     ██│   ██████┌┘██████┐   ██│    │//
// │        └────┘      └─┘   └─────┘ └─────┘   └─┘    │//
// └───────────────────────────────────────────────────┘//
//////////////////////////////////////////////////////////

#undef VAR
#undef FUNC
#undef STRUCT

#define VAR long long
#define FUNC(NAME) NAME##64
#define STRUCT(NAME) struct NAME##64

#include "holy_grail_sort.c"

#ifndef _WIN32
//////////////////////////////////////////////////////////
//┌────────────────────────────────────────────────────┐//
//│  ▄██┐  ██████┐  █████┐    ██████┐ ██████┐████████┐ │//
//│ ████│  └────██┐██┌──██┐   ██┌──██┐└─██┌─┘└──██┌──┘ │//
//│ └─██│   █████┌┘└█████┌┘   ██████┌┘  ██│     ██│    │//
//│   ██│  ██┌───┘ ██┌──██┐   ██┌──██┐  ██│     ██│    │//
//│ ██████┐███████┐└█████┌┘   ██████┌┘██████┐   ██│    │//
//│ └─────┘└──────┘ └────┘    └─────┘ └─────┘   └─┘    │//
//└────────────────────────────────────────────────────┘//
//////////////////////////////////////////////////////////

#undef VAR
#undef FUNC
#undef STRUCT

#define VAR long double
#define FUNC(NAME) NAME##128
#define STRUCT(NAME) struct NAME##128

#include "holy_grail_sort.c"
#endif


////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//┌──────────────────────────────────────────────────────────────────────────────────────────────────────────┐//
//│   ██╗  ██╗ ██████╗ ██╗  ██╗   ██╗ ██████╗ ██████╗  █████╗ ██╗██╗     ███████╗ ██████╗ ██████╗ ████████╗  │//
//│   ██║  ██║██╔═══██╗██║  ╚██╗ ██╔╝██╔════╝ ██╔══██╗██╔══██╗██║██║     ██╔════╝██╔═══██╗██╔══██╗╚══██╔══╝  │//
//│   ███████║██║   ██║██║   ╚████╔╝ ██║  ███╗██████╔╝███████║██║██║     ███████╗██║   ██║██████╔╝   ██║     │//
//│   ██╔══██║██║   ██║██║    ╚██╔╝  ██║   ██║██╔══██╗██╔══██║██║██║     ╚════██║██║   ██║██╔══██╗   ██║     │//
//│   ██║  ██║╚██████╔╝███████╗██║   ╚██████╔╝██║  ██║██║  ██║██║███████╗███████║╚██████╔╝██║  ██║   ██║     │//
//│   ╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝    ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚══════╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝     │//
//└──────────────────────────────────────────────────────────────────────────────────────────────────────────┘//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////


void holyGrailCommonSort(void *array, size_t nelements, void *extBuffer, size_t extBufferLen, size_t elemsize, GRAILCMP *cmp)
{
	if (nelements < 2)
	{
		return;
	}

	switch (elemsize)
	{
		case sizeof(char):
			return holyGrailCommonSort8(array, nelements, extBuffer, extBufferLen, cmp);

		case sizeof(short):
			return holyGrailCommonSort16(array, nelements, extBuffer, extBufferLen, cmp);

		case sizeof(int):
			return holyGrailCommonSort32(array, nelements, extBuffer, extBufferLen, cmp);

		case sizeof(long long):
			return holyGrailCommonSort64(array, nelements, extBuffer, extBufferLen, cmp);

#ifndef _WIN32
		case sizeof(long double):
			return holyGrailCommonSort128(array, nelements, extBuffer, extBufferLen, cmp);
#endif

		default:
			return assert(elemsize == sizeof(char) || elemsize == sizeof(short) || elemsize == sizeof(int) || elemsize == sizeof(long long) || elemsize == sizeof(long double));
	}
}

void holyGrailSortInPlace(void *array, size_t nelements, size_t elemsize, GRAILCMP *cmp) {
    holyGrailCommonSort(array, nelements, NULL, 0, elemsize, cmp);
}

void holyGrailSortStaticOOP(void *array, size_t nelements, size_t elemsize, GRAILCMP *cmp) {
    char buffer[HOLY_GRAIL_STATIC_EXT_BUFFER_LEN * elemsize];
    holyGrailCommonSort(array, nelements, (void*)buffer, HOLY_GRAIL_STATIC_EXT_BUFFER_LEN, elemsize, cmp);
}

void holyGrailSortDynamicOOP(void *array, size_t nelements, size_t elemsize, GRAILCMP *cmp) {
    size_t bufferLen = 1;
    while (bufferLen * bufferLen < nelements) {
        bufferLen *= 2;
    }
    void* buffer = malloc(bufferLen * elemsize);
    holyGrailCommonSort(array, nelements, buffer, bufferLen, elemsize, cmp);
    free(buffer);
}

#undef VAR
#undef FUNC
#undef STRUCT

#endif
