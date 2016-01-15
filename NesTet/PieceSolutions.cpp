#include "PieceSolutions.h"

PieceSolutions::PieceSolutions()
{
	createStates();
    resetStates();
	remove("pieceSolutions.txt");
	solutions = 0;
}

void PieceSolutions::find(bool (*mem)[10], int linesToClear)
{
	lines = linesToClear;
	wellMem = mem;
    resetStates();
	search();
}

void PieceSolutions::search()
{
	for(int piece=0; piece<7; piece++){
		solutionPieces.push_back(piece);
		std::vector<State*> locks;
		getLocks(piece, &locks);
		for (unsigned int i=0; i<locks.size(); i++){
			State *state = locks.at(i);
			solutionStates.push_back(state);
			for(int i=0; i<4; i++){
				wellMem[tPos[piece][state->rotation][i][1]+state->y]
						[tPos[piece][state->rotation][i][0]+state->x] = true;
			}
			int rowsClear = 0;
			for(int y=2; y<2+lines; y++){
				rowsClear++;
				for(int x=0; x<10; x++){
					if(!wellMem[y][x]){
						rowsClear--;
						break;
					}
				}
			}
			if(rowsClear==0)
				search();
			else if(rowsClear==lines)
				printSolution();
			for(int i=0; i<4; i++){
				wellMem[tPos[piece][state->rotation][i][1]+state->y]
						[tPos[piece][state->rotation][i][0]+state->x] = false;
			}
			solutionStates.pop_back();
		}
		solutionPieces.pop_back();
	}
}

void PieceSolutions::createStates() {
	for(int x = 0; x < 10; x++) {
		for(int y = 0; y < 8; y++) {        
			for(int rotation = 0; rotation < 4; rotation++) { 
				states[x][y][rotation] = State(x, y, rotation);
			}
		}
	}
	for(int i=0; i<4; i++){
		locked[0][i] = false;
		locked[11][i] = false;
	}
}
	
void PieceSolutions::resetStates() {
	for(int x = 0; x < 10; x++) {
		for(int y = 0; y < 8; y++) {        
			for(int rotation = 0; rotation < 4; rotation++) { 
				states[x][y][rotation].visited = false;
			}
		}
		for(int i=0; i<4; i++)
			locked[x+1][i] = false;
	}
}

bool PieceSolutions::pieceClear2(int curPiece, int rotation, int x, int y){
    for(int i=0; i<4; i++){
        if(!tileClear2(tPos[curPiece][rotation][i][0]+x, tPos[curPiece][rotation][i][1]+y)){
        	return false;
        }
    }
    return true;
}
	
bool PieceSolutions::tileClear2(int x, int y){
    if(y < 2){
    	return true;
    }
    return !wellMem[y][x];
}

bool PieceSolutions::pieceClear(int curPiece, int rotation, int x, int y){
    for(int i=0; i<4; i++){
        if(!tileClear(tPos[curPiece][rotation][i][0]+x, tPos[curPiece][rotation][i][1]+y)){
        	return false;
        }
    }
    return true;
}
	
bool PieceSolutions::tileClear(int x, int y){
    if(y < 0){
    	return false;
    }
    return !wellMem[y][x];
}

// returns true if the position is valid even if the node is not enqueued
bool PieceSolutions::addChild(int tetriminoType, int x, int y, int rotation) {
	if (x < bounds[tetriminoType][rotation][0] || x > bounds[tetriminoType][rotation][1] || 
			y > bounds[tetriminoType][rotation][3]-16+lines) {
		return false;
	}
		
	State *childNode = &states[x][y][rotation];
	if (childNode->visited == true) {
		return true;
	}

	if (!pieceClear2(tetriminoType, rotation, x, y)) {
		return false;
	}

	childNode->visited = true;
	queue.push(childNode);   
	return true; 
}

void PieceSolutions::getLocks(int tetriminoType, std::vector<State*> *locks) {
	resetStates();
	int maxRotation = numOfRot[tetriminoType] - 1;
	for(int rot = 0; rot < numOfRot[tetriminoType]; rot++){
		for(int x = bounds[tetriminoType][rot][0]; x <= bounds[tetriminoType][rot][1]; x++){
			addChild(tetriminoType, x, 0, rot);
		}
	}
	while(!queue.empty()) {
		State *state = queue.front();
		queue.pop();
		if (!addChild(tetriminoType, state->x, state->y + 1, state->rotation)) {
			locked[state->x+1][state->rotation] = true;
			if(pieceClear(tetriminoType, state->rotation, state->x, state->y)){
				locks->push_back(state);
			}
		}

		if (maxRotation != 0) {
			int rotation = state->rotation == 0 ? maxRotation : state->rotation - 1;
			if (locked[state->x+1][rotation]) {
				addChild(tetriminoType, state->x, state->y, rotation);
			}
			if (maxRotation != 1) {
				rotation = state->rotation == maxRotation ? 0 : state->rotation + 1;
				if (locked[state->x+1][rotation]) {
					addChild(tetriminoType, state->x, state->y, rotation);
				}
			}
		}
		if (locked[state->x][state->rotation]) {
			addChild(tetriminoType, state->x - 1, state->y, state->rotation);
		}
		if (locked[state->x+2][state->rotation]) {
			addChild(tetriminoType, state->x + 1, state->y, state->rotation);
		}
	}
	std::reverse(locks->begin(),locks->end());
}

void PieceSolutions::printSolution(){
	solutions++;
	printf("solution #%d",solutions);
	FILE * pFile;
	fopen_s (&pFile, "pieceSolutions.txt","a");
	fprintf(pFile,"****************************************\n");
	for(unsigned int i=0; i<solutionPieces.size(); i++){
		State *s = solutionStates.at(i); 
		fprintf(pFile, "%x,%x,%x,%d\n", solutionPieces.at(i), s->x, s->y, s->rotation);
	}
	fclose(pFile);
}