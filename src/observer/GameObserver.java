package observer;
import board.Position;
import pieces.Piece;
public interface GameObserver {
	void moveMade(Piece p, Position from, Position to);
}
