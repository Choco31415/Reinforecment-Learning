package TicTacToe;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.Random;

public class RandomPlayer extends Player {

	Point2D move;
	GamePanel gp;
	
	Random rng;
	
	public RandomPlayer(GameState gs_) {
		super(gs_);
		
		gp = gs.getGamePanel();
		rng = new Random();
		
		move = new Point2D.Double(0, 0);
	}
	
	public void notifyGameBegin(int id) {
		
	}
	
	@Override
	public Point2D getMove(GameState gs, int id) {
		move.setLocation(rng.nextInt(3), rng.nextInt(3));
		return move;
	}

	@Override
	public void moveCompleted(int id) {
	}

	@Override
	public void notifyGameEnd(double value, int id) {
		//Meh.
	}
}
