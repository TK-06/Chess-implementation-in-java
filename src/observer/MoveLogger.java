package observer;

import board.Position;
import pieces.Piece;
import java.util.ArrayList;

public class MoveLogger implements GameObserver {

    // store each move as a String
    // e.g. "Pawn (1,2) → (2,2)"
    private ArrayList<String> movesHistory = new ArrayList<>();

    @Override
    public void moveMade(Piece p, Position from, Position to) {
        // build a string describing the move and add it to moveHistory
    	String m = p.getType();
    	m += " ("+from.row+"," + from.col + ") → (" + to.row + "," + to.col + ")";
        movesHistory.add(m);
        System.out.println("Move " + movesHistory.size() + ": " + m);
    }

    // one extra method — useful for the GUI later
    public void printMoves() {
        // loop through moveHistory and print each entry
        System.out.println("=== Move History ===");
        for (int i = 0; i < movesHistory.size(); i++) {
            System.out.println((i + 1) + ". " + movesHistory.get(i));
        }
    }

    public ArrayList<String> getHistory() { return movesHistory;}
    public void clear() {movesHistory.clear();}
}