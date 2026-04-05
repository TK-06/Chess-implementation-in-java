// MoveRecord.java
package manager;

import board.Position;
import pieces.Piece;

public class MoveRecord {
    public final Piece piece;
    public final Position from;
    public final Position to;
    public final Piece captured;
    public final boolean hadMoved;  // add this

    public MoveRecord(Piece piece, Position from, Position to, Piece captured, boolean hadMoved) {
        this.piece    = piece;
        this.from     = from;
        this.to       = to;
        this.captured = captured;
        this.hadMoved = hadMoved;  // add this
    }
}