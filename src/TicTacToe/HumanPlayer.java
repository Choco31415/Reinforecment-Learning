package TicTacToe;

import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

public class HumanPlayer extends Player implements MouseListener {

	Point2D move;
	GamePanel gp;
	
	boolean waitingForMouseUpToStart;
	boolean deciding;
	
	public HumanPlayer(GameState gs_) {
		super(gs_);
		gp = gs.getGamePanel();
		
		move = new Point2D.Double(0, 0);
		
		waitingForMouseUpToStart = true;
		deciding = true;
	}
	
	public void notifyGameBegin(int id) {
		
	}
	
	@Override
	public Point2D getMove(GameState gs, int id) {
		if (deciding) {
			return null;
		} else {
			return move;
		}
	}

	@Override
	public void moveCompleted(int id) {
		deciding = true;
	}

	@Override
	public void notifyGameEnd(double value, int id) {
		//Meh.
		waitingForMouseUpToStart = true;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!waitingForMouseUpToStart) {
			if (e.getY() < gp.getInnerHeight() && e.getY() > 0) {
				if (e.getX() < gp.getInnerHeight() && e.getX() > 0) {
					//Mouse clicked the board.
					move.setLocation(e.getX()*3/gp.getInnerHeight(),e.getY()*3/gp.getInnerHeight());
					deciding = false;
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		waitingForMouseUpToStart = false;
	}
}
