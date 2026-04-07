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
import java.util.List;
import java.awt.RenderingHints;

public class BoardPanel extends JPanel {

    private static final int TILE_SIZE = 80;
    private Board board;
    private GameManager gm;
    private Position selected = null;
    private List<Position> highlights = new ArrayList<>();

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
        if (row < 0 || row > 7 || col < 0 || col > 7) return;

        // While a pawn is awaiting promotion, only the picker is active
        if (gm.isPendingPromotion()) {
            handlePromotionClick(row, col);
            return;
        }

        if (selected == null) {
            Piece p = board.getPiece(row, col);
            if (p != null && p.isWhite() == gm.isWhiteTurn()) {
                selected = new Position(row, col);
                highlights = gm.getLegalMovesFiltered(selected);  // only truly legal moves
            }
        } else {
            gm.makeMove(selected, new Position(row, col));
            selected = null;
            highlights.clear();  // clear highlights after move
            repaint();
        }
        repaint();  // repaint on first click too so highlights show
    }

    /** Order of pieces shown in the promotion picker (matches chess.com). */
    private static final String[] PROMO_CHOICES = {"Queen", "Knight", "Rook", "Bishop"};

    /** Optional callback invoked after promotion completes (used to refresh status label). */
    private Runnable onPromotionComplete;
    public void setOnPromotionComplete(Runnable r) { onPromotionComplete = r; }

    private void handlePromotionClick(int row, int col) {
        Position ps = gm.getPromotionSquare();
        if (col != ps.col) return;  // click must be in the promotion column

        boolean isWhite  = gm.isPromotionWhite();
        int     startRow = isWhite ? 7 : 0;
        int     dir      = isWhite ? -1 : 1;  // white: go down visually; black: go up

        for (int i = 0; i < PROMO_CHOICES.length; i++) {
            if (row == startRow + i * dir) {
                gm.promote(PROMO_CHOICES[i]);
                repaint();
                if (onPromotionComplete != null) onPromotionComplete.run();
                return;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawHighlights(g);
        drawSelected(g);
        drawPieces(g);
        if (gm.isPendingPromotion()) drawPromotionOverlay(g);
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
    
    public void clearSelection() {
        selected = null;
        highlights.clear();
    }

    /** Draws a chess.com-style vertical picker at the promotion column. */
    private void drawPromotionOverlay(Graphics g) {
        Position ps      = gm.getPromotionSquare();
        boolean isWhite  = gm.isPromotionWhite();
        int     startRow = isWhite ? 7 : 0;
        int     dir      = isWhite ? -1 : 1;

        // Dim the rest of the board
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setFont(new Font("Serif", Font.PLAIN, 52));
        FontMetrics fm = g.getFontMetrics();

        for (int i = 0; i < PROMO_CHOICES.length; i++) {
            int r     = startRow + i * dir;
            int drawX = ps.col * TILE_SIZE;
            int drawY = (7 - r) * TILE_SIZE;

            // Tile background — white pieces get light tile, black get dark
            Color bg     = isWhite ? new Color(240, 217, 181) : new Color(58, 71, 93);
            Color border = new Color(80, 80, 80, 180);
            g.setColor(bg);
            g2.fillRoundRect(drawX + 3, drawY + 3, TILE_SIZE - 6, TILE_SIZE - 6, 12, 12);
            g.setColor(border);
            g2.drawRoundRect(drawX + 3, drawY + 3, TILE_SIZE - 6, TILE_SIZE - 6, 12, 12);

            // Piece symbol
            String sym = getSymbolForType(PROMO_CHOICES[i], isWhite);
            int x = drawX + (TILE_SIZE - fm.stringWidth(sym)) / 2;
            int y = drawY + (TILE_SIZE - fm.getHeight()) / 2 + fm.getAscent();
            g.setColor(isWhite ? Color.BLACK : Color.WHITE);
            g.drawString(sym, x, y);
        }
    }

    private String getSymbolForType(String type, boolean isWhite) {
        switch (type) {
            case "Queen":  return isWhite ? "♕" : "♛";
            case "Rook":   return isWhite ? "♖" : "♜";
            case "Bishop": return isWhite ? "♗" : "♝";
            case "Knight": return isWhite ? "♘" : "♞";
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