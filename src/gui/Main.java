// Main.java
package gui;

import manager.GameManager;

public class Main {
    public static void main(String[] args) {
        GameManager gm = GameManager.getInstance();
        ChessGUI gui = new ChessGUI();
        gm.getBoard().addObserver(gui);
    }
}