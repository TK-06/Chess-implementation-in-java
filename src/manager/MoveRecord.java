// MoveRecord.java
package manager;

import board.Position;
import pieces.Piece;

public class MoveRecord {
    public final Piece    piece;
    public final Position from;
    public final Position to;
    public final Piece    captured;
    public final boolean  hadMoved;

    // En passant — all null/−1 when the move was NOT an en passant capture
    public final Piece    epPawn;   // the pawn removed by en passant
    public final int      epRow;    // where that pawn was
    public final int      epCol;
    public final Position prevEnPassantTarget; // board's EP target before this move

    public MoveRecord(Piece piece, Position from, Position to, Piece captured, boolean hadMoved,
                      Piece epPawn, int epRow, int epCol, Position prevEP) {
        this.piece    = piece;
        this.from     = from;
        this.to       = to;
        this.captured = captured;
        this.hadMoved = hadMoved;
        this.epPawn   = epPawn;
        this.epRow    = epRow;
        this.epCol    = epCol;
        this.prevEnPassantTarget = prevEP;
    }
}
