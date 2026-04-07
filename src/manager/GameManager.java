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

    public static final String VERSION = "1.2.2";

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
        System.out.println("VERSION: " + VERSION);
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
        if (pendingPromotion) return false;
        Piece moving = board.getPiece(from.row, from.col);
        if (moving == null) return false;
        if (moving.isWhite() != isWhiteTurn) return false;

        // ① validate piece's own movement rules
        List<Position> legal = moving.getLegalMoves(board);
        boolean valid = legal.stream().anyMatch(p -> p.row == to.row && p.col == to.col);
        if (!valid) return false;

        // ② save state & detect en passant
        Piece captured = board.getPiece(to.row, to.col);
        boolean hadMoved = moving.isHasMoved();
        Position prevEP  = board.getEnPassantTarget();

        boolean isEnPassant = moving.getType().equals("Pawn")
                && prevEP != null
                && to.row == prevEP.row && to.col == prevEP.col
                && captured == null;                // en passant square is always empty
        Piece  epPawn = null;
        int    epRow  = -1, epCol = -1;
        if (isEnPassant) {
            epRow  = from.row;   // captured pawn sits on same rank as moving pawn
            epCol  = to.col;
            epPawn = board.getPiece(epRow, epCol);
        }

        // ③ temporarily make move
        board.setPiece(moving, to.row,   to.col);
        board.setPiece(null,   from.row, from.col);
        moving.setPosition(to.row, to.col);
        if (isEnPassant) board.setPiece(null, epRow, epCol);

        // ④ reject if own King ends up in check
        if (checkDetector.isInCheck(isWhiteTurn)) {
            board.setPiece(moving,   from.row, from.col);
            board.setPiece(captured, to.row,   to.col);
            moving.setPosition(from.row, from.col);
            moving.setHasMoved(hadMoved);
            if (isEnPassant) board.setPiece(epPawn, epRow, epCol);
            return false;
        }

        // ⑤ update en passant target for next move
        if (moving.getType().equals("Pawn") && Math.abs(to.row - from.row) == 2) {
            board.setEnPassantTarget(new Position((from.row + to.row) / 2, from.col));
        } else {
            board.setEnPassantTarget(null);
        }

        // ⑥ finalize
        moving.setHasMoved(true);
        doneStack.push(new MoveRecord(moving, from, to, captured, hadMoved,
                epPawn, epRow, epCol, prevEP));
        undoStack.clear();
        isWhiteTurn = !isWhiteTurn;
        board.notifyObservers(moving, from, to);

        // ⑦ pawn promotion — pause game until player picks a piece
        if (moving.getType().equals("Pawn")) {
            int backRank = moving.isWhite() ? 7 : 0;
            if (to.row == backRank) {
                pendingPromotion = true;
                promotionSquare  = new Position(to.row, to.col);
                promotionIsWhite = moving.isWhite();
            }
        }

        // ⑧ check for checkmate / stalemate (only after promotion is resolved)
        if (!pendingPromotion && (isCheckmate(isWhiteTurn) || isStalemate(isWhiteTurn))) {
            gameOver = true;
        }
        return true;
    }

    /** Called by BoardPanel when the player clicks a promotion choice. */
    public void promote(String pieceType) {
        if (!pendingPromotion) return;
        int row = promotionSquare.row;
        int col = promotionSquare.col;

        Piece promoted = PieceFactory.create(pieceType, row, col, promotionIsWhite);
        promoted.setHasMoved(true);
        board.setPiece(promoted, row, col);

        System.out.println("Pawn promoted to " + pieceType + " (" + row + "," + col + ")");

        pendingPromotion = false;
        promotionSquare  = null;

        String currentSide = isWhiteTurn ? "White" : "Black";
        String winningSide  = isWhiteTurn ? "Black" : "White";

        if (isCheckmate(isWhiteTurn)) {
            gameOver = true;
            System.out.println("CHECKMATE! " + winningSide + " wins!");
        } else if (isStalemate(isWhiteTurn)) {
            gameOver = true;
            System.out.println("STALEMATE! It's a draw.");
        } else if (checkDetector.isInCheck(isWhiteTurn)) {
            System.out.println(currentSide + " King is in CHECK!");
        }
    }

    public boolean isPendingPromotion()  { return pendingPromotion; }
    public Position getPromotionSquare() { return promotionSquare; }
    public boolean isPromotionWhite()    { return promotionIsWhite; }

    public void undo() {
        // Cancel any pending promotion and undo the pawn move
        if (pendingPromotion) {
            pendingPromotion = false;
            promotionSquare  = null;
        }
        if (doneStack.isEmpty()) return;
        MoveRecord last = doneStack.pop();

        board.setPiece(last.piece,    last.from.row, last.from.col);
        board.setPiece(last.captured, last.to.row,   last.to.col);
        last.piece.setPosition(last.from.row, last.from.col);
        last.piece.setHasMoved(last.hadMoved);

        // Restore en-passant captured pawn
        if (last.epPawn != null) {
            board.setPiece(last.epPawn, last.epRow, last.epCol);
            last.epPawn.setPosition(last.epRow, last.epCol);
        }

        // Restore the en passant target that was active before this move
        board.setEnPassantTarget(last.prevEnPassantTarget);

        undoStack.push(last);
        isWhiteTurn = !isWhiteTurn;
        gameOver    = false;   // allow continuing after undo
    }

    public void redo() {
        if (undoStack.isEmpty()) return;
        MoveRecord next = undoStack.pop();

        Piece    captured = board.getPiece(next.to.row, next.to.col);
        boolean  hadMoved = next.piece.isHasMoved();
        Position prevEP   = board.getEnPassantTarget();

        board.setPiece(next.piece, next.to.row,   next.to.col);
        board.setPiece(null,       next.from.row, next.from.col);
        next.piece.setPosition(next.to.row, next.to.col);
        next.piece.setHasMoved(true);

        // Re-apply en passant capture
        if (next.epPawn != null) board.setPiece(null, next.epRow, next.epCol);

        // Restore en passant target
        if (next.piece.getType().equals("Pawn")
                && Math.abs(next.to.row - next.from.row) == 2) {
            board.setEnPassantTarget(
                    new Position((next.from.row + next.to.row) / 2, next.from.col));
        } else {
            board.setEnPassantTarget(null);
        }

        doneStack.push(new MoveRecord(next.piece, next.from, next.to, captured, hadMoved,
                next.epPawn, next.epRow, next.epCol, prevEP));
        isWhiteTurn = !isWhiteTurn;
        board.notifyObservers(next.piece, next.from, next.to);
    }

    public List<Position> getLegalMovesFiltered(Position from) {
        Piece moving = board.getPiece(from.row, from.col);
        if (moving == null) return new ArrayList<>();

        List<Position> filtered = new ArrayList<>();
        for (Position to : moving.getLegalMoves(board)) {
            Piece    captured = board.getPiece(to.row, to.col);
            boolean  hadMoved = moving.isHasMoved();
            Position ep       = board.getEnPassantTarget();

            boolean isEnPassant = moving.getType().equals("Pawn")
                    && ep != null
                    && to.row == ep.row && to.col == ep.col
                    && captured == null;
            Piece epPawn = isEnPassant ? board.getPiece(from.row, to.col) : null;

            board.setPiece(moving,   to.row,   to.col);
            board.setPiece(null,     from.row, from.col);
            moving.setPosition(to.row, to.col);
            if (isEnPassant) board.setPiece(null, from.row, to.col);

            if (!checkDetector.isInCheck(moving.isWhite())) filtered.add(to);

            board.setPiece(moving,   from.row, from.col);
            board.setPiece(captured, to.row,   to.col);
            moving.setPosition(from.row, from.col);
            moving.setHasMoved(hadMoved);
            if (isEnPassant) board.setPiece(epPawn, from.row, to.col);
        }
        return filtered;
    }

    private boolean hasNoLegalMoves(boolean white) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.isWhite() == white) {
                    if (!getLegalMovesFiltered(new Position(r, c)).isEmpty())
                        return false;
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

    public void resetGame() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board.setPiece(null, r, c);

        board.setEnPassantTarget(null);
        doneStack.clear();
        undoStack.clear();
        isWhiteTurn      = true;
        gameOver         = false;
        pendingPromotion = false;
        promotionSquare  = null;
        logger.clear();
        initBoard();
        System.out.println("VERSION: " + VERSION + " — New game started");
    }

    public boolean isGameOver()               { return gameOver; }
    public Board getBoard()                   { return board; }
    public boolean isWhiteTurn()              { return isWhiteTurn; }
    public MoveLogger getLogger()             { return logger; }
    public CheckDetector getCheckDetector()   { return checkDetector; }
}
