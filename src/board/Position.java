package board;

public class Position {

	public final int row;
	public final int col;
	
	public Position (int row, int col) {
		this.row = row;
		this.col = col;
	}

	public boolean isValid() {
		return row>=0 && row< 8 && col>=0 && col<8;
	}
	
}
