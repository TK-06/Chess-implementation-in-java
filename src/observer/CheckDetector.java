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

    private boolean isInCheck(boolean isWhiteKing) {
        Position kingPos = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.isWhite() == isWhiteKing && p.getType().equals("King")) {
                    kingPos = new Position(r, c);
                }
            }
        }
        if (kingPos == null) return false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.isWhite() != isWhiteKing) {
                    if (p.getLegalMoves(board).contains(kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}