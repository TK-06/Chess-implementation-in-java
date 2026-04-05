package pieces;
import java.util.ArrayList;
import board.Board;
import board.Position;

public abstract class Piece {
	
	public int col ;
	public int row;
	public boolean isWhite;
	public boolean hasMoved;
	
	public Piece (int r, int c, boolean isW) {
		this.col = c;
		this.row = r;
		this.isWhite = isW;
		this.hasMoved = false;
	}
	
	public abstract ArrayList<Position> getLegalMoves(Board board);
	
	public abstract String getType();
	
	public boolean isWhite() {return isWhite;}
	
	public int getRow() {return row;}
	public int getCol() {return col;}
	public void setPosition(int row, int col) {
		this.row = row;
		this.col = col;
		this.hasMoved = true;
	}
	
	public boolean isSameColor(Piece other) {
		return this.isWhite == other.isWhite;
	}
	
}
