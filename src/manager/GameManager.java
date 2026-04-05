package manager;

import board.Board;
import board.Position;
import factory.PieceFactory;
import observer.CheckDetector;
import observer.MoveLogger;

public class GameManager {

    private static GameManager instance = null;
    private Board board;
    private boolean isWhiteTurn;
    private MoveLogger logger;
    private CheckDetector checkDetector;

    private GameManager() {
        board = new Board();
        isWhiteTurn = true;  // white always goes first

        // wire up observers
        logger = new MoveLogger();
        checkDetector = new CheckDetector(board);
        board.addObserver(logger);
        board.addObserver(checkDetector);

        // set up pieces
        initBoard();
    }

    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    private void initBoard() {
        // use PieceFactory to place all pieces
        // hint: String[] backRank = {"Rook","Knight","Bishop","Queen","King","Bishop","Knight","Rook"};
    }

    public boolean makeMove(Position from, Position to) {
        // hint: check whose turn it is before allowing move
        // if move succeeds, switch turns
    }

    public Board getBoard()        { return board; }
    public boolean isWhiteTurn()   { return isWhiteTurn; }
    public MoveLogger getLogger()  { return logger; }
}