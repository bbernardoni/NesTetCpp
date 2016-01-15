#include "AI.h"

AI::AI(Emu *emu, int pGarbageHeights[], int garbageHeightsLen, int maxPieces):
		emu(emu),
		garbageHeightsLen(garbageHeightsLen),
		nubPieces(0),
		maxPieces(maxPieces),
		minFrames(INT_MAX)
{
    createStates();
    resetStates();
	for(int i=0; i<4; i++){
		locked[0][i] = false;
		locked[11][i] = false;
	}
    garbageHeights = pGarbageHeights;
	garbageHeights = new int[garbageHeightsLen];
    garbageHeights[0] = 7+pGarbageHeights[0];
    for(int i=1; i<garbageHeightsLen; i++){
        garbageHeights[i] = garbageHeights[i-1]+pGarbageHeights[i];
    }
    multiGHLen = 0;
    for(int i=0; i<garbageHeightsLen; i++){
        if(pGarbageHeights[i]>1){
            multiGHLen++;
        }
    }
    multiGH = new int[multiGHLen];
    int counter = 0;
    for(int i=0; i<garbageHeightsLen; i++){
        if(pGarbageHeights[i]>1){
            multiGH[counter] = garbageHeights[i];
            counter++;
        }
    }
    garbageHeightsIndex = 0;
}


void AI::start(){
	//for(int i=0; i<garbageHeights.length; i++)
	{
		for(int j=0; j<4; j++)
			minClearFrames[j] = INT_MAX;
		minClearMax = INT_MAX;
		/*int[][] replay = {
		{0, 8, 1, 10},
		{4, 7, 1, 2},
		{8, 7, 3, 1},
		{9, 7, 0, 2},
		{1, 6, 0, 2},
		{4, 6, 1, 0},
		{6, 7, 2, 0}};
		for(int i=0; i<replay.length; i++){
			emu->placePiece(replay[i][0], replay[i][1], replay[i][2]);
			for(int j=0; j<replay[i][3]; j++){
				emu->incWaitFrames();
			}
		}
		garbageHeightsIndex = 1;*/
		search();
	}
}

void AI::lineClear(){
	garbageHeightsIndex++;
}
	
void AI::createStates() {
	for(int x = 0; x < 10; x++) {
		for(int y = 0; y < 20; y++) {        
			for(int rotation = 0; rotation < 4; rotation++) { 
				states[x][y][rotation] = State(x, y, rotation);
			}
		}
	}
}
	
void AI::resetStates() {
	for(int x = 0; x < 10; x++) {
		for(int y = 0; y < 20; y++) {        
			for(int rotation = 0; rotation < 4; rotation++) { 
				states[x][y][rotation].visited = false;
			}
		}
		for(int i=0; i<4; i++)
			locked[x+1][i] = false;
	}
}

// returns true if the position is valid even if the node is not enqueued
bool AI::addChild(int tetriminoType, int x, int y, int rotation) {
	if (x < bounds[tetriminoType][rotation][0] || x > bounds[tetriminoType][rotation][1] || 
			y > bounds[tetriminoType][rotation][3]) {
		return false;
	}
		
	State *childNode = &states[x][y][rotation];
	if (childNode->visited == true) {
		return true;
	}

	if (!emu->pieceClear2(tetriminoType, rotation, x, y)) {
		return false;
	}

	childNode->visited = true;
	queue.push(childNode);   
	return true; 
}  
	  
void AI::search() {
	bool tetriminos[7];
	for(int i=0; i<7; i++)
		tetriminos[i] = false;
	do{
		if(tetriminos[emu->hist.back().curPiece])
			continue;
		tetriminos[emu->hist.back().curPiece] = true;
		
		if(emu->hist.back().rand == 44366)
			tetriminos[emu->hist.back().curPiece] = true;
		
		std::vector<State*> locks;
		getLocks(&locks);
		for (unsigned int i=0; i<locks.size(); i++){
			State *state = locks.at(i);
			nubPieces++;
			int *lines = emu->placePiece(state->x, state->y, state->rotation);
			int lineClear = -1;
			int lineLength = 0;
			if(lines != NULL){
				lineClear = lines[0];
				lineLength = emu->hist.back().numOfLineClears;
			}
			bool bAnalyze = analyze(state, lineClear, lineLength);
			if(bAnalyze){
				search();
			}
			if(lineClear>=0){
				garbageHeightsIndex--;
			}
			emu->undoPlace();
			nubPieces--;
		}
	}while(emu->incCurWaitFrames());
}
	  
void AI::getLocks(std::vector<State*> *locks) {
	resetStates();
	int maxRotation = numOfRot[emu->hist.back().curPiece] - 1;
	for(int rot = 0; rot < numOfRot[emu->hist.back().curPiece]; rot++){
		for(int x = bounds[emu->hist.back().curPiece][rot][0]; x <= bounds[emu->hist.back().curPiece][rot][1]; x++){
			addChild(emu->hist.back().curPiece, x, 0, rot);
		}
	}
	while(!queue.empty()) {
		State *state = queue.front();
		queue.pop();
		if (!addChild(emu->hist.back().curPiece, state->x, state->y + 1, state->rotation) && state->y > 1) {
			locked[state->x+1][state->rotation] = true;
			locks->push_back(state);
		}

		if (maxRotation != 0) {
			int rotation = state->rotation == 0 ? maxRotation : state->rotation - 1;
			if (locked[state->x+1][rotation]) {
				addChild(emu->hist.back().curPiece, state->x, state->y, rotation);
			}
			if (maxRotation != 1) {
				rotation = state->rotation == maxRotation ? 0 : state->rotation + 1;
				if (locked[state->x+1][rotation]) {
					addChild(emu->hist.back().curPiece, state->x, state->y, rotation);
				}
			}
		}
		if (locked[state->x][state->rotation]) {
			addChild(emu->hist.back().curPiece, state->x - 1, state->y, state->rotation);
		}
		if (locked[state->x+2][state->rotation]) {
			addChild(emu->hist.back().curPiece, state->x + 1, state->y, state->rotation);
		}
	}
	std::reverse(locks->begin(),locks->end());
}

bool AI::analyze(State *state, int lineClear, int lineLength) {
	if(lineClear>=0){
		garbageHeightsIndex++;
		if(garbageHeights[garbageHeightsIndex]>lineClear){
			return false;
		}
		if(lineLength != 3){ // temp
			return false;
		}
	}
	if(maxPieces<=nubPieces){
		if(lineClear==19){
			for(int i = 0; i<10; i++){
				if(emu->isCellFilled(i,19)){
					return false;
				}
			}
			if(minFrames>=emu->totalFrames-10){
				emu->printGame();
			}
		}
		return false;
	}
	if(state->y<5){//temp 4){ 
		return false;
	}
	if(emu->hist.back().totalFrames > minClearMax){
		return false;
	}
	if(emu->linesCleared>5){
		for(int x=0; x<10; x++){
	        for(int y=0; y<emu->linesCleared-5; y++){
	            if(emu->isCellFilled(x,y)){
	            	return false;
	            }
			}
		}
	}
	for(int x=0; x<10; x++){
        for(int y=garbageHeights[garbageHeightsIndex]; y>0; y--){
            if(badHole(x,y)){
            	return false;
            }
		}
	}
	for(int i=0; i<multiGHLen; i++){
		int y = multiGH[i];
		if(y>garbageHeights[garbageHeightsIndex]){
            for(int x=0; x<10; x++){
            	if(badHole(x,y)){
                	return false;
                }
			}
		}
	}
	for(int x=0; x<10; x++){
		if(garbageHeights[garbageHeightsIndex]<19){
			if(emu->isCellFilled(x,garbageHeights[garbageHeightsIndex]+1)){
				continue;
			}
		}
        for(int y=0; y<garbageHeights[garbageHeightsIndex]-3; y++){
            if(emu->isCellFilled(x,y)){
            	return false;
            }
		}
	}
	if(lineClear>=0){
		if(minClearFrames[lineLength-1] > emu->hist.back().totalFrames){
			minClearFrames[lineLength-1] = emu->hist.back().totalFrames;
			minClearMax = minClearFrames[lineLength-1];/* temp 0];
			for(int i=1; i<4; i++){
				if(minClearMax < minClearFrames[i]){
					minClearMax = minClearFrames[i];
				}
			}*/
			printf("Lines=%d\n", lineLength);
			emu->printGame();
		}
		return false;
	}
	return true;
}

bool AI::badHole(int x, int y) {
	if(emu->isCellFilled(x,y) || !emu->isCellFilled(x,y-1)){
		return false;
    }
	if(x>1){
		if(!emu->isCellFilled(x-1,y)&&!emu->isCellFilled(x-2,y-1)&&!emu->isCellFilled(x-2,y)&&!emu->isCellFilled(x-1,y-1)){
			return false;
			/*if(!emu->isCellFilled(x-1,y-1)){
				return false;
			}
			if(!emu->isCellFilledSafe(x-2,y-2)&&!emu->isCellFilledSafe(x-3,y)){
				if(!emu->isCellFilledSafe(x-2,y+1)){
					return false;
				}
				if(!emu->isCellFilledSafe(x-3,y-2)&&!emu->isCellFilledSafe(x-4,y)&&
						!emu->isCellFilledSafe(x-4,y-1)&&!emu->isCellFilledSafe(x-3,y-1)){
					return false;
				}
			}*/
		}
	}
	if(x<8){
		if(!emu->isCellFilled(x+1,y)&&!emu->isCellFilled(x+2,y-1)&&!emu->isCellFilled(x+2,y)&&!emu->isCellFilled(x+1,y-1)){
			return false;
			/*if(!emu->isCellFilled(x+1,y-1)){
				return false;
			}
			if(!emu->isCellFilledSafe(x+2,y-2)&&!emu->isCellFilledSafe(x+3,y)){
				if(!emu->isCellFilledSafe(x+2,y+1)){
					return false;
				}
				if(!emu->isCellFilledSafe(x+3,y-2)&&!emu->isCellFilledSafe(x+4,y)&&
						!emu->isCellFilledSafe(x+4,y-1)&&!emu->isCellFilledSafe(x+3,y-1)){
					return false;
				}
			}*/
		}
	}
	return true;
}