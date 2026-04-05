// GameManager.java
package manager;

import board.Board;
import board.Position;
import factory.PieceFactory;
import observer.CheckDetector;
import observer.MoveLogger;
import pieces.Piece;

public class GameManager {

    private static GameManager instance = null;
    private Board board;
    private boolean isWhiteTurn;
    private MoveLogger logger;
    private CheckDetector checkDetector;

    private GameManager() {
        board = new Board();
        isWhiteTurn = true;
        logger = new MoveLogger();
        checkDetector = new CheckDetector(board);
        board.addObserver(logger);
        board.addObserver(checkDetector);
        initBoard();
    }

    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    private void initBoard() {
        String[] backRank = {"Rook","Knight","Bishop","Queen","King","Bishop","Knight","Rook"};
        for (int i = 0; i < 8; i++) {
            board.setPiece(PieceFactory.create(backRank[i], 0, i, true),  0, i);
            board.setPiece(PieceFactory.create("Pawn", 1, i, true),       1, i);
            board.setPiece(PieceFactory.create(backRank[i], 7, i, false), 7, i);
            board.setPiece(PieceFactory.create("Pawn", 6, i, false),      6, i);
        }
    }

    public boolean makeMove(Position from, Position to) {
        Piece moving = board.getPiece(from.row, from.col);
        if (moving == null) return false;
        if (moving.isWhite() != isWhiteTurn) return false;  // wrong turn
        boolean success = board.movePiece(from, to);
        if (success) isWhiteTurn = !isWhiteTurn;  // switch turns
        return success;
    }

    public Board getBoard()       { return board; }
    public boolean isWhiteTurn()  { return isWhiteTurn; }
    public MoveLogger getLogger() { return logger; }
}