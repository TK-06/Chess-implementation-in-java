package board;

import java.util.Objects;

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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Position other)) return false;
		return row == other.row && col == other.col;
	}

	@Override
	public int hashCode() {
		return Objects.hash(row, col);
	}
	
}
