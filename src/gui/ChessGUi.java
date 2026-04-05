// ChessGUI.java
package gui;

import observer.GameObserver;
import board.Position;
import manager.GameManager;
import pieces.Piece;

import javax.swing.*;
import java.awt.*;

public class ChessGUI extends JFrame implements GameObserver {

    private BoardPanel boardPanel;

    public ChessGUI() {
        setTitle("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // undo / redo buttons
        JButton undoBtn = new JButton("◀ Undo");
        JButton redoBtn = new JButton("Redo ▶");

        undoBtn.addActionListener(e -> {
            GameManager.getInstance().undo();
            boardPanel.repaint();
        });
        redoBtn.addActionListener(e -> {
            GameManager.getInstance().redo();
            boardPanel.repaint();
        });

        JPanel controls = new JPanel();
        controls.add(undoBtn);
        controls.add(redoBtn);
        add(controls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void moveMade(Piece p, Position from, Position to) {
        boardPanel.repaint();
    }
}