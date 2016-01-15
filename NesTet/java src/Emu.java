package Bennett.Bernardoni;

import java.util.ArrayList;

public class Emu {
    //tPos[piece][rotation][tile#][x/y]
	public static final int[][][][]  tPos = { { { { -1,  0 }, {  0,  0 }, {  1,  0 }, {  0, -1 } },  //-- 00: T up
												{ {  0, -1 }, {  0,  0 }, {  1,  0 }, {  0,  1 } },  //-- 01: T right
												{ { -1,  0 }, {  0,  0 }, {  1,  0 }, {  0,  1 } },  //-- 02: T down (spawn)
												{ {  0, -1 }, { -1,  0 }, {  0,  0 }, {  0,  1 } }}, //-- 03: T left
											  { { {  0, -1 }, {  0,  0 }, { -1,  1 }, {  0,  1 } },  //-- 04: J left
												{ { -1, -1 }, { -1,  0 }, {  0,  0 }, {  1,  0 } },  //-- 05: J up
												{ {  0, -1 }, {  1, -1 }, {  0,  0 }, {  0,  1 } },  //-- 06: J right
												{ { -1,  0 }, {  0,  0 }, {  1,  0 }, {  1,  1 } }}, //-- 07: J down (spawn)
											  { { { -1,  0 }, {  0,  0 }, {  0,  1 }, {  1,  1 } },  //-- 08: Z horizontal (spawn) 
												{ {  1, -1 }, {  0,  0 }, {  1,  0 }, {  0,  1 } }}, //-- 09: Z vertical
											  { { { -1,  0 }, {  0,  0 }, { -1,  1 }, {  0,  1 } }}, //-- 0A: O (spawn)
											  { { {  0,  0 }, {  1,  0 }, { -1,  1 }, {  0,  1 } },  //-- 0B: S horizontal (spawn)
												{ {  0, -1 }, {  0,  0 }, {  1,  0 }, {  1,  1 } }}, //-- 0C: S vertical
											  { { {  0, -1 }, {  0,  0 }, {  0,  1 }, {  1,  1 } },  //-- 0D: L right
												{ { -1,  0 }, {  0,  0 }, {  1,  0 }, { -1,  1 } },  //-- 0E: L down (spawn)
												{ { -1, -1 }, {  0, -1 }, {  0,  0 }, {  0,  1 } },  //-- 0F: L left
												{ {  1, -1 }, { -1,  0 }, {  0,  0 }, {  1,  0 } }}, //-- 10: L up
											  { { {  0, -2 }, {  0, -1 }, {  0,  0 }, {  0,  1 } },  //-- 11: I vertical
												{ { -2,  0 }, { -1,  0 }, {  0,  0 }, {  1,  0 } }}};//-- 12: I horizontal (spawn)
    //bounds[piece][rotation][xmin/xmax/ymin/ymax] (inclusive)
	public static final int[][][]  bounds = {{{1,8,1,19}, {0,8,1,18}, {1,8,0,18}, {1,9,1,18}},	//T
											 {{1,9,1,18}, {1,8,1,19}, {0,8,1,18}, {1,8,0,18}},	//J
											 {{1,8,0,18}, {0,8,1,18}}, 							//Z
											 {{1,9,0,18}}, 										//O
											 {{1,8,0,18}, {0,8,1,18}},							//S
											 {{0,8,1,18}, {1,8,0,18}, {1,9,1,18}, {1,8,1,19}},	//L
											 {{0,9,2,18}, {2,8,0,19}}};							//I
	public static final boolean[] tileArray = {false, true, false, true, true, true, false, false};
	public static final byte[] spawnTable = {0x02,0x07,0x08,0x0A,0x0B,0x0E,0x12}; //2-T 7-J 8-Z A-O B-S E-L 12-I
	public static final byte[] pieceOffset ={0x00,0x04,0x08,0x0A,0x0B,0x0D,0x11};
	public static final byte[] rotTable = {2,3,0,0,0,1,1};
	
	ArrayList2<Event> hist = new ArrayList2<Event>(); //TJZOSLI
	boolean[][] wellMem = new boolean[10][20];
	int px = -1;
	int py = -1;
	int tempRand;
	int totalFrames;
	int linesCleared = 0;
	
	public Emu(boolean[][] mem, int curPiece, int nextPiece, int rand, int totalFrames) {
		hist.add(new Event(curPiece, nextPiece, rand, totalFrames, 0, 0));
        for(int x=0; x<10; x++){
        	for(int y=0; y<20; y++){
            	wellMem[x][y] = mem[x][y];
            }
        }
    }
	
	public void printGame() {
		int lastPieceFrames = (Math.abs(hist.last().lastX-5)-1)*2;
		lastPieceFrames = (lastPieceFrames<0)? 0: lastPieceFrames;
		System.out.print("$B1="+Integer.toHexString(hist.get(hist.size()-2).totalFrames)+" TotalFrames="+
				(hist.get(hist.size()-2).totalFrames+263+lastPieceFrames)+"\n");
		System.out.print("px py rotation waitFrames\n");
		for(int i=1; i<hist.size(); i++){
			Event prev = hist.get(i-1);
			Event cur = hist.get(i);
			System.out.print(""+cur.lastX+" "+Integer.toHexString(cur.lastY)+" "+prev.rotation+" "+cur.waitFrames+"\n");
		}
	}
	
	public void placePiece() {
		placePiece(px/32, py/32);
	}

	public int[] placePiece(int px, int py, int rotation) {
		hist.last().rotation = rotation;
		return placePiece(px, py);
	}
	
	public int[] placePiece(int px, int py) {
		for(int i=0; i<4; i++){
        	wellMem[tPos[hist.last().curPiece][hist.last().rotation][i][0]+px]
        		   [tPos[hist.last().curPiece][hist.last().rotation][i][1]+py] = true;
        }
    	ArrayList<Integer> lineClears = new ArrayList<Integer>();
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
    			lineClears.add(line);
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
		hist.add(new Event(hist.last().nextPiece, index, tempRand, totalFrames, px, py));
		if(lineClears.size()>0){
			linesCleared += lineClears.size();
			int[] ret = new int[lineClears.size()];
			for(int i=0; i<ret.length; i++)
				ret[i] = lineClears.get(ret.length-i-1);
			hist.last().lineClears = ret;
			return ret;
		}
		return null;
	}
	
	public boolean incCurWaitFrames() {
		if(hist.size() <= 2){
			return false;
		}
		if(hist.get(hist.size()-2).waitFrames>=10){
			return false;
		}
		Event curE = hist.removeLast();
		hist.last().waitFrames++;
		int rand = hist.last().rand;
		for(int i = 0; i < hist.last().waitFrames; i++){
			rand = Emu.rand(rand);
		}
		hist.last().nextPiece = Emu.getNextBlocks(rand, Emu.spawnTable[hist.last().curPiece], (byte)hist.size());
		int randCycles = (6-(curE.lastY+2)/4)*2+curE.lastY*2+9;
		rand = hist.last().rand;
		if(hist.size() == 1){
			randCycles--;
		} else {
			for(int i = 0; i < hist.last().waitFrames; i++){
				rand = Emu.rand(rand);
			}
			int index = ((rand >> 8) + hist.size() + 1) & 7;
			if (index == 7 || index == hist.last().curPiece) {
				rand = Emu.rand(rand);
			}
		}
		int totalFrames = hist.last().totalFrames + randCycles + hist.last().waitFrames;
		if(curE.lineClears != null){
			int lineClearFrames = 20 - ((totalFrames-6)%4);
			randCycles += lineClearFrames;
			totalFrames += lineClearFrames;
		}
		for(int i = 0; i < randCycles; i++){
			rand = Emu.rand(rand);
		}
		curE.curPiece = hist.last().nextPiece;
		curE.nextPiece = Emu.getNextBlocks(rand, Emu.spawnTable[hist.last().nextPiece], (byte) (hist.size()+1));
		curE.rand = rand;
		curE.totalFrames = totalFrames;
		curE.waitFrames = 0;
		hist.add(curE);
		return true;
	}
	
	public boolean incWaitFrames(){
		if(hist.last().waitFrames<10 && hist.size() > 1){
			hist.last().waitFrames++;
			updateNextPiece();
			return true;
		}
		return false;
	}
	
	public void decWaitFrames(){
		if(hist.last().waitFrames>0 && hist.size() > 1){
			hist.last().waitFrames--;
			updateNextPiece();
		}
	}
	
	public void rotClockwise(){
		if(hist.last().rotation<tPos[hist.last().curPiece].length-1){
			hist.last().rotation++;
		}else{
			hist.last().rotation=0;
		}
	}
	
	public void rotCounterClockwise(){
		if(hist.last().rotation>0){
			hist.last().rotation--;
		}else{
			hist.last().rotation=tPos[hist.last().curPiece].length-1;
		}
	}

	public void undoPlace() {
		if(hist.size()>1){
			Event oldE = hist.removeLast();
			if(oldE.lineClears != null){
				linesCleared -= oldE.lineClears.length;
				for(int line: oldE.lineClears){
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
            	wellMem[tPos[hist.last().curPiece][hist.last().rotation][i][0]+oldE.lastX]
            		   [tPos[hist.last().curPiece][hist.last().rotation][i][1]+oldE.lastY] = false;
            }
		}
	}
	
	public int getCurPieceX(int tile){
		return tPos[hist.last().curPiece][hist.last().rotation][tile][0]+px/32;
	}
	
	public int getCurPieceY(int tile){
		return tPos[hist.last().curPiece][hist.last().rotation][tile][1]+py/32;
	}
	
	public int getNextPieceX(int tile){
		return tPos[hist.last().nextPiece][Emu.rotTable[hist.last().nextPiece]][tile][0];
	}
	
	public int getNextPieceY(int tile){
		return tPos[hist.last().nextPiece][Emu.rotTable[hist.last().nextPiece]][tile][1];
	}

	public boolean isCellFilled(int x, int y) {
		return wellMem[x][y];
	}

	public boolean isCellFilledSafe(int x, int y) {
		if(x<0||x>9||y<0||y>19){
			return true;
		}
		return wellMem[x][y];
	}
    
	public boolean curPieceClear(){
    	return pieceClear(wellMem, hist.last().curPiece, hist.last().rotation, px/32, py/32);
    }
	
    public static boolean pieceClear(boolean[][] wellMem, int curPiece, int rotation, int x, int y){
    	for(int i=0; i<4; i++){
        	if(!tileClear(wellMem, tPos[curPiece][rotation][i][0]+x, tPos[curPiece][rotation][i][1]+y)){
        		return false;
        	}
        }
    	return true;
    }
	
	public static boolean tileClear(boolean[][] wellMem, int x, int y){
    	if(x < 0 || y < 0 || x >= 10 || y >= 20){
    		return false;
    	}
    	return !wellMem[x][y];
    }
	
    public boolean pieceClear2(int curPiece, int rotation, int x, int y){
    	for(int i=0; i<4; i++){
        	if(!tileClear2(tPos[curPiece][rotation][i][0]+x, tPos[curPiece][rotation][i][1]+y)){
        		return false;
        	}
        }
    	return true;
    }
	
	public boolean tileClear2(int x, int y){
    	if(y < 0){
    		return true;
    	}
    	return !wellMem[x][y];
    }

	private boolean testLine(int line) {
		for(int i=0; i<10; i++){
			if(!wellMem[i][line]){
	    		return false;
	    	}
        }
		return true;
	}

	private int getNextPiece(int height, boolean lineClear) {
		int randCycles = (6-(height+2)/4)*2+height*2+9;
		int rand = hist.last().rand;
		if(hist.size() == 1){
			randCycles--;
		} else {
			for(int i = 0; i < hist.last().waitFrames; i++){
				rand = Emu.rand(rand);
			}
			int index = ((rand >> 8) + hist.size() + 1) & 7;
			if (index == 7 || index == hist.last().curPiece) {
				rand = Emu.rand(rand);
			}
		}
		totalFrames = hist.last().totalFrames + randCycles + hist.last().waitFrames;
		if(lineClear){
			int lineClearFrames = 20 - ((totalFrames-6)%4);
			randCycles += lineClearFrames;
			totalFrames += lineClearFrames;
		}
		for(int i = 0; i < randCycles; i++){
			rand = Emu.rand(rand);
		}
		tempRand = rand;
		return Emu.getNextBlocks(rand, Emu.spawnTable[hist.last().nextPiece], (byte) (hist.size()+1));
	}
	
	private void updateNextPiece() {
		int rand = hist.last().rand;
		for(int i = 0; i < hist.last().waitFrames; i++){
			rand = Emu.rand(rand);
		}
		hist.last().nextPiece = Emu.getNextBlocks(rand, Emu.spawnTable[hist.last().curPiece], (byte)hist.size());
	}
	
	public static int getNextBlocks(int rand, byte spawnID, byte spawnCount) {
		spawnCount++;
		int index = ((rand >> 8) + spawnCount) & 7;
		if (index != 7) {
			if (spawnTable[index] != spawnID) {
				spawnID = spawnTable[index];
				return index;
			}
		}
		rand = rand(rand);
		return (((rand >> 8) & 7) + spawnID) % 7;
	}
	
	public static int generateSeed(boolean wellMem[][], int rand) {
		for(int curRow = 0x0C; curRow!=0; curRow--){
			byte rowOffsetIndex = (byte) (0x0C - curRow + 8);
			for(int curColumn = 0x09; curColumn>=0; curColumn--){
				rand = rand(rand);
				wellMem[curColumn][rowOffsetIndex] = tileArray[(rand >> 8) & 0x07];
			}
			do{
				rand = rand(rand);
			}while(((rand >> 8) & 0x0F) >= 0x0A);
			wellMem[(rand >> 8) & 0x0F][rowOffsetIndex] = false;
			rand = rand(rand);
		}
		wellMem[0][8] = false;
		return rand;
	}
	
	public static int rand(int rand) {
		return ((((rand >> 9) & 1) ^ ((rand >> 1) & 1)) << 15) | (rand >> 1);
	}
}