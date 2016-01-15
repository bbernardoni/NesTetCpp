#pragma once

#include <climits>
#include <vector>
#include <queue>
#include <algorithm>
#include "State.h"
#include "Emu.h"
#include "LUT.h"

class AI
{
public:
	AI(Emu *emu, int pGarbageHeights[], int garbageHeightsLen, int maxPieces);
	
	void start();
	void search();

	void lineClear();
private:
	State states[10][20][4];
	bool locked[12][4];
	std::queue<State*> queue;
	Emu *emu;

	int *garbageHeights;
	int garbageHeightsLen;
	int *multiGH;
	int multiGHLen;
	int garbageHeightsIndex;
	int nubPieces;
	int maxPieces;
	int minFrames;
	int minClearFrames[4];
	int minClearMax;

	void createStates();
	void resetStates();

	bool addChild(int tetriminoType, int x, int y, int rotation);
	void getLocks(std::vector<State*> *locks);

	bool analyze(State *state, int lineClear, int lineLength);
	bool badHole(int x, int y);
};