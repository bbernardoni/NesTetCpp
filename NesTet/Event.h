#pragma once

#include "LUT.h"

struct Event{
	int curPiece;
	int nextPiece;
	int rand;
	int waitFrames;
	int rotation;
	int totalFrames;

	int lastX;
	int lastY;
	int *lineClears;
	int numOfLineClears;

	Event(int nCurPiece, int nNextPiece, int nRand, int nTotalFrames, int nLastX, int nLastY):
		curPiece(nCurPiece),
		nextPiece(nNextPiece),
		rand(nRand),
		waitFrames(0),
		rotation(rotTable[nCurPiece]),
		totalFrames(nTotalFrames),
		lastX(nLastX),
		lastY(nLastY),
		lineClears(nullptr),
		numOfLineClears(0)
	{}
};

