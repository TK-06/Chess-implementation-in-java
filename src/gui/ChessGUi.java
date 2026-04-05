// ChessGUI.java
package gui;

import observer.GameObserver;
import board.Position;
import pieces.Piece;

import javax.swing.*;

public class ChessGUI extends JFrame implements GameObserver {

    private BoardPanel boardPanel;

    public ChessGUI() {
        setTitle("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        boardPanel = new BoardPanel();
        add(boardPanel);
        pack();
        setLocationRelativeTo(null);  // center on screen
        setVisible(true);
    }

    @Override
    public void moveMade(Piece p, Position from, Position to) {
        boardPanel.repaint();  // redraw whenever a move happens
    }
}