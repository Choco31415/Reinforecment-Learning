package TicTacToe;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 
 * This class is basically the "main" class of the code, handling the genetic algorithm, tanks, mines, ect...
 * 
 * A lot of aspects of the program I left as variables so that they could be edited easily and quickly.
 * Feel free to edit them!
 *
 */

@SuppressWarnings({ "serial", "unused" })
public class GamePanel extends JPanel implements Runnable, ActionListener, KeyListener {
	
	// window dimensions
	public static final int WIDTH = 600;
	public static int HEIGHT = 440;
	public static final int SCALE = 1;
	
	// image variables
	private BufferedImage image;
	private Graphics2D g;
	
	//FPS stuff
	public static final int SLOW_FPS = 3;
	public static final int NORMAL_FPS = 30;
	public static final int FAST_FPS = 450000;
	public static int FPS = NORMAL_FPS;
	private double targetTime;
	
	//extra vars
	boolean EXTRA_INFO = true;
	double UPDATE_FREQUENCY = 5.0;
	
	boolean showWindow = true;
	boolean running = true;
	
	private JFrame game;
	
	//Few rendering vars for more accurate FPS control. FPS is accurate to about 0.3%
	int framesNeeded;
	double startOfSecond;
	
	GameState state;
	
	//UI
	JButton newGameButton;
	JButton swapPlayer1Button;
	JButton trainButton1;
	JButton trainButton2;
	JButton trainButton3;
	JButton trainButton4;
	JButton clearAI;
	
	public GamePanel(JFrame game_) {
		super();
		
		game = game_;
		
		setPreferredSize(
			new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		setFocusable(true);
		requestFocus();
		
		//showWindow = false;
		
		state = new GameState(this);
		state.newGame();
		
		addMouseListener(state.getPlayer(1));
		addMouseListener(state.getPlayer(2));
		addKeyListener(this);
		
		this.setLayout(null);
		
		newGameButton = new JButton("Start New Game");
		newGameButton.setBounds(0,getInnerHeight(),150,20);
		swapPlayer1Button = new JButton("Swap Random & AI");
		swapPlayer1Button.setBounds(150,getInnerHeight(),150,20);
		trainButton1 = new JButton("Train AI 1,000 games");
		trainButton1.setBounds(300,getInnerHeight(),150,20);
		trainButton2 = new JButton("Train AI 10,000 games");
		trainButton2.setBounds(450,getInnerHeight(),150,20);
		trainButton3 = new JButton("Train AI 100,000");
		trainButton3.setBounds(300,getInnerHeight()+20,150,20);
		trainButton4 = new JButton("Train AI 1,000,000");
		trainButton4.setBounds(450,getInnerHeight()+20,150,20);
		clearAI = new JButton("New AI");
		clearAI.setBounds(0,getInnerHeight()+20,150,20);
		
		this.add(newGameButton);
		this.add(swapPlayer1Button);
		this.add(trainButton1);
		this.add(trainButton2);
		this.add(trainButton3);
		this.add(trainButton4);
		this.add(clearAI);
		
		newGameButton.addActionListener(this);
		swapPlayer1Button.addActionListener(this);
		trainButton1.addActionListener(this);
		trainButton2.addActionListener(this);
		trainButton3.addActionListener(this);
		trainButton4.addActionListener(this);
		clearAI.addActionListener(this);
		
		validate();
	}
	
	private void init() {
		image = new BufferedImage(WIDTH, HEIGHT - 40, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
	}
	
	//This particular version of run allows for a wide range of FPS values, including those in the thousands.
	@Override
	public void run() {
		init();
		
		long start = System.nanoTime();
		//long elapsed
		double wait = 0.0;
		float totalTime = 0;
		long elapsed = 0;
		long currentTime = start;
		float updateTime = 0f;
		long lastUpdateTime = start;
		int frameCounter = 0;
		framesNeeded = FPS;
		startOfSecond = start;
		
		while(running) {
			// 1000 is a second.

			start = System.nanoTime();
			
			framesNeeded--;
			
			if (framesNeeded == 0) {
				framesNeeded = FPS;
				startOfSecond = start;
			}
			
			// Do the brunt of things.
			update();
			draw();
			drawToScreen();
			
			// Print out a game info readout.
			frameCounter++;
			if (EXTRA_INFO && (System.nanoTime() - lastUpdateTime)/1000000 > UPDATE_FREQUENCY*1000) {
				System.out.println("fps:" + (frameCounter/UPDATE_FREQUENCY));
				//Do something.
				lastUpdateTime = System.nanoTime();
				frameCounter = 0;
			}
			
			// Update times, and wait if needed.
			currentTime = System.nanoTime();

			targetTime = (startOfSecond / 1000000.0 + 1000.0 - currentTime / 1000000.0) / framesNeeded;
			
			elapsed = currentTime - start;
			
			wait = (double) ((targetTime - elapsed / 1000000.0) + wait); 
			
			if ((int)Math.floor(wait) > 0) {
				try {
					Thread.sleep((int)Math.floor(wait));
					wait -= (int)Math.floor(wait);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void setFPS(int FPS_) {
		FPS = FPS_;
		framesNeeded = FPS;
		startOfSecond = System.nanoTime();
	}
	
	public void toggleFPS() {
		if (FPS == NORMAL_FPS) {
			setFPS(SLOW_FPS);
		} else if (FPS == FAST_FPS){
			toggleWindow();
			setFPS(NORMAL_FPS);
		} else {
			setFPS(FAST_FPS);
			toggleWindow();
		}
	}
	
    public void toggleWindow() {
    	showWindow = !showWindow;
    	if (showWindow) {
    		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
    		setFPS(NORMAL_FPS);
    	} else {
    		setPreferredSize(new Dimension(80, 50));
    		setFPS(FAST_FPS);
    	}
    	game.pack();
    }
	
	/*
	 * This method updates the program one frame at a time.
	 */
	private void update() {
		state.update();
	}

	/*
	 * This method handles all graphical information, but does not draw to the screen.
	 */
	private void draw() {
		if (showWindow) {
			state.draw(g);	
		}
	}
	
	/*
	 * This method pushes all graphical information to the screen. Aka it draws everything.
	 */
	private void drawToScreen() {
		if (showWindow) {
			Graphics g2 = getGraphics();
			g2.drawImage(image,  0,  0, WIDTH * SCALE, image.getHeight() * SCALE, null);
			
			g2.dispose();
		}
	}
	
	public void close() {
		//If the game is closed, save any relevant data.
		//Meh.
	}
	
	public int getWidth() {
		return WIDTH;
	}
	
	public int getInnerHeight() {
		return HEIGHT - 40;
	}
	
	public boolean getShowWindow() {
		return showWindow;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(newGameButton)) {
			state.newGame();
		} else if (e.getSource().equals(swapPlayer1Button)) {
			state.swapRandomAndAIPlayers();
		} else if (e.getSource().equals(trainButton1)) {
			state.trainAI(1000);
		} else if (e.getSource().equals(trainButton2)) {
			state.trainAI(10000);
		} else if (e.getSource().equals(trainButton3)) {
			state.trainAI(100000);
		} else if (e.getSource().equals(trainButton4)) {
			state.trainAI(1000000);
		} else if (e.getSource().equals(clearAI)) {
			state.clearAI();
		}
		requestFocus();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_Q) {
			toggleFPS();
		}
		
		state.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}