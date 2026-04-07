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
        boardPanel.setOnPromotionComplete(this::updateStatus);
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

    private boolean gameOverShown = false;

    private void updateStatus() {
        GameManager gm = GameManager.getInstance();
        boolean nextIsWhite = gm.isWhiteTurn();
        String currentSide = nextIsWhite ? "White" : "Black";
        String winningSide  = nextIsWhite ? "Black" : "White";

        if (gm.isGameOver() && !gameOverShown) {
            gameOverShown = true;
            String message;
            if (gm.isCheckmate(nextIsWhite)) {
                statusLabel.setText("CHECKMATE! " + winningSide + " wins!");
                statusLabel.setForeground(new Color(180, 0, 0));
                message = "GAME ENDS!\n" + winningSide + " WIN!";
            } else {
                statusLabel.setText("STALEMATE! It's a draw!");
                statusLabel.setForeground(new Color(0, 100, 180));
                message = "GAME ENDS!\nIt's a draw!";
            }
            Object[] options = {"New Game", "Close"};
            int choice = JOptionPane.showOptionDialog(this,
                    message, "Game Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);
            if (choice == 0) startNewGame();
        } else if (!gm.isGameOver()) {
            if (gm.getCheckDetector().isInCheck(nextIsWhite)) {
                statusLabel.setText(currentSide + " King is in CHECK!");
                statusLabel.setForeground(Color.RED);
            } else {
                statusLabel.setText(currentSide + "'s turn");
                statusLabel.setForeground(Color.BLACK);
            }
        }
    }

    private void startNewGame() {
        GameManager.getInstance().resetGame();
        gameOverShown = false;
        boardPanel.clearSelection();
        boardPanel.repaint();
        statusLabel.setText("White's turn");
        statusLabel.setForeground(Color.BLACK);
    }

    @Override
    public void moveMade(Piece p, Position from, Position to) {
        boardPanel.repaint();
        updateStatus();
    }
}