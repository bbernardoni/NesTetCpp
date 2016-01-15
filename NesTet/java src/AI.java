package Bennett.Bernardoni;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class AI {

	private State[][][] states;
	private boolean[][] locked = new boolean[12][4];
	private Queue<State> queue = new LinkedList<State>();
	private Emu emu;
	int[] garbageHeights;
	int[] multiGH;
	int garbageHeightsIndex;
	int nubPieces = 0;
	int maxPieces;
	int minFrames = Integer.MAX_VALUE;
	int[] minClearFrames = new int[4];
	int minClearMax;
	
	/*long start;
	long locksTime;
	long placeTime;
	long anaTime;
	long undoTime;
	long incWaitTime;

	long locksResetTime;
	long locksInitTime;
	long locksDownTime;
	long locksRotTime;
	long locksTransTime;
	long locksRevTime;*/
	
	public AI(boolean[][] mem, int curPiece, int nextPiece, int rand, int totalFrames, int[] garbageHeights, int maxPieces) {
        emu = new Emu(mem, curPiece, nextPiece, rand, totalFrames);
		//GUI gui = new GUI(emu);gui.startThread();
        createStates();
        resetStates();
        Arrays.fill(locked[0], false);
        Arrays.fill(locked[11], false);
        this.garbageHeights = new int[garbageHeights.length];
        this.garbageHeights[0] = 7+garbageHeights[0];
        for(int i=1; i<garbageHeights.length; i++){
            this.garbageHeights[i] = this.garbageHeights[i-1]+garbageHeights[i];
        }
        int counter = 0;
        for(int i=0; i<garbageHeights.length; i++){
            if(garbageHeights[i]>1){
            	counter++;
            }
        }
        multiGH = new int[counter];
        counter = 0;
        for(int i=0; i<garbageHeights.length; i++){
            if(garbageHeights[i]>1){
            	multiGH[counter] = this.garbageHeights[i];
            	counter++;
            }
        }
        garbageHeightsIndex = 0;
        this.maxPieces = maxPieces;
    }
	
	public void start(){
		//for(int i=0; i<garbageHeights.length; i++)
		{
			Arrays.fill(minClearFrames, Integer.MAX_VALUE);
			minClearMax = Integer.MAX_VALUE;
			//start = System.currentTimeMillis();
			/*int[][] replay = {
			{0, 8, 1, 10},
			{4, 7, 1, 2},
			{8, 7, 3, 1},
			{9, 7, 0, 2},
			{1, 6, 0, 2},
			{4, 6, 1, 0},
			{6, 7, 2, 0}};
			for(int i=0; i<replay.length; i++){
				emu.placePiece(replay[i][0], replay[i][1], replay[i][2]);
				for(int j=0; j<replay[i][3]; j++){
					emu.incWaitFrames();
				}
			}
			garbageHeightsIndex = 1;*/
			search();
		}
	}
	
	private void createStates() {
		states = new State[10][20][4];
		for(int x = 0; x < 10; x++) {
			for(int y = 0; y < 20; y++) {        
				for(int rotation = 0; rotation < 4; rotation++) { 
					states[x][y][rotation] = new State(x, y, rotation);
				}
			}
		}
	}
	
	private void resetStates() {
		for(int x = 0; x < 10; x++) {
			for(int y = 0; y < 20; y++) {        
				for(int rotation = 0; rotation < 4; rotation++) { 
					states[x][y][rotation].visited = false;
				}
			}
            Arrays.fill(locked[x+1], false);
		}
	}

	// returns true if the position is valid even if the node is not enqueued
	private boolean addChild(int tetriminoType, State state, int x, int y, int rotation) {
		if (x < Emu.bounds[tetriminoType][rotation][0] || x > Emu.bounds[tetriminoType][rotation][1] || 
				y > Emu.bounds[tetriminoType][rotation][3]) {
			return false;
		}
		
		State childNode = states[x][y][rotation];
		if (childNode.visited == true) {
			return true;
		}

		if (!emu.pieceClear2(tetriminoType, rotation, x, y)) {
			return false;
		}

		childNode.visited = true;
		queue.add(childNode);   
		return true; 
	}  
	  
	public void search() {
		boolean[] tetriminos = new boolean[7];
		boolean searchLoop;
		do{
			/*if(System.currentTimeMillis()-start > 10000){
				start = 0;
			}
			long locksStart = System.nanoTime();*/
			if(tetriminos[emu.hist.last().curPiece])
				continue;
			tetriminos[emu.hist.last().curPiece] = true;
			
			ArrayList<State> locks = new ArrayList<State>();
			getLocks(locks);
			//long locksEnd = System.nanoTime();
			//locksTime += locksEnd-locksStart;
			for(State state: locks){
				nubPieces++;
				//long placeStart = System.nanoTime();
				int[] lines = emu.placePiece(state.x, state.y, state.rotation);
				//long placeEnd = System.nanoTime();
				//placeTime += placeEnd-placeStart;
				int lineClear = -1;
				int lineLength = 0;
				if(lines != null){
					lineClear = lines[0];
					lineLength = lines.length;
				}
				//long anaStart = System.nanoTime();
				boolean analyze = analyze(state, lineClear, lineLength);
				//long anaEnd = System.nanoTime();
				//anaTime += anaEnd-anaStart;
				if(analyze){
					search();
				}
				if(garbageHeightsIndex>0){
					if(garbageHeights[garbageHeightsIndex-1]<=lines[0]){
						garbageHeightsIndex--;
					}
				}
				//long undoStart = System.nanoTime();
				emu.undoPlace();
				//long undoEnd = System.nanoTime();
				//undoTime += undoEnd-undoStart;
				nubPieces--;
			}
			//long incWaitStart = System.nanoTime();
			//searchLoop = emu.incCurWaitFrames();
			//long incWaitEnd = System.nanoTime();
			//incWaitTime += incWaitEnd-incWaitStart;
		}while(emu.incCurWaitFrames());
	}
	  
	private void getLocks(ArrayList<State> locks) {
		//long locksResetStart = System.nanoTime();
		resetStates();
		//long locksResetEnd = System.nanoTime();
		//locksResetTime += locksResetEnd-locksResetStart;
		//long locksInitStart = System.nanoTime();
		int maxRotation = Emu.tPos[emu.hist.last().curPiece].length - 1;
		for(int rot = 0; rot < Emu.bounds[emu.hist.last().curPiece].length; rot++){
			for(int x = Emu.bounds[emu.hist.last().curPiece][rot][0]; x <= Emu.bounds[emu.hist.last().curPiece][rot][1]; x++){
				addChild(emu.hist.last().curPiece, null, x, 0, rot);
			}
		}
		//long locksInitEnd = System.nanoTime();
		//locksInitTime += locksInitEnd-locksInitStart;
		while(!queue.isEmpty()) {
			//long locksDownStart = System.nanoTime();
			State state = queue.remove();
			if (!addChild(emu.hist.last().curPiece, state, state.x, state.y + 1, state.rotation) && state.y > 1) {
				locked[state.x+1][state.rotation] = true;
				locks.add(state);
			}
			//long locksDownEnd = System.nanoTime();
			//locksDownTime += locksDownEnd-locksDownStart;

			//long locksRotStart = System.nanoTime();
			if (maxRotation != 0) {
				int rotation = state.rotation == 0 ? maxRotation : state.rotation - 1;
				if (locked[state.x+1][rotation]) {
					addChild(emu.hist.last().curPiece, state, state.x, state.y, rotation);
				}
				if (maxRotation != 1) {
					rotation = state.rotation == maxRotation ? 0 : state.rotation + 1;
					if (locked[state.x+1][rotation]) {
						addChild(emu.hist.last().curPiece, state, state.x, state.y, rotation);
					}
				}
			}
			//long locksRotEnd = System.nanoTime();
			//locksRotTime += locksRotEnd-locksRotStart;
			//long locksTransStart = System.nanoTime();
			if (locked[state.x][state.rotation]) {
				addChild(emu.hist.last().curPiece, state, state.x - 1, state.y, state.rotation);
			}
			if (locked[state.x+2][state.rotation]) {
				addChild(emu.hist.last().curPiece, state, state.x + 1, state.y, state.rotation);
			}
			//long locksTransEnd = System.nanoTime();
			//locksTransTime += locksTransEnd-locksTransStart;
		}
		//long locksRevStart = System.nanoTime();
	    Collections.reverse(locks);
		//long locksRevEnd = System.nanoTime();
		//locksRevTime += locksRevEnd-locksRevStart;
	}

	private boolean analyze(State state, int lineClear, int lineLength) {
		if(lineClear>=0){
			if(garbageHeights[garbageHeightsIndex]<=lineClear){
				garbageHeightsIndex++;
			}
			else{
				return false;
			}
		}
		if(maxPieces<=nubPieces){
			if(lineClear==19){
				for(int i = 0; i<10; i++){
					if(emu.isCellFilled(i,19)){
						return false;
					}
				}
				if(minFrames>=emu.totalFrames-10){
					emu.printGame();
				}
			}
			return false;
		}
		if(state.y<4){
			return false;
		}
		if(emu.hist.last().totalFrames > minClearMax){
			return false;
		}
		if(emu.linesCleared>5){
			for(int x=0; x<10; x++){
	        	for(int y=0; y<emu.linesCleared-5; y++){
	            	if(emu.isCellFilled(x,y)){
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
		for(int y: multiGH){
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
				if(emu.isCellFilled(x,garbageHeights[garbageHeightsIndex]+1)){
					continue;
				}
			}
        	for(int y=0; y<garbageHeights[garbageHeightsIndex]-3; y++){
            	if(emu.isCellFilled(x,y)){
            		return false;
            	}
            }
        }
		if(lineClear>=0){
			if(minClearFrames[lineLength-1] > emu.hist.last().totalFrames){
				minClearFrames[lineLength-1] = emu.hist.last().totalFrames;
				minClearMax = minClearFrames[0];
				for(int i=1; i<4; i++){
					if(minClearMax < minClearFrames[i]){
						minClearMax = minClearFrames[i];
					}
				}
				System.out.println("Lines="+lineLength);
				emu.printGame();
			}
			return false;
		}
		return true;
	}

	private boolean badHole(int x, int y) {
		if(emu.isCellFilled(x,y) || !emu.isCellFilled(x,y-1)){
			return false;
    	}
		if(x>1){
			if(!emu.isCellFilled(x-1,y)&&!emu.isCellFilled(x-2,y-1)&&!emu.isCellFilled(x-2,y)&&!emu.isCellFilled(x-1,y-1)){
				return false;
				/*if(!emu.isCellFilled(x-1,y-1)){
					return false;
				}
				if(!emu.isCellFilledSafe(x-2,y-2)&&!emu.isCellFilledSafe(x-3,y)){
					if(!emu.isCellFilledSafe(x-2,y+1)){
						return false;
					}
					if(!emu.isCellFilledSafe(x-3,y-2)&&!emu.isCellFilledSafe(x-4,y)&&
							!emu.isCellFilledSafe(x-4,y-1)&&!emu.isCellFilledSafe(x-3,y-1)){
						return false;
					}
				}*/
			}
		}
		if(x<8){
			if(!emu.isCellFilled(x+1,y)&&!emu.isCellFilled(x+2,y-1)&&!emu.isCellFilled(x+2,y)&&!emu.isCellFilled(x+1,y-1)){
				return false;
				/*if(!emu.isCellFilled(x+1,y-1)){
					return false;
				}
				if(!emu.isCellFilledSafe(x+2,y-2)&&!emu.isCellFilledSafe(x+3,y)){
					if(!emu.isCellFilledSafe(x+2,y+1)){
						return false;
					}
					if(!emu.isCellFilledSafe(x+3,y-2)&&!emu.isCellFilledSafe(x+4,y)&&
							!emu.isCellFilledSafe(x+4,y-1)&&!emu.isCellFilledSafe(x+3,y-1)){
						return false;
					}
				}*/
			}
		}
		return true;
	}
}
