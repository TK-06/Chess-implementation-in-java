package pieces;

import java.util.ArrayList;
import board.Board;
import board.Position;

public class Knight extends Piece{
	public Knight(int row, int col, boolean isWhite) {
		super(row, col, isWhite);
	}

	@Override
	public ArrayList<Position> getLegalMoves(Board board) {
		ArrayList<Position> legalMoves = new ArrayList<Position>();
		int r = super.getRow();
		int c = super.getCol();
		int[][] jumps = {
			    {-2,-1},{-2,1},{-1,-2},{-1,2},
			    {1,-2},{1,2},{2,-1},{2,1}
			};

			for (int[] jump : jumps) {
			    Position p = new Position(r + jump[0], c + jump[1]);
			    if (p.isValid()) {
			    	if (board.isEmpty(r+jump[0], c+jump[1])) {
			    		legalMoves.add(p);
			    	}else {// occupied
			    		Piece target = board.getPiece(r+jump[0], c+jump[1]);
			    		if (!target.isSameColor(this)){
			    			legalMoves.add(p);
			    		}
			    	}
			    }
			}
		return legalMoves;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return "Knight";
	}
}
