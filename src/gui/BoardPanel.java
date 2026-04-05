// BoardPanel.java
package gui;

import board.Board;
import board.Position;
import manager.GameManager;
import pieces.Piece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class BoardPanel extends JPanel {

    private static final int TILE_SIZE = 80;
    private Board board;
    private GameManager gm;
    private Position selected = null;
    private ArrayList<Position> highlights = new ArrayList<>();

    public BoardPanel() {
        gm    = GameManager.getInstance();
        board = gm.getBoard();
        setPreferredSize(new Dimension(8 * TILE_SIZE, 8 * TILE_SIZE));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / TILE_SIZE;
                int row = 7 - (e.getY() / TILE_SIZE);  // flip Y
                handleClick(row, col);
            }
        });
    }

    private void handleClick(int row, int col) {
        if (selected == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == gm.isWhiteTurn()) {
                selected = new Position(row, col);
                highlights = (ArrayList<Position>) p.getLegalMoves(board);  // get legal moves
            }
        } else {
            gm.makeMove(selected, new Position(row, col));
            selected = null;
            highlights.clear();  // clear highlights after move
            repaint();
        }
        repaint();  // repaint on first click too so highlights show
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawHighlights(g);  // ← after board, before pieces
        drawSelected(g);
        drawPieces(g);
    }

    private void drawBoard(Graphics g) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 0) g.setColor(new Color(240, 217, 181));
                else                   g.setColor(new Color(181, 136, 99));
                int drawY = (7 - r) * TILE_SIZE;
                g.fillRect(c * TILE_SIZE, drawY, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawPieces(Graphics g) {
        g.setFont(new Font("Serif", Font.PLAIN, 52));
        FontMetrics fm = g.getFontMetrics();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null) {
                    String symbol = getSymbol(p);
                    int drawY = (7 - r) * TILE_SIZE;
                    int x = c * TILE_SIZE + (TILE_SIZE - fm.stringWidth(symbol)) / 2;
                    int y = drawY + (TILE_SIZE - fm.getHeight()) / 2 + fm.getAscent();
                    g.setColor(Color.BLACK);
                    g.drawString(symbol, x, y);
                }
            }
        }
    }

    private void drawSelected(Graphics g) {
        if (selected == null) return;
        g.setColor(new Color(0, 255, 0, 80));
        int drawY = (7 - selected.row) * TILE_SIZE;
        g.fillRect(selected.col * TILE_SIZE, drawY, TILE_SIZE, TILE_SIZE);
    }

    private String getSymbol(Piece p) {
        boolean w = p.isWhite();
        switch (p.getType()) {
            case "King":   return w ? "♔" : "♚";
            case "Queen":  return w ? "♕" : "♛";
            case "Rook":   return w ? "♖" : "♜";
            case "Bishop": return w ? "♗" : "♝";
            case "Knight": return w ? "♘" : "♞";
            case "Pawn":   return w ? "♙" : "♟";
            default:       return "?";
        }
    }
    
    private void drawHighlights(Graphics g) {
        g.setColor(new Color(0, 255, 0, 80));  // transparent green
        for (Position p : highlights) {
            int drawY = (7 - p.row) * TILE_SIZE;
            g.fillRect(p.col * TILE_SIZE, drawY, TILE_SIZE, TILE_SIZE);
        }
    }
}