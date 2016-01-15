package Bennett.Bernardoni;

public class PieceSolutions {

	int[][][] wellMem = new int[10][20][4]; //0=no 1=filled 2=needed 3=available 4=dependent
	int[][] solution;
	int garbageHeight;
	int garbageNumber;
	int maxPieces;
	int minHeight;
	//tPos[piece][tile#][x/y]
	public static final int[][][]  tPos = 
		{{{-1, 0},{ 0, 0},{ 1, 0},{ 0,-1}}, //-- 00: T up
		 {{ 0,-1},{ 0, 0},{ 1, 0},{ 0, 1}}, //-- 01: T right
		 {{-1, 0},{ 0, 0},{ 1, 0},{ 0, 1}}, //-- 02: T down (spawn)
		 {{ 0,-1},{-1, 0},{ 0, 0},{ 0, 1}}, //-- 03: T left
		 {{ 0,-1},{ 0, 0},{-1, 1},{ 0, 1}}, //-- 04: J left
		 {{-1,-1},{-1, 0},{ 0, 0},{ 1, 0}}, //-- 05: J up
		 {{ 0,-1},{ 1,-1},{ 0, 0},{ 0, 1}}, //-- 06: J right
		 {{-1, 0},{ 0, 0},{ 1, 0},{ 1, 1}}, //-- 07: J down (spawn)
		 {{-1, 0},{ 0, 0},{ 0, 1},{ 1, 1}}, //-- 08: Z horizontal (spawn) 
		 {{ 1,-1},{ 0, 0},{ 1, 0},{ 0, 1}}, //-- 09: Z vertical
		 {{-1, 0},{ 0, 0},{-1, 1},{ 0, 1}}, //-- 0A: O (spawn)
		 {{ 0, 0},{ 1, 0},{-1, 1},{ 0, 1}}, //-- 0B: S horizontal (spawn)
		 {{ 0,-1},{ 0, 0},{ 1, 0},{ 1, 1}}, //-- 0C: S vertical
		 {{ 0,-1},{ 0, 0},{ 0, 1},{ 1, 1}}, //-- 0D: L right
		 {{-1, 0},{ 0, 0},{ 1, 0},{-1, 1}}, //-- 0E: L down (spawn)
		 {{-1,-1},{ 0,-1},{ 0, 0},{ 0, 1}}, //-- 0F: L left
		 {{ 1,-1},{-1, 0},{ 0, 0},{ 1, 0}}, //-- 10: L up
		 {{ 0,-2},{ 0,-1},{ 0, 0},{ 0, 1}}, //-- 11: I vertical
		 {{-2, 0},{-1, 0},{ 0, 0},{ 1, 0}}};//-- 12: I horizontal (spawn)
	
    //bounds[piece][xmin/xmax/ymin/ymax] (inclusive)
	public static final int[][]  bounds = { {1,8,1,19}, {0,8,1,18}, {1,8,0,18}, {1,9,1,18},	//T
											{1,9,1,18}, {1,8,1,19}, {0,8,1,18}, {1,8,0,18},	//J
											{1,8,0,18}, {0,8,1,18}, 						//Z
											{1,9,0,18}, 									//O
											{1,8,0,18}, {0,8,1,18},							//S
											{0,8,1,18}, {1,8,0,18}, {1,9,1,18}, {1,8,1,19},	//L
											{0,9,2,18}, {2,8,0,19}};						//I
	
	public PieceSolutions(boolean[][] mem, int gn, int gh, int maxPieces) {
        garbageNumber = gn;
        garbageHeight = gh;
        this.maxPieces = maxPieces;
        solution = new int[maxPieces][3];
        minHeight = garbageHeight;
        for(int i=0; i<mem.length; i++){
        	for(int j=0; j<=garbageHeight; j++){
            	if(mem[i][19-j]){
            		wellMem[i][j][0] = 1;
            	}
            }
        }
        for(int i=0; i<mem.length; i++){
        	if(!mem[i][19-garbageHeight]){
        		wellMem[i][garbageHeight][0] = 2;
        	}
        	for(int y=garbageHeight-1; y>=0; y--){
        		if(mem[i][19-y]){
        			if(y == garbageHeight-1){
        				wellMem[i][garbageHeight+1][0] = 3;
        				wellMem[i][garbageHeight+2][0] = 3;
        				wellMem[i][garbageHeight+3][0] = 3;
        				wellMem[i][garbageHeight+4][0] = 3;
        			}else{
        				wellMem[i][garbageHeight+1][0] = 4;
        				wellMem[i][garbageHeight+2][0] = 4;
        				wellMem[i][garbageHeight+3][0] = 4;
        				wellMem[i][garbageHeight+4][0] = 4;
        			}
        			break;
            	}else if(mem[i][19-garbageHeight]){
            		break;
            	}else{
            		if(y<minHeight){
            			minHeight = y;
            		}
            		wellMem[i][y][0] = 3;
        		}
        	}
        }
        for(int i=1; i<4; i++){
        	for(int x=0; x<10; x++){
        		for(int y=0; y<=garbageHeight; y++){
        			wellMem[x][y][i] = wellMem[x][y][0];
                }
        		for(int y=garbageHeight+1; y<=garbageHeight+i; y++){
        			wellMem[x][y][i] = 2;
                }
        		for(int y=garbageHeight+i+1; y<20; y++){
        			wellMem[x][y][i] = wellMem[x][y-i][0];
                }
            }
        }
    }
	
	public void start(){
		for(int i=0; i<4; i++){
			search(i, 0);
		}
	}
	  
	public void search(int level, int depth) {
		for(int pieceY=minHeight+1; pieceY<garbageHeight+level+4; pieceY++){
			for(int piece=0; piece<13; piece++){
				nextnode: for(int pieceX=bounds[piece][0]; pieceX<=bounds[piece][1]; pieceX++){
					int[][] piecePos = new int[4][3];//x,y,state
					for(int i=0; i<4; i++){
						piecePos[i][0] = pieceX+tPos[piece][i][0];
						piecePos[i][1] = pieceY+tPos[piece][i][1];
						piecePos[i][2] = wellMem[piecePos[i][0]][piecePos[i][1]][level];
						if(piecePos[i][2]<2){
							continue nextnode;
						}
					}
					boolean locked = false;
					for(int i=0; i<4; i++){
						if(wellMem[piecePos[i][0]][piecePos[i][1]-1][level]==1){
							locked = true;
						}
					}
					if(!locked){
						continue nextnode;
					}
					for(int i=0; i<4; i++){
						if(piecePos[i][2]==5){
							if(wellMem[piecePos[i][0]][garbageHeight-1][level] != 1){
								continue nextnode;
							}
						}
					}
					for(int i=0; i<4; i++){
						wellMem[piecePos[i][0]][piecePos[i][1]][level] = 1;
					}
					int counter = 0;
					for(int x=0; x<10; x++){
						for(int y=garbageHeight; y<=garbageHeight+level; y++){
							if(wellMem[x][y][level]==1){
								counter++;
							}else{
								break;
							}
						}
					}
					if(counter == 10*(level+1)){
						printGame(depth);
						continue nextnode;
					}
					search(level, depth+1);
					for(int i=0; i<4; i++){
						wellMem[piecePos[i][0]][piecePos[i][1]][level] = piecePos[i][2];
					}
				}
			}
		}
	}

	private void printGame(int depth) {
		System.out.println("************************");
		for(int i=0; i<=depth; i++){
			System.out.println(""+solution[i][0]+","+solution[i][1]+","+solution[i][2]);
		}
	}
}
