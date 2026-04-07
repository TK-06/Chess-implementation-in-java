package pieces;

import java.util.ArrayList;
import board.Board;
import board.Position;

public class Pawn extends Piece {

    public Pawn(int row, int col, boolean isWhite) {
        super(row, col, isWhite);
    }

    @Override
    public ArrayList<Position> getLegalMoves(Board board) {
        ArrayList<Position> legalMoves = new ArrayList<>();
        int r = getRow();
        int c = getCol();
        int dir = isWhite() ? 1 : -1;  // white goes up (+1), black goes down (-1)

        // 1. one step forward — only if empty
        Position oneStep = new Position(r + dir, c);
        if (oneStep.isValid() && board.isEmpty(r + dir, c)) {
            legalMoves.add(oneStep);

            // 2. two steps — only if first step is clear AND hasn't moved yet
            Position twoStep = new Position(r + 2 * dir, c);
            if (!hasMoved && twoStep.isValid() && board.isEmpty(r + 2 * dir, c)) {
                legalMoves.add(twoStep);
            }
        }

        // 3. diagonal captures — only if an enemy piece is there
        for (int dc : new int[]{-1, 1}) {
            Position capture = new Position(r + dir, c + dc);
            if (capture.isValid()) {
                Piece target = board.getPiece(r + dir, c + dc);
                if (target != null && !target.isSameColor(this)) {
                    legalMoves.add(capture);
                }
            }
        }

        // 4. en passant — the board tracks the square a pawn just skipped over
        Position ep = board.getEnPassantTarget();
        if (ep != null && ep.row == r + dir && Math.abs(ep.col - c) == 1) {
            legalMoves.add(ep);
        }

        return legalMoves;
    }

    @Override
    public String getType() {
        return "Pawn";
    }
}