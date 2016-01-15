#include "Emu.h"

Emu::Emu(bool mem[10][20], int curPiece, int nextPiece, int rand, int totalFrames)
{
	px = -1;
	py = -1;
	linesCleared = 0;
	hist.push_back(Event(curPiece, nextPiece, rand, totalFrames, 0, 0));
	wellMem = mem;
}

void Emu::printGame() {
	int lastPieceFrames = (abs(hist.back().lastX-5)-1)*2;
	lastPieceFrames = (lastPieceFrames<0)? 0: lastPieceFrames;
	printf("$B1=%x TotalFrames=%d\n", hist.at(hist.size()-2).totalFrames, (hist.at(hist.size()-2).totalFrames+263+lastPieceFrames));
	printf("px py rotation waitFrames\n");
	for(int i=1; i<hist.size(); i++){
		Event prev = hist.at(i-1);
		Event cur = hist.at(i);
		printf("%x %x %d %d\n", cur.lastX, cur.lastY, prev.rotation, cur.waitFrames);
	}
}

void Emu::placePiece() {
	placePiece(px/32, py/32);
}

int* Emu::placePiece(int px, int py, int rotation) {
	hist.back().rotation = rotation;
	return placePiece(px, py);
}
	
int* Emu::placePiece(int px, int py) {
	for(int i=0; i<4; i++){
        wellMem[tPos[hist.back().curPiece][hist.back().rotation][i][0]+px]
        		[tPos[hist.back().curPiece][hist.back().rotation][i][1]+py] = true;
    }
    std::vector<int> lineClears;
    int minLine = py-2;
    if(minLine<0){
    	minLine = 0;
    }
    int maxLine = py+3;
    if(maxLine>20){
    	maxLine = 20;
    }
    for(int line=minLine; line<maxLine; line++){
    	if(testLine(line)){
    		lineClears.push_back(line);
        	for(int y=line; y>0; y--){
        		for(int x=0; x<10; x++){
        			wellMem[x][y] = wellMem[x][y-1];
        	    }
        	}
        	for(int x=0; x<10; x++){
    			wellMem[x][0] = false;
    	    }
        }
    }
    int index = getNextPiece(py, lineClears.size()>0);
	hist.push_back(Event(hist.back().nextPiece, index, tempRand, totalFrames, px, py));
	if(lineClears.size()>0){
		linesCleared += lineClears.size();
		int numOfLineClears = lineClears.size();
		int *ret = new int[numOfLineClears];
		for(int i=0; i<numOfLineClears; i++)
			ret[i] = lineClears.at(numOfLineClears-i-1);
		hist.back().lineClears = ret;
		hist.back().numOfLineClears = numOfLineClears;
		return ret;
	}
	return NULL;
}
	
bool Emu::incCurWaitFrames() {
	if(hist.size() <= 2){
		return false;
	}
	if(hist.at(hist.size()-2).waitFrames>=10){
		return false;
	}
	Event curE = hist.back();
	hist.pop_back();
	hist.back().waitFrames++;
	int nRand = hist.back().rand;
	for(int i = 0; i < hist.back().waitFrames; i++){
		nRand = rand(nRand);
	}
	hist.back().nextPiece = getNextBlocks(nRand, spawnTable[hist.back().curPiece], (char)hist.size());
	int randCycles = (6-(curE.lastY+2)/4)*2+curE.lastY*2+9;
	nRand = hist.back().rand;
	if(hist.size() == 1){
		randCycles--;
	} else {
		for(int i = 0; i < hist.back().waitFrames; i++){
			nRand = rand(nRand);
		}
		int index = ((nRand >> 8) + hist.size() + 1) & 7;
		if (index == 7 || index == hist.back().curPiece) {
			nRand = rand(nRand);
		}
	}
	int totalFrames = hist.back().totalFrames + randCycles + hist.back().waitFrames;
	if(curE.lineClears != NULL){
		int lineClearFrames = 20 - ((totalFrames-6)%4);
		randCycles += lineClearFrames;
		totalFrames += lineClearFrames;
	}
	for(int i = 0; i < randCycles; i++){
		nRand = rand(nRand);
	}
	curE.curPiece = hist.back().nextPiece;
	curE.nextPiece = getNextBlocks(nRand, spawnTable[hist.back().nextPiece], (char) (hist.size()+1));
	curE.rand = nRand;
	curE.totalFrames = totalFrames;
	curE.waitFrames = 0;
	hist.push_back(curE);
	return true;
}
	
bool Emu::incWaitFrames(){
	if(hist.back().waitFrames<10 && hist.size() > 1){
		hist.back().waitFrames++;
		updateNextPiece();
		return true;
	}
	return false;
}
	
void Emu::decWaitFrames(){
	if(hist.back().waitFrames>0 && hist.size() > 1){
		hist.back().waitFrames--;
		updateNextPiece();
	}
}
	
void Emu::rotClockwise(){
	if(hist.back().rotation<numOfRot[hist.back().curPiece]-1){
		hist.back().rotation++;
	}else{
		hist.back().rotation=0;
	}
}
	
void Emu::rotCounterClockwise(){
	if(hist.back().rotation>0){
		hist.back().rotation--;
	}else{
		hist.back().rotation=numOfRot[hist.back().curPiece]-1;
	}
}

void Emu::undoPlace() {
	if(hist.size()>1){
		Event oldE = hist.back();
		hist.pop_back();
		if(oldE.lineClears != NULL){
			linesCleared -= oldE.numOfLineClears;
			for(int i=0; i<oldE.numOfLineClears; i++){
				int line = oldE.lineClears[i];
				for(int y=0; y<line; y++){
	        		for(int x=0; x<10; x++){
	        			wellMem[x][y] = wellMem[x][y+1];
	        	    }
	        	}
	        	for(int x=0; x<10; x++){
	    			wellMem[x][line] = true;
	    	    }
		    }
		}
		for(int i=0; i<4; i++){
            wellMem[tPos[hist.back().curPiece][hist.back().rotation][i][0]+oldE.lastX]
            		[tPos[hist.back().curPiece][hist.back().rotation][i][1]+oldE.lastY] = false;
        }
	}
}
	
int Emu::getCurPieceX(int tile){
	return tPos[hist.back().curPiece][hist.back().rotation][tile][0]+px/32;
}
	
int Emu::getCurPieceY(int tile){
	return tPos[hist.back().curPiece][hist.back().rotation][tile][1]+py/32;
}
	
int Emu::getNextPieceX(int tile){
	return tPos[hist.back().nextPiece][rotTable[hist.back().nextPiece]][tile][0];
}
	
int Emu::getNextPieceY(int tile){
	return tPos[hist.back().nextPiece][rotTable[hist.back().nextPiece]][tile][1];
}

bool Emu::isCellFilled(int x, int y) {
	return wellMem[x][y];
}

bool Emu::isCellFilledSafe(int x, int y) {
	if(x<0||x>9||y<0||y>19){
		return true;
	}
	return wellMem[x][y];
}
    
bool Emu::curPieceClear(){
    return pieceClear(wellMem, hist.back().curPiece, hist.back().rotation, px/32, py/32);
}
	
bool Emu::pieceClear(bool wellMem[10][20], int curPiece, int rotation, int x, int y){
    for(int i=0; i<4; i++){
        if(!tileClear(wellMem, tPos[curPiece][rotation][i][0]+x, tPos[curPiece][rotation][i][1]+y)){
        	return false;
        }
    }
    return true;
}
	
bool Emu::tileClear(bool wellMem[10][20], int x, int y){
    if(x < 0 || y < 0 || x >= 10 || y >= 20){
    	return false;
    }
    return !wellMem[x][y];
}
	
bool Emu::pieceClear2(int curPiece, int rotation, int x, int y){
    for(int i=0; i<4; i++){
        if(!tileClear2(tPos[curPiece][rotation][i][0]+x, tPos[curPiece][rotation][i][1]+y)){
        	return false;
        }
    }
    return true;
}
	
bool Emu::tileClear2(int x, int y){
    if(y < 0){
    	return true;
    }
    return !wellMem[x][y];
}

bool Emu::testLine(int line) {
	for(int i=0; i<10; i++){
		if(!wellMem[i][line]){
	    	return false;
	    }
    }
	return true;
}

int Emu::getNextPiece(int height, bool lineClear) {
	int randCycles = (6-(height+2)/4)*2+height*2+9;
	int nRand = hist.back().rand;
	if(hist.size() == 1){
		randCycles--;
	} else {
		for(int i = 0; i < hist.back().waitFrames; i++){
			nRand = rand(nRand);
		}
		int index = ((nRand >> 8) + hist.size() + 1) & 7;
		if (index == 7 || index == hist.back().curPiece) {
			nRand = rand(nRand);
		}
	}
	totalFrames = hist.back().totalFrames + randCycles + hist.back().waitFrames;
	if(lineClear){
		int lineClearFrames = 20 - ((totalFrames-6)%4);
		randCycles += lineClearFrames;
		totalFrames += lineClearFrames;
	}
	for(int i = 0; i < randCycles; i++){
		nRand = rand(nRand);
	}
	tempRand = nRand;
	return getNextBlocks(nRand, spawnTable[hist.back().nextPiece], (char) (hist.size()+1));
}
	
void Emu::updateNextPiece() {
	int nRand = hist.back().rand;
	for(int i = 0; i < hist.back().waitFrames; i++){
		nRand = rand(nRand);
	}
	hist.back().nextPiece = getNextBlocks(nRand, spawnTable[hist.back().curPiece], (char)hist.size());
}
	
int Emu::getNextBlocks(int nRand, char spawnID, char spawnCount) {
	spawnCount++;
	int index = ((nRand >> 8) + spawnCount) & 7;
	if (index != 7) {
		if (spawnTable[index] != spawnID) {
			spawnID = spawnTable[index];
			return index;
		}
	}
	nRand = rand(nRand);
	return (((nRand >> 8) & 7) + spawnID) % 7;
}
	
int Emu::generateSeed(bool wellMem[10][20], int nRand) {
	for(int curRow = 0x0C; curRow!=0; curRow--){
		char rowOffsetIndex = (char) (0x0C - curRow + 8);
		for(int curColumn = 0x09; curColumn>=0; curColumn--){
			nRand = rand(nRand);
			wellMem[curColumn][rowOffsetIndex] = tileArray[(nRand >> 8) & 0x07];
		}
		do{
			nRand = rand(nRand);
		}while(((nRand >> 8) & 0x0F) >= 0x0A);
		wellMem[(nRand >> 8) & 0x0F][rowOffsetIndex] = false;
		nRand = rand(nRand);
	}
	wellMem[0][8] = false;
	return nRand;
}
	
int Emu::rand(int nRand) {
	return ((((nRand >> 9) & 1) ^ ((nRand >> 1) & 1)) << 15) | (nRand >> 1);
}