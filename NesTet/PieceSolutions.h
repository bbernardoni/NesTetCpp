#pragma once

#include <cstdio>
#include <vector>
#include <queue>
#include <algorithm>
#include "State.h"
#include "LUT.h"

class PieceSolutions
{
public:
	PieceSolutions();
	
	void find(bool (*mem)[10], int linesToClear);
private:
	int lines;
	bool (*wellMem)[10];
	int solutions;
	
	State states[10][8][4];
	bool locked[12][4];
	std::queue<State*> queue;
	std::vector<State*> solutionStates;
	std::vector<int> solutionPieces;
	
	void search();
	void createStates();
	void resetStates();
	bool pieceClear2(int curPiece, int rotation, int x, int y);
	bool tileClear2(int x, int y);
	bool pieceClear(int curPiece, int rotation, int x, int y);
	bool tileClear(int x, int y);
	bool addChild(int tetriminoType, int x, int y, int rotation);
	void getLocks(int tetriminoType, std::vector<State*> *locks);
	void printSolution();
};
