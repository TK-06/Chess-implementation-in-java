package factory;

import pieces.*;

public class PieceFactory {

    // no constructor needed — all static, just call PieceFactory.create(...)
    private PieceFactory() {}

    public static Piece create(String type, int row, int col, boolean isWhite) {
        switch (type) {
            case "Pawn":   return new Pawn(row, col, isWhite);
            case "Rook":   return new Rook(row, col, isWhite);
            case "Knight": return new Knight(row, col, isWhite);
            case "Bishop": return new Bishop(row, col, isWhite);
            case "Queen":  return new Queen(row, col, isWhite);
            case "King":   return new King(row, col, isWhite);
            default: throw new IllegalArgumentException("Unknown piece type: " + type);
        }
    }
}