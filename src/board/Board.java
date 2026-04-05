package board;
import java.util.*;
import pieces.Pawn;
import observer.GameObserver;
import pieces.Piece;

public class Board {
	private Piece[][] table = new Piece[8][8];
	private ArrayList<GameObserver> observers = new ArrayList<>();
	
	public void addObserver (GameObserver o) {observers.add(o);}
	public void removeObserver (GameObserver o) {observers.remove(o);}
	private void notifyObservers(Piece p, Position from, Position to) {
		for (GameObserver o : observers) o.moveMade(p, from, to);
	}
	public boolean movePiece(Position from, Position to) {
		Piece p = table[from.row][from.col];
		if (p == null) return false;
		List<Position> legal = p.getLegalMoves(this);
		boolean valid = legal.stream().anyMatch(pos -> pos.row == to.row && pos.col == to.col);
		if (!valid) return false;
		table[to.row][to.col] = p;
		table[from.row][from.col] = null;
		p.setPosition(to.row, to.col);
		notifyObservers(p,from,to);
		return true;
	}
	
	public boolean isEmpty(int r, int c) {
		return table[r][c] == null;
	}
	public Piece getPiece(int r, int c) {
		return table[r][c];
	}
}