#include <cstdio>
#include <cstdlib>

#include "Emu.h"

int counter;
bool wellMem[10][20];
bool rowsFinished[] = {false,false,false,false,false,false,false,false,false,false,false,false};//12 rows
int rowClearAnim;
const int seedArray[] = {0x8e2d,0x31c5,0xf463,0xfa31,0xbf46,0x97e8,0x1fcb,0x750f,0xd750,0x3d75,0x21eb,0xca43,0x9948,0x2652,0xa265,0x3a26,0xc744,0x38e8,0x41c7,0x1a0e,
						 0x4341,0x1434,0x0143,0x2028,0x8405,0x0840,0x0210,0x9021,0x2408,0x6240,0x2c48,0x8b12,0x9162,0xe645,0x1e64,0x8f32,0xd1e6,0x6f47,0x8de8,0xd946,
						 0xf651,0x3f65,0x4b4f,0x4969,0x824b,0x4824,0x9209,0xb241,0x3648,0xf364,0x7e6c,0x97e6,0xee25,0xbdc4,0x2f71,0x2f2f,0x05e5,0xe00b,0xbc01,0xebc0,
						 0xbd78,0x15eb,0xfe2b,0x5fc5,0x5357,0x4a6a,0x8253,0x8825,0xa209,0x5a20,0xab44,0xf568,0x3bd5,0xdcef,0x6773,0x14ce,0xd14c,0xda29,0x3b45,0xe768,
						 0x8fce,0xd047,0xf411,0x2f41,0xcbd0,0x0dcb,0x8c37,0xec61,0x7630,0x3763,0xa376,0xd546,0x7551,0x49d5,0x724e,0xce49,0x79c9,0xc3ce,0x1b0f,0x4361};
const char letterTable[] = {'T','J','Z','O','S','L','I'};
enum lt {T, J, Z, O, S, L, I};

Emu *emu = NULL;

void solveGame(int seedIndex, lt cur, lt next, int gh[], int ghLen);

void findPieceSolutions();

void displayGame(int seedIndex, lt cur, lt next);
void displayEmpty(int rand, int startFrame, lt cur, lt next);

void printNextRand(int rand, int nub);
void printNextBlocks(long input);

void printSeeds();
void analyzeSeed();	
int getRowsLeft();
int getRow(int row);

int main()
{
	//printNextRand(0x8E2D, 20);
	//printNextBlocks(0xACD312BA);
	//printSeeds();

	//displayGame(10, T, L); //Break at 884A totalFrames = $B1
	//displayGame(13, Z, I); //Break at 884A totalFrames = $B1
	//displayEmpty(0x7F31, 0x1C, L, T);

	system("pause");
	if(emu)
		delete emu;
	return 0;
}

void solveGame(int seedIndex, lt cur, lt next, int gh[], int ghLen) {
	int endRand = Emu::generateSeed(wellMem, seedArray[seedIndex]);
	counter = 0;
	for(int i = 0; i<10; i++){
		for(int j = 0; j<20; j++){
			if(wellMem[i][j]){
				counter++;
			}
		}
	}
	emu = new Emu(wellMem, cur, next, endRand, 50+seedIndex);
}

void printNextRand(int rand, int nub) {
	for(int i = 0; i<nub; i++){
		rand = Emu::rand(rand);
		printf("%x\n",rand);
	}
}

void printNextBlocks(long input) {
	int prevIndex = -1;
	int rand = (int) ((input >> 16) & 0xFFFF);
	char spawnID = (char) ((input >> 8) & 0xFF);
	char spawnCount = (char) (input & 0xFF);
	for(int i = 0; i < 20; i++){
		int index = Emu::getNextBlocks(rand, spawnID, spawnCount);
		if(prevIndex != index){
			printf("Seed=%x	rand cycles=%d	next block=%c (%x)\n", rand, i, letterTable[index], spawnTable[index]);
		}
		prevIndex = index;
		rand = Emu::rand(rand);
	}
}

void printSeeds() {
	int frames = 0;
	for(int i=0; i<100; i++){
		int rand = seedArray[i];
		Emu::generateSeed(wellMem, rand);
		counter = 0;
		for(int i = 0; i<10; i++){
			for(int j = 0; j<20; j++){
				if(wellMem[i][j]){
					counter++;
				}
			}
		}
		if(counter%4 == 2){
			analyzeSeed();
			printf("Seed=%x	wait frames=%d	# of pieces needed=%d	clear anim=%d\n", rand, frames, (250-counter)/4, rowClearAnim);
		}
		frames++;
	}
}

void analyzeSeed() {
	for(int i=0; i<12; i++){
		rowsFinished[i] = false;
	}
	rowClearAnim = 0;
	while(getRowsLeft() > 1){
		bool clearable = true;
		for(int i = 0; i < 10; i++){
			if(!wellMem[i][getRow(2)+8]){
				if(wellMem[i][getRow(1)+8]){
					bool tileClearable = false;
					for(int j=i-1; j>0; j--){
						if(!wellMem[j][getRow(1)+8] && !wellMem[j][getRow(2)+8] &&
								!wellMem[j-1][getRow(1)+8] && !wellMem[j-1][getRow(2)+8]){
							tileClearable = true;
							break;
						}
						else if(wellMem[j][getRow(2)+8]){
							break;
						}
					}
					for(int j=i+1; j<9; j++){
						if(!wellMem[j][getRow(1)+8] && !wellMem[j][getRow(2)+8] &&
								!wellMem[j+1][getRow(1)+8] && !wellMem[j+1][getRow(2)+8]){
							tileClearable = true;
							break;
						}
						else if(wellMem[j][getRow(2)+8]){
							break;
						}
					}
					if(!tileClearable){
						clearable = false;
					}
				}
			}
		}
		if(getRowsLeft() > 2){
			bool thirdClearable = true;
			for(int i = 0; i < 10; i++){
				if(!wellMem[i][getRow(3)+8]){
					if(wellMem[i][getRow(1)+8] || wellMem[i][getRow(2)+8]){
						thirdClearable = false;
					}
				}
			}
			if(thirdClearable){
				rowsFinished[getRow(3)] = true;
			}
		}
		if(clearable){
			rowsFinished[getRow(2)] = true;
		}
		rowsFinished[getRow(1)] = true;
		rowClearAnim++;
	}
	if(getRowsLeft() == 1){
		rowClearAnim++;
	}
}
	
int getRowsLeft() {
	int rowsLeft = 0;
	for(int i = 0; i < 12; i++){
		if(!rowsFinished[i]){
			rowsLeft++;
		}
	}
	return rowsLeft;
}
	
int getRow(int row) {
	int rowsLeft = 0;
	for(int i = 0; i < 12; i++){
		if(!rowsFinished[i]){
			rowsLeft++;
			if(rowsLeft == row){
				return i;
			}
		}
	}
	return -1;
}