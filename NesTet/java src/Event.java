package Bennett.Bernardoni;

public class Event {
	public int curPiece;
	public int nextPiece;
	public int rand;
	public int waitFrames;
	public int rotation;
	public int totalFrames;

	public int lastX;
	public int lastY;
	public int lineClears[];
	
	public Event(int curPiece, int nextPiece, int rand, int totalFrames, int lastX, int lastY) {
		this.curPiece = curPiece;
		this.nextPiece = nextPiece;
		this.rand = rand;
		this.waitFrames = 0;
		this.rotation = Emu.rotTable[curPiece];
		this.totalFrames = totalFrames;
		this.lastX = lastX;
		this.lastY = lastY;
		this.lineClears = null;
    }
}
