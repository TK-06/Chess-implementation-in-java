// CheckDetector.java
package observer;

import board.Position;
import pieces.Piece;
import board.Board;

public class CheckDetector implements GameObserver {

    private Board board;

    public CheckDetector(Board board) {
        this.board = board;
    }

    @Override
    public void moveMade(Piece p, Position from, Position to) {
        boolean opponentIsWhite = !p.isWhite();
        if (isInCheck(opponentIsWhite)) {
            System.out.println((opponentIsWhite ? "White" : "Black") + " is in check!");
        }
    }

    public boolean isInCheck(boolean isWhiteKing) {
        Position kingPos = null;

        // step 1 — find the king
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.isWhite() == isWhiteKing && p.getType().equals("King")) {
                    kingPos = new Position(r, c);
                }
            }
        }
        if (kingPos == null) return false;

        // step 2 — check if any enemy can reach the king
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.isWhite() != isWhiteKing) {
                    for (Position pos : p.getLegalMoves(board)) {
                        if (pos.equals(kingPos)) return true;
                    }
                }
            }
        }
        return false;
    }
}