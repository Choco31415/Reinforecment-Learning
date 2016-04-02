package TicTacToe;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AIPlayer extends Player {

	private final int NUM_STATES = 19683;
	ArrayList<Double> values = new ArrayList<Double>();
	
	private double eGreedy;
	private double learningRate;
	private boolean exploring;
	private boolean amLearning;
	Random rng;
	
	private int previousStateIndex = 0;
	private int[][] previousState;
	private int currentStateIndex = 0;
	
	ArrayList<Double> futureValues;
	private int delay;
	
	private Point2D move;
	
	public AIPlayer(GameState gs_) {
		super(gs_);
		// TODO Auto-generated constructor stub
		eGreedy = 0.1;
		learningRate = 0.1;
		rng = new Random();
		amLearning = false;
		
		setUpStatesAndValues(new int[3][3], 0);
		
		delay = 0;
		
		move = null;
		previousState = null;
	}
	
	public void setUpStatesAndValues(int[][] board, int boardSpot) {
		for (int i = 0; i < NUM_STATES; i++) {
			values.add(0.5);
		}
	}
	
	public void notifyGameBegin(int id) {
		previousState = cloneBoard(gs.getBoard());
		previousStateIndex = indexOfBoard(gs.getBoard());
		//currentStateIndex = previousStateIndex;
		move = null;
	}
	
	private int[][] cloneBoard(int[][] board) {
		int length = board.length;
		int height = board[0].length;
		int[][] clone = new int[length][height];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < height; j++) {
				clone[i][j] = board[i][j];
			}
		}
		return clone;
	}

	@Override
	public Point2D getMove(GameState gs, int id) {
		//System.out.println(amLearning);
		if (move == null) {
			getMove(id);
		}
		
		if (delay > 0) {
			delay--;
			return null;
		} else {
			if (amLearning && rng.nextDouble() < eGreedy) {
				//System.out.println("exploring");
				exploring = true;
				return new Point2D.Double(rng.nextInt(3), rng.nextInt(3));
			} else {
				exploring = false;
				
				return move;
			}
		}
	}
	
	public void getMove(int id) {
		futureValues = new ArrayList<Double>();
		
		ArrayList<Point2D> vacancies = gs.getVacancies();
		
		int[][] board;
		int maxValueIndex = 0;
		double maxValue = 0;
		for (int i = 0; i < vacancies.size(); i++) {
			//Get a random vacancy
			Point2D point = vacancies.get(i);
			
			//Set the vacancy to player piece.
			board = cloneBoard(gs.getBoard());
			int row = (int)point.getX();
			int col = (int)point.getY();
			board[row][col] = id;
			int boardIndex = indexOfBoard(board);
			
			//Look at value of the resulting state. Should we try it?
			double value = values.get(boardIndex);
			if (id == 2) {
				value = 1 - value;
			}
			futureValues.add(value);
			if (maxValueIndex >= futureValues.size()) {
				throw new Error();
			}
			if (maxValue < value) {
				maxValueIndex = i;
				maxValue = value;
			}
		}
		
		move = vacancies.get(maxValueIndex);
	}
	
	public void updateFutureValues(int id) {
		//System.out.println("update");
		futureValues = new ArrayList<Double>();
		
		ArrayList<Point2D> vacancies = gs.getVacancies();
		
		int[][] board;
		for (int i = 0; i < vacancies.size(); i++) {
			//Get a random vacancy
			Point2D point = vacancies.get(i);
			
			//Set the vacancy to player piece.
			board = cloneBoard(gs.getBoard());
			int row = (int)point.getX();
			int col = (int)point.getY();
			board[row][col] = id;
			int boardIndex = indexOfBoard(board);
			
			//Look at value of the resulting state. Should we try it?
			double value = values.get(boardIndex);
			//System.out.print(boardIndex + ",");
			//System.out.print(value + ",");
			if (id == 2) {
				value = 1 - value;
			}
			futureValues.add(value);

		}
	}

	@Override
	public void moveCompleted(int id) {
		move = null;
		// TODO Auto-generated method stub
		previousStateIndex = currentStateIndex;
		currentStateIndex = indexOfBoard(gs.getBoard());
		if (!exploring && amLearning) {
			//System.out.println("a: " + values.get(previousStateIndex) + "," + values.get(currentStateIndex));
			double value = (1.0 - learningRate)*values.get(previousStateIndex) + learningRate*values.get(currentStateIndex);
			setStateValue(previousState, value);
			//System.out.println("l" + values.get(previousStateIndex));
			//System.out.println((1.0 - learningRate)*values.get(previousStateIndex) + learningRate*values.get(currentStateIndex));
		}
		previousState = cloneBoard(gs.getBoard());
	}

	@Override
	public void notifyGameEnd(double value, int id) {
		
		if (id == 1) {
			previousStateIndex = currentStateIndex;
			int[][] board = gs.getBoard();
			
			setStateValue(board, value);
			
			//To compensate for moveCompleted undoing the AI's work.
			for (int i = 0; i < 2; i++) {
				double pastValue = (1 - learningRate)*values.get(previousStateIndex) + learningRate*values.get(currentStateIndex);
				setStateValue(previousState, pastValue);
			}
			
			if (amLearning) {
				gs.newGame();
			}
		}
	}
	
	public int indexOfBoard(int[][] board) {
		int index = 0;
		int division = 3;
		
		for (int i = 0; i < 9; i++) {
			int col = i%3;
			int row = (i)/3;
			index += board[row][col]*NUM_STATES/division;
			division *= 3;
		}
		
		return index;
	}
	
	public void setLearning(boolean val) {
		amLearning = val;
	}
	
	public ArrayList<Double> getValues() {
		return futureValues;
	}
	
	public void setDelay(int delay_) {
		delay = delay_;
	}
	
	public void setStateValue(int[][] board_, double value) {
		try {
			ArrayList<int[][]> boards = new ArrayList<int[][]>();
			
			int[][] board = cloneBoard(board_);
			boards.add(board);
			boards.add(vFlipBoard(board));
			
			int[][] hboard = hFlipBoard(board);
			boards.add(hboard);
			boards.add(vFlipBoard(hboard));
			
			int[][] dboard = dFlipBoard(board);
			boards.add(dboard);
			boards.add(vFlipBoard(dboard));
			
			hboard = hFlipBoard(dboard);
			boards.add(hboard);
			boards.add(vFlipBoard(hboard));
			
			//System.out.println(value);
			for (int[][] b : boards) {
				int index = indexOfBoard(b);
				values.set(index, value);
				//System.out.print("l," + index);
			}
		} catch (NullPointerException e) {
			
		}
	}
	
	public int[][] vFlipBoard(int[][] board_) {
		int[][] board = cloneBoard(board_);
		int[] temp = board[2];
		board[2] = board[0];
		board[0] = temp;
		return board;
	}
	
	public int[][] hFlipBoard(int[][] board_) {
		int[][] board = cloneBoard(board_);
		for (int i = 0; i < 3; i++) {
			int temp = board[i][2];
			board[i][2] = board[i][0];
			board[i][0] = temp;
		}
		return board;
	}
	
	public int[][] dFlipBoard(int[][] board_) {
		int[][] board = cloneBoard(board_);
		
		int temp = board[2][2];
		board[2][2] = board[0][0];
		board[0][0] = temp;
		
		temp = board[1][0];
		board[1][0] = board[2][1];
		board[2][1] = temp;
		
		temp = board[0][1];
		board[0][1] = board[1][2];
		board[1][2] = temp;
		
		return board;
	}
}
