// GameManager.java
package manager;

import board.Board;
import board.Position;
import factory.PieceFactory;
import observer.CheckDetector;
import observer.MoveLogger;
import pieces.Piece;
import java.util.ArrayList;
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
    private boolean gameOver = false;

    // Promotion state
    private boolean pendingPromotion = false;
    private Position promotionSquare  = null;
    private boolean promotionIsWhite  = false;

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
        if (gameOver) return false;
        if (pendingPromotion) return false;   // must resolve promotion first
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

        // ⑥ pawn promotion — pause game until player picks a piece
        if (moving.getType().equals("Pawn")) {
            int backRank = moving.isWhite() ? 7 : 0;
            if (to.row == backRank) {
                pendingPromotion = true;
                promotionSquare  = new Position(to.row, to.col);
                promotionIsWhite = moving.isWhite();
            }
        }

        // ⑦ check for checkmate / stalemate (only after promotion is resolved)
        if (!pendingPromotion && (isCheckmate(isWhiteTurn) || isStalemate(isWhiteTurn))) {
            gameOver = true;
        }
        return true;
    }

    /** Called by BoardPanel when the player clicks a promotion choice. */
    public void promote(String pieceType) {
        if (!pendingPromotion) return;
        Piece promoted = PieceFactory.create(pieceType,
                promotionSquare.row, promotionSquare.col, promotionIsWhite);
        promoted.setHasMoved(true);
        board.setPiece(promoted, promotionSquare.row, promotionSquare.col);
        pendingPromotion = false;
        promotionSquare  = null;
    }

    public boolean isPendingPromotion()  { return pendingPromotion; }
    public Position getPromotionSquare() { return promotionSquare; }
    public boolean isPromotionWhite()    { return promotionIsWhite; }

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

    public List<Position> getLegalMovesFiltered(Position from) {
        Piece moving = board.getPiece(from.row, from.col);
        if (moving == null) return new ArrayList<>();

        List<Position> filtered = new ArrayList<>();
        for (Position to : moving.getLegalMoves(board)) {
            Piece captured = board.getPiece(to.row, to.col);
            boolean hadMoved = moving.isHasMoved();

            board.setPiece(moving,   to.row,   to.col);
            board.setPiece(null,     from.row, from.col);
            moving.setPosition(to.row, to.col);

            if (!checkDetector.isInCheck(moving.isWhite())) {
                filtered.add(to);
            }

            board.setPiece(moving,   from.row, from.col);
            board.setPiece(captured, to.row,   to.col);
            moving.setPosition(from.row, from.col);
            moving.setHasMoved(hadMoved);
        }
        return filtered;
    }

    /** Returns true if the given side has no legal moves at all. */
    private boolean hasNoLegalMoves(boolean white) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.isWhite() == white) {
                    if (!getLegalMovesFiltered(new Position(r, c)).isEmpty()) return false;
                }
            }
        }
        return true;
    }

    public boolean isCheckmate(boolean white) {
        return checkDetector.isInCheck(white) && hasNoLegalMoves(white);
    }

    public boolean isStalemate(boolean white) {
        return !checkDetector.isInCheck(white) && hasNoLegalMoves(white);
    }

    public boolean isGameOver()               { return gameOver; }
    public Board getBoard()                   { return board; }
    public boolean isWhiteTurn()              { return isWhiteTurn; }
    public MoveLogger getLogger()             { return logger; }
    public CheckDetector getCheckDetector()   { return checkDetector; }
}