package TicTacToe;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

/**
 * Single press q to enter slow mode.
 * Double press q to enter speed mode.
 * Triple press q to return to normal. (Aka single for slow, then double for normal.)
 * @author ErnieParke
 *
 */
public class GameState {

	GamePanel gp;
	
	int[][] board;
	int turn;
	Player player1;
	Player player2;
	int gameCount;
	boolean playing = false;
	int winner;
	
	Random rng;
	ArrayList<Point2D> vacantSpots = new ArrayList<Point2D>();
	
	AIPlayer ai;
	RandomPlayer rp;
	HumanPlayer hp;
	
	private int trainFor = -1;
	private int gamesTrainedOn = 0;
	
	//For tracking performance
	private int wins = 0;
	private int games = 0;
	private boolean delaying;//Press w to make the ai play. Also displays ai's predicated state values.
	
	//Style
	DecimalFormat formatter = new DecimalFormat("#,###");
	
	public GameState(GamePanel gp_) {
		gp = gp_;
		board = new int[3][3];
		turn = 1 ;
		gameCount = 0;
		
		rng = new Random();
		
		ai = new AIPlayer(this);
		rp = new RandomPlayer(this);
		hp = new HumanPlayer(this);
		
		player1 = rp;
		player2 = hp;
		
		delaying = false;
	}
	
	public void newGame() {
		//Occassionally swap players.
		turn = rng.nextInt(2) + 1;
		
		playing = true;
		winner = -1;
		gameCount++;
		
		board = new int[3][3];
		
		vacantSpots.clear();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				vacantSpots.add(new Point2D.Double(i, j));
				board[i][j] = 0;
			}
		}
		
		if (delaying && gp.getShowWindow()) {
			ai.updateFutureValues(1);
		}
		
		player1.notifyGameBegin(1);
		player2.notifyGameBegin(2);
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_W) {
			ai.setDelay(0);
		}
		
	}
	
	public void update() {
		if (playing) {
			if (turn == 1) {
				if (gp.getShowWindow() && player1 == ai) {
					ai.updateFutureValues(1);
				}
				Point2D point = player1.getMove(this, turn);
				if (point != null) {
					//System.out.println(player1.getClass().getSimpleName() + ":" + point);
					//Perform move.
					setTile(point, 1);
					player1.moveCompleted(1);
					turn = 2;
					
					if (gp.getShowWindow() && delaying) {
						ai.updateFutureValues(1);
					}
				}
			} else {
				Point2D point = player2.getMove(this, turn);
				if (point != null) {
					//Perform move.
					setTile(point, 2);
					player2.moveCompleted(2);
					turn = 1;
					
					if (gp.getShowWindow() && delaying) {
						ai.updateFutureValues(1);
					}
				}
			}
		}
		
		if (delaying && gp.getShowWindow()) {
			ai.setDelay(1);
		}

		if (winner != -1) {
			notifyGameOver();
		}
		
	}
	
	private void notifyGameOver() {
		if (winner == 0) {
			player1.notifyGameEnd(0.5, 1);
			player2.notifyGameEnd(0.5, 2);
		} else if (winner == 1) {
			player1.notifyGameEnd(1, 1);
			player2.notifyGameEnd(0, 2);
		} else {
			player1.notifyGameEnd(0, 1);
			player2.notifyGameEnd(1, 2);
		}
		
		if (trainFor > -1) {
			trainFor--;
			gamesTrainedOn++;
			
			if (trainFor == 0) {
				trainFor = -1;
				ai.setLearning(false);
				player2 = hp;
				
				if (!gp.getShowWindow()) {
					gp.toggleFPS();
				}
				
				if (delaying) {
					ai.updateFutureValues(1);
				}
			}
		}
	}
	
	public void hardReset() {
		newGame();
	}
	
	/*
	 * 0 - Tie
	 * 1 - Player 1
	 * 2 - Player 2
	 */
	public int getWinner() {
		return winner;
	}
	
	public Player getPlayer(int id) {
		if (id > 2 || id < 1) {
			throw new Error("Out of bounds.");
		}
		if (id == 1) {
			return player1;
		} else {
			return player2;
		}
	}
	
	public void setPlayer(int id, Player player) {
		if (id > 2 || id < 1) {
			throw new Error("Out of bounds.");
		}
		if (id == 1) {
			player1 = player;
		} else {
			player2 = player;
		}
	}
	
	public int getTile(int row, int col) {
		return board[row][col];
	}
	
	public int[][] getBoard() {
		return board;
	}
	
	public ArrayList<Point2D> getVacancies() {
		return vacantSpots;
	}
	
	public void setTile(Point2D spot, int type) {
		if (vacantSpots.contains(spot)) {
			//Board spot occupied. Do random move.
			int row = (int)spot.getX();
			int col = (int)spot.getY();
			if (row > 2 || row < 0 || col > 2 || col < 0) {
				throw new Error("Out of bounds.");
			}
			board[row][col] = type;
			vacantSpots.remove(spot);
		} else {
			//Board spot full. Place piece at random vacant spot.
			if (vacantSpots.size() == 0) {
				throw new Error("Out of spots! Sorry.");
			}
			Point2D newSpot = vacantSpots.get(rng.nextInt(vacantSpots.size()));
			int row = (int)newSpot.getX();
			int col = (int)newSpot.getY();
			board[row][col] = type;
			vacantSpots.remove(newSpot);
		}
		checkForGameOver();
	}
	
	public void checkForGameOver() {
		//Check vertical and horizontal combos.
		int commonTile;
		for (int i = 0; i < 3; i++) {
			commonTile = board[i][0];
			if (board[i][1] == commonTile && board[i][2] == commonTile && commonTile > 0) {
				winner = commonTile;
				playing = false;
				return;
			}
		}
		for (int j = 0; j < 3; j++) {
			commonTile = board[0][j];
			if (board[1][j] == commonTile && board[2][j] == commonTile && commonTile > 0) {
				winner = commonTile;
				playing = false;
				return;
			}
		}
		
		//Check diagonals.
		commonTile = board[0][0];
		if (board[1][1] == commonTile && board[2][2] == commonTile && commonTile>0) {
			winner = commonTile;
			playing = false;
			return;
		}
		commonTile = board[0][2];
		if (board[1][1] == commonTile && board[2][0] == commonTile && commonTile>0) {
			winner = commonTile;
			playing = false;
			return;
		}
		if (vacantSpots.size() == 0) {
			winner = 0;
			playing = false;
			return;
		}
	}

	public void draw(Graphics2D g) {
		int tileSize = gp.getInnerHeight()/3;
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, gp.getWidth(), gp.getInnerHeight());
		g.setColor(Color.BLACK);
		g.drawLine(tileSize, 0, tileSize, gp.getInnerHeight());
		g.drawLine(tileSize*2, 0, tileSize*2, gp.getInnerHeight());
		g.drawLine(tileSize*3, 0, tileSize*3, gp.getInnerHeight());
		g.drawLine(0, tileSize, gp.getInnerHeight(), tileSize);
		g.drawLine(0, tileSize*2, gp.getInnerHeight(), tileSize*2);
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board[i][j] == 1) {
					g.setColor(Color.RED);
					g.drawOval(i*tileSize, j*tileSize, tileSize, tileSize);
				} else if (board[i][j] == 2) {
					g.setColor(Color.BLUE);
					g.drawLine(i*tileSize, j*tileSize, (i+1)*tileSize, (j+1)*tileSize);
					g.drawLine(i*tileSize, (j+1)*tileSize, (i+1)*tileSize, j*tileSize);
				}
			}
		}
		String winStatus;
		if (winner == -1) {
			winStatus = "Playing.";
		} else if (winner == 0) {
			winStatus = "Tied.";
		} else if (winner == 1) {
			winStatus = "Player 1 won!";
		} else {
			winStatus = "Player 2 won!";
		}
		
		String currentPlayer;
		if (winner == -1) {
			currentPlayer = "Waiting on Player " + turn + ".";
		} else {
			currentPlayer = "";
		}
		
		String player1Status = "Player1 is: " + player1.getClass().getSimpleName();
		String player2Status = "Player2 is: " + player2.getClass().getSimpleName();
		
		String training = "AI games: " + formatter.format(gamesTrainedOn);
		
		String AIquality = "AI win percent: " + (double)wins/games ;
		
		g.setColor(Color.BLACK);
		int leftMargin = gp.getInnerHeight() + 10;
		g.drawString(winStatus, leftMargin, 20);
		g.drawString(currentPlayer, leftMargin, 40);
		g.drawString(player1Status, leftMargin, 80);
		g.drawString(player2Status, leftMargin, 100);
		g.drawString(training, leftMargin, 140);
		g.drawString(AIquality, leftMargin, 160);
		
		if (delaying) {
			ArrayList<Double> values = ai.getValues();
			if (values != null) {
				for (int i = 0; i < vacantSpots.size(); i++) {
					try {
						String value = new Double(Math.max(values.get(i), 0.001)).toString();
						Point2D spot = vacantSpots.get(i);
						g.drawString(value.substring(0, Math.min(5, value.length())), (int)spot.getX()*tileSize + tileSize/2, (int)spot.getY()*tileSize + tileSize/2);
					} catch (IndexOutOfBoundsException e) {
						g.drawString("Nan", 10, 10);
					}
				}
			}
		}
	}
	
	public GamePanel getGamePanel() {
		return gp;
	}
	
	public void swapRandomAndAIPlayers() {
		if (player1.equals(rp)) {
			player1 = ai;
		} else {
			player1 = rp;
		}
		newGame();
	}
	
	public void trainAI(int games_) {
		player1 = ai;
		player2 = ai;
		
		trainFor = games_;
		ai.setLearning(true);
	}
	
	public void measureAI(int games_) {
		//what
	}
	
	public void clearAI() {
		ai = new AIPlayer(this);
		gamesTrainedOn = 0;
		trainFor = 1;
		if (!gp.getShowWindow()) {
			gp.toggleFPS();
		}
		newGame();
	}
}
