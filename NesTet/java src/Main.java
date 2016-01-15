package Bennett.Bernardoni;

public class Main {

	int counter;
	boolean[][] wellMem = new boolean[10][20];
	boolean[] rowsFinished = {false,false,false,false,false,false,false,false,false,false,false,false};//12 rows
	int rowClearAnim;
	public static final int[] seedArray = {0x8e2d,0x31c5,0xf463,0xfa31,0xbf46,0x97e8,0x1fcb,0x750f,0xd750,0x3d75,0x21eb,0xca43,0x9948,0x2652,0xa265,0x3a26,0xc744,0x38e8,0x41c7,0x1a0e,0x4341,0x1434,0x143,0x2028,0x8405,0x840,0x210,0x9021,0x2408,0x6240,0x2c48,0x8b12,0x9162,0xe645,0x1e64,0x8f32,0xd1e6,0x6f47,0x8de8,0xd946,0xf651,0x3f65,0x4b4f,0x4969,0x824b,0x4824,0x9209,0xb241,0x3648,0xf364,0x7e6c,0x97e6,0xee25,0xbdc4,0x2f71,0x2f2f,0x5e5,0xe00b,0xbc01,0xebc0,0xbd78,0x15eb,0xfe2b,0x5fc5,0x5357,0x4a6a,0x8253,0x8825,0xa209,0x5a20,0xab44,0xf568,0x3bd5,0xdcef,0x6773,0x14ce,0xd14c,0xda29,0x3b45,0xe768,0x8fce,0xd047,0xf411,0x2f41,0xcbd0,0xdcb,0x8c37,0xec61,0x7630,0x3763,0xa376,0xd546,0x7551,0x49d5,0x724e,0xce49,0x79c9,0xc3ce,0x1b0f,0x4361};
	public static final String[] letterTable = {"T","J","Z","O","S","L","I"};
	public static enum lt {T, J, Z, O, S, L, I};
	
	//break at 87E3
	public void main (){
		//printNextRand(0x8E2D, 20);
		//printNextBlocks(0xACD312BA);
		//printSeeds();
		displayGame(10, lt.T, lt.L); //Break at 884A totalFrames = $B1
		//displayGame(13, lt.Z, lt.I); //Break at 884A totalFrames = $B1
		//displayEmpty(0x7F31, 0x1C, lt.L, lt.T);
		//findPieceSolutions(10, 1, 11, 48); //1,1,1,1,2,1,1,1,1,1,1
		//solveGame(10, lt.T, lt.L, new int[]{1,1,1,1,2,1,1,1,1,1,1});//13, lt.Z, lt.I, new int[]{1,2,2,1,1,1,1,1,1,1});//53, lt.T, lt.L, new int[]{1,2,1,1,1,2,1,1,1,1});
	}
	
	private void solveGame(int seedIndex, lt cur, lt next, int[] gh) {
		int endRand = Emu.generateSeed(wellMem, seedArray[seedIndex]);
		counter = 0;
		for(int i = 0; i<10; i++){
			for(int j = 0; j<20; j++){
				if(wellMem[i][j]){
					counter++;
				}
			}
		}
		AI ai = new AI(wellMem, cur.ordinal(), next.ordinal(), endRand, 50+seedIndex, gh, (250-counter)/4);
		ai.start();
	}
	
	private void solveGame2(int seedIndex, lt cur, lt next, int[] gh) {
		int endRand = Emu.generateSeed(wellMem, seedArray[seedIndex]);
		counter = 0;
		for(int i = 0; i<10; i++){
			for(int j = 0; j<20; j++){
				if(wellMem[i][j]){
					counter++;
				}
			}
		}
		AI ai = new AI(wellMem, cur.ordinal(), next.ordinal(), endRand, 50+seedIndex, gh, (250-counter)/4);
		ai.start();
	}
	
	private void findPieceSolutions(int seedIndex, int gn, int gh, int piecesLeft) {
		Emu.generateSeed(wellMem, seedArray[seedIndex]);
		PieceSolutions ps = new PieceSolutions(wellMem, gn, gh, piecesLeft);
		ps.start();
	}
	
	private void displayGame(int seedIndex, lt cur, lt next) {
		int endRand = Emu.generateSeed(wellMem, seedArray[seedIndex]);
		GUI gui = new GUI(wellMem, cur.ordinal(), next.ordinal(), endRand, 50+seedIndex);
		gui.startThread();
	}
	
	private void displayEmpty(int rand, int startFrame, lt cur, lt next) {
		GUI gui = new GUI(wellMem, cur.ordinal(), next.ordinal(), rand, startFrame);
		gui.startThread();
	}
	
	private void printNextRand(int rand, int nub) {
		for(int i = 0; i<nub; i++){
			rand = Emu.rand(rand);
			System.out.println(Integer.toHexString(rand));
		}
	}

	private void printNextBlocks(long input) {
		int prevIndex = -1;
		int rand = (int) ((input >> 16) & 0xFFFF);
		byte spawnID = (byte) ((input >> 8) & 0xFF);
		byte spawnCount = (byte) (input & 0xFF);
		for(int i = 0; i < 20; i++){
			int index = Emu.getNextBlocks(rand, spawnID, spawnCount);
			if(prevIndex != index){
				System.out.println("Seed="+Integer.toHexString(rand)+"	rand cycles="+i+
						"	next block="+lt.values()[index].name()+" ("+Integer.toHexString(Emu.spawnTable[index])+")");
			}
			prevIndex = index;
			rand = Emu.rand(rand);
		}
	}

	void printSeeds() {
		int frames = 0;
		for(int rand: seedArray){
			Emu.generateSeed(wellMem, rand);
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
				System.out.println("Seed="+Integer.toHexString(rand)+"	wait frames="+frames+"	# of pieces needed="+(250-counter)/4+"	clear anim="+rowClearAnim);
			}
			frames++;
		}
	}

	void analyzeSeed() {
		for(int i=0; i<rowsFinished.length; i++){
			rowsFinished[i] = false;
		}
		rowClearAnim = 0;
		while(getRowsLeft() > 1){
			boolean clearable = true;
			for(int i = 0; i < 10; i++){
				if(!wellMem[i][getRow(2)+8]){
					if(wellMem[i][getRow(1)+8]){
						boolean tileClearable = false;
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
				boolean thirdClearable = true;
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
		for(int i = 0; i < rowsFinished.length; i++){
			if(!rowsFinished[i]){
				rowsLeft++;
			}
		}
		return rowsLeft;
	}
	
	int getRow(int row) {
		int rowsLeft = 0;
		for(int i = 0; i < rowsFinished.length; i++){
			if(!rowsFinished[i]){
				rowsLeft++;
				if(rowsLeft == row){
					return i;
				}
			}
		}
		return -1;
	}
}