package TicTacToe;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

public abstract class Player implements MouseListener {
	
	protected GameState gs;
	
	public Player(GameState gs_) {
		gs = gs_;
	}
	
	public abstract void notifyGameBegin(int id);
	
	public abstract Point2D getMove(GameState gs, int id);
	
	public abstract void moveCompleted(int id);
	
	public abstract void notifyGameEnd(double value, int id);
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
