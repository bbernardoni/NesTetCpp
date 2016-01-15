#pragma once

#include <vector>
#include <cstdio>
#include "LUT.h"
#include "Event.h"

class Emu
{
public:
	Emu(bool mem[10][20], int curPiece, int nextPiece, int rand, int totalFrames);
	
	void printGame();
	
	void placePiece();
	int* placePiece(int px, int py, int rotation);
	int* placePiece(int px, int py);

	bool incCurWaitFrames();
	bool incWaitFrames();
	void decWaitFrames();
	
	void rotClockwise();
	void rotCounterClockwise();

	void undoPlace();

	int getCurPieceX(int tile);
	int getCurPieceY(int tile);
	int getNextPieceX(int tile);
	int getNextPieceY(int tile);

	bool isCellFilled(int x, int y);
	bool isCellFilledSafe(int x, int y);

	bool curPieceClear();
	static bool pieceClear(bool wellMem[10][20], int curPiece, int rotation, int x, int y);
	static bool tileClear(bool wellMem[10][20], int x, int y);
	bool pieceClear2(int curPiece, int rotation, int x, int y);
	bool tileClear2(int x, int y);
	
	static int getNextBlocks(int rand, char spawnID, char spawnCount);
	static int generateSeed(bool wellMem[10][20], int rand);
	static int rand(int rand);
	
	std::vector<Event> hist; //TJZOSLI
	int totalFrames;
	int linesCleared;
	int px;
	int py;

private:
	bool (*wellMem)[20];
	int tempRand;

	bool testLine(int line);
	int getNextPiece(int height, bool lineClear);
	void updateNextPiece();
};