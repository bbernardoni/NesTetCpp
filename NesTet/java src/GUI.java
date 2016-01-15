package Bennett.Bernardoni;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI extends JFrame implements MouseListener, MouseMotionListener, KeyListener {
	BufferedImage tile = null;
	BufferedImage next = null;
	Display pan = new Display();
	Emu emu;
    
	public GUI(boolean[][] mem, int curPiece, int nextPiece, int rand, int totalFrames) {
        setTitle("Tetris Gui");
        setSize(500, 678);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(pan);
        pan.addMouseListener(this);//comment
        pan.addMouseMotionListener(this);//comment
        addKeyListener(this);
        emu = new Emu(mem, curPiece, nextPiece, rand, totalFrames);
        try {
			tile = ImageIO.read(new File("src/tile.png"));
			next = ImageIO.read(new File("src/next.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	public GUI(Emu emu) {
        setTitle("Tetris Gui");
        setSize(500, 678);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(pan);
        pan.addMouseListener(this);
        pan.addMouseMotionListener(this);
        addKeyListener(this);
        this.emu = emu;
        try {
			tile = ImageIO.read(new File("src/tile.png"));
			next = ImageIO.read(new File("src/next.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	public void startThread() {
        setVisible(true);
        (new displayThread()).start();
	}
	
	public class displayThread extends Thread {
	    public void run() {
	    	while(true){
	    		pan.repaint();
	    		try {
					sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    }
	}
	
	public class Display extends JPanel{
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			draw(g);
		}
	}
	
    public void draw(Graphics g) {
    	g.setColor(Color.BLACK);
    	g.fillRect(0, 0, this.getWidth(), this.getHeight());
    	if(emu.curPieceClear()){//comment
        	for(int i=0; i<4; i++){
            	g.drawImage(next, emu.getCurPieceX(i)*32, emu.getCurPieceY(i)*32, null);
            }
        }
        for(int i=0; i<4; i++){
        	g.drawImage(next, 400+emu.getNextPieceX(i)*32, 300+emu.getNextPieceY(i)*32, null);
        }
        
        for(int x=0; x<10; x++){
        	for(int y=0; y<20; y++){
            	if(emu.isCellFilled(x,y)){
            		g.drawImage(tile, x*32, y*32, null);
            	}
            }
        }
        
        g.setColor(Color.WHITE);
		g.drawString("Wait Frames: "+emu.hist.last().waitFrames, 340, 20);//comment
		g.drawString("Rotation: "+emu.hist.last().rotation, 340, 40);
		g.drawString("Rand: 0x"+Integer.toHexString(emu.hist.last().rand), 340, 60);
		g.drawString("Total Frames: 0x"+Integer.toHexString(emu.hist.last().totalFrames), 340, 80);
		g.drawString("Lines Left: "+(25-emu.linesCleared), 340, 100);
		g.drawString("px: "+emu.px/32, 340, 120);
		g.drawString("py: "+emu.py/32, 340, 140);
    }

    @Override
	public void mousePressed(MouseEvent arg0) {
    	if(emu.curPieceClear()){
    		emu.placePiece();
    	}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		emu.px = (arg0.getX()/32)*32;
		emu.py = (arg0.getY()/32)*32;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		switch(arg0.getKeyCode()){
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			emu.incWaitFrames();
			break;
		case KeyEvent.VK_E:
			emu.incCurWaitFrames();
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			emu.decWaitFrames();
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			emu.rotClockwise();
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			emu.rotCounterClockwise();
			break;
		case KeyEvent.VK_BACK_SPACE:
			emu.undoPlace();
			break;
		case KeyEvent.VK_ENTER:
			emu.printGame();
			break;
		}
	}

	@Override public void mouseClicked(MouseEvent arg0) {}
	@Override public void mouseEntered(MouseEvent arg0) {}
	@Override public void mouseExited(MouseEvent arg0) {}
	@Override public void mouseReleased(MouseEvent arg0) {}
	@Override public void keyReleased(KeyEvent arg0) {}
	@Override public void keyTyped(KeyEvent arg0) {}
	@Override public void mouseDragged(MouseEvent arg0) {}
}