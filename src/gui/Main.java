// Main.java
package gui;

import manager.GameManager;
import board.Board;
import pieces.Piece;

public class Main {
    public static void main(String[] args) {
        GameManager gm = GameManager.getInstance();

        // wire ChessGUI as an observer
        ChessGUI gui = new ChessGUI();
        gm.getBoard().addObserver(gui);
     // temporarily add this to Main.java
        Piece p = gm.getBoard().getPiece(1, 0);
        System.out.println("Piece at (1,0): " + (p != null ? p.getType() : "null"));
    }
}