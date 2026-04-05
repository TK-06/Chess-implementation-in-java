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
    private JLabel statusLabel = new JLabel("White's turn", SwingConstants.CENTER);

    public ChessGUI() {
        setTitle("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // status label at top
        statusLabel.setFont(new Font("Serif", Font.BOLD, 16));
        add(statusLabel, BorderLayout.NORTH);

        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // undo / redo buttons
        JButton undoBtn = new JButton("◀ Undo");
        JButton redoBtn = new JButton("Redo ▶");

        undoBtn.addActionListener(e -> {
            GameManager.getInstance().undo();
            boardPanel.clearSelection();
            boardPanel.repaint();
            updateStatus();
        });
        redoBtn.addActionListener(e -> {
            GameManager.getInstance().redo();
            boardPanel.clearSelection();
            boardPanel.repaint();
            updateStatus();
        });

        JPanel controls = new JPanel();
        controls.add(undoBtn);
        controls.add(redoBtn);
        add(controls, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateStatus() {
        GameManager gm = GameManager.getInstance();
        boolean nextIsWhite = gm.isWhiteTurn();
        String side = nextIsWhite ? "White" : "Black";

        if (gm.getCheckDetector().isInCheck(nextIsWhite)) {
            statusLabel.setText(side + " is in CHECK!");
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setText(side + "'s turn");
            statusLabel.setForeground(Color.BLACK);
        }
    }

    @Override
    public void moveMade(Piece p, Position from, Position to) {
        boardPanel.repaint();
        updateStatus();
    }
}