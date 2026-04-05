package pieces;

import java.util.ArrayList;
import board.Board;
import board.Position;

public class King extends Piece{
	public King (int row, int col, boolean isWhite) {
		super(row,col,isWhite);
	}

	@Override
	public ArrayList<Position> getLegalMoves(Board board) {
		int r = super.getRow();
		int c = super.getCol();
		ArrayList<Position> legalMoves = new ArrayList<Position>();
		int[][] dir = {{0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}};
		for (int[] d : dir) {
		    Position p = new Position(r + d[0], c + d[1]);
		    if (p.isValid()) {
		    	if (board.isEmpty(r+d[0], c+d[1])) {
		    		legalMoves.add(p);
		    	}else {// occupied
		    		Piece target = board.getPiece(r+d[0], c+d[1]);
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
		return "King";
	}
}
