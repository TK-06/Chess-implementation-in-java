package pieces;

import java.util.ArrayList;
import board.Board;
import board.Position;

public class Bishop extends Piece{
	public Bishop (int row, int col, boolean isWhite) {
		super(row,col,isWhite);
	}

	@Override
	public ArrayList<Position> getLegalMoves(Board board) {
		int r = super.getRow();
		int c = super.getCol();
		ArrayList<Position> legalMoves = new ArrayList<Position>();
		int[][] dir = {{1,1},{1,-1},{-1,1},{-1,-1}};
		for (int[] d : dir) {
			int nr = r+ d[0];
			int nc = c + d[1];
			while(new Position(nr,nc).isValid()) {
				Piece target= board.getPiece(nr, nc);
				if(target == null) {
					legalMoves.add(new Position(nr,nc));
				}else if(!target.isSameColor(this)) {
					legalMoves.add(new Position(nr,nc));
					break;
				}else {
					break;
				}
				nr += d[0];
				nc += d[1];
			}
		}
		return legalMoves;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "Bishop";
	}
}
