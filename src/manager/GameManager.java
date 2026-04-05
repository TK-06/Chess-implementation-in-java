// GameManager.java
package manager;

import board.Board;
import board.Position;
import factory.PieceFactory;
import observer.CheckDetector;
import observer.MoveLogger;
import pieces.Piece;
import java.util.List;
import java.util.Stack;

public class GameManager {

    private static GameManager instance = null;
    private Board board;
    private boolean isWhiteTurn;
    private MoveLogger logger;
    private CheckDetector checkDetector;
    private Stack<MoveRecord> doneStack = new Stack<>();
    private Stack<MoveRecord> undoStack = new Stack<>();

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
            board.setPiece(PieceFactory.create("Pawn",      1, i, true),  1, i);
            board.setPiece(PieceFactory.create(backRank[i], 7, i, false), 7, i);
            board.setPiece(PieceFactory.create("Pawn",      6, i, false), 6, i);
        }
    }

    public boolean makeMove(Position from, Position to) {
        Piece moving = board.getPiece(from.row, from.col);
        if (moving == null) return false;
        if (moving.isWhite() != isWhiteTurn) return false;

        // ① validate piece's own movement rules
        List<Position> legal = moving.getLegalMoves(board);
        boolean valid = legal.stream().anyMatch(p -> p.row == to.row && p.col == to.col);
        if (!valid) return false;

        // ② save state
        Piece captured = board.getPiece(to.row, to.col);
        boolean hadMoved = moving.isHasMoved();

        // ③ temporarily make move
        board.setPiece(moving, to.row,   to.col);
        board.setPiece(null,   from.row, from.col);
        moving.setPosition(to.row, to.col);

        // ④ reject if own King ends up in check
        if (checkDetector.isInCheck(isWhiteTurn)) {
            board.setPiece(moving,   from.row, from.col);
            board.setPiece(captured, to.row,   to.col);
            moving.setPosition(from.row, from.col);
            moving.setHasMoved(hadMoved);
            return false;
        }

        // ⑤ finalize
        moving.setHasMoved(true);
        doneStack.push(new MoveRecord(moving, from, to, captured, hadMoved));
        undoStack.clear();
        isWhiteTurn = !isWhiteTurn;
        board.notifyObservers(moving, from, to);
        
        System.out.println("After temp move — is " + (isWhiteTurn ? "White" : "Black") + " in check? " 
        	    + checkDetector.isInCheck(isWhiteTurn));
        return true;
    }

    public void undo() {
        if (doneStack.isEmpty()) return;
        MoveRecord last = doneStack.pop();

        board.setPiece(last.piece,    last.from.row, last.from.col);
        board.setPiece(last.captured, last.to.row,   last.to.col);
        last.piece.setPosition(last.from.row, last.from.col);
        last.piece.setHasMoved(last.hadMoved);

        undoStack.push(last);
        isWhiteTurn = !isWhiteTurn;
    }

    public void redo() {
        if (undoStack.isEmpty()) return;
        MoveRecord next = undoStack.pop();

        Piece captured = board.getPiece(next.to.row, next.to.col);
        boolean hadMoved = next.piece.isHasMoved();

        board.setPiece(next.piece, next.to.row,   next.to.col);
        board.setPiece(null,       next.from.row, next.from.col);
        next.piece.setPosition(next.to.row, next.to.col);
        next.piece.setHasMoved(true);

        doneStack.push(new MoveRecord(next.piece, next.from, next.to, captured, hadMoved));
        isWhiteTurn = !isWhiteTurn;
        board.notifyObservers(next.piece, next.from, next.to);
    }

    public Board getBoard()                   { return board; }
    public boolean isWhiteTurn()              { return isWhiteTurn; }
    public MoveLogger getLogger()             { return logger; }
    public CheckDetector getCheckDetector()   { return checkDetector; }
}