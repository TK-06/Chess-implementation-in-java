# Chess — Java Swing Implementation

A fully-featured two-player chess game built in Java with a Swing GUI.
Current version: **1.2.2**

---

## Features

- Full chess rules: legal move filtering, check, checkmate, stalemate
- En passant
- Pawn promotion with chess.com-style overlay picker
- Undo / Redo
- New Game button on game over
- Console move log with check / checkmate / stalemate messages

---

## Project Structure

```
src/
├── board/
│   ├── Board.java          # 8×8 grid + Observer subject
│   └── Position.java       # Immutable (row, col) value object
├── factory/
│   └── PieceFactory.java   # Factory Pattern — creates any piece by name
├── observer/
│   ├── GameObserver.java   # Observer interface
│   ├── MoveLogger.java     # Logs moves to console/history
│   └── CheckDetector.java  # Detects check after every move
├── pieces/
│   ├── Piece.java          # Abstract base class
│   ├── King.java
│   ├── Queen.java
│   ├── Rook.java
│   ├── Bishop.java
│   ├── Knight.java
│   └── Pawn.java
├── manager/
│   ├── GameManager.java    # Singleton Pattern — central game controller
│   └── MoveRecord.java     # Snapshot used by undo/redo stack
└── gui/
    ├── Main.java           # Entry point
    ├── ChessGUI.java       # JFrame + Observer — status bar, game-over popup
    └── BoardPanel.java     # JPanel — renders board, handles clicks
```

---

## Design Patterns

### 1. Factory Pattern — `PieceFactory`

`PieceFactory.create(type, row, col, isWhite)` is the single place where piece objects are constructed. Callers only need to pass a string name (`"Queen"`, `"Knight"`, etc.) and never depend on concrete subclasses directly.

**Why:** Adding a new piece type only requires creating the class and adding one `case` in the factory — no other code changes.

```
PieceFactory.create("Queen", 7, 3, true)
    → new Queen(7, 3, true)
```

---

### 2. Singleton Pattern — `GameManager`

`GameManager` holds the entire game state (board, turn, stacks, promotion state). It is constructed once and shared via `GameManager.getInstance()`. The constructor is private to prevent duplicate instances.

**Why:** Both `ChessGUI` and `BoardPanel` need the same game state. A singleton ensures they always read/write the same object without passing it around manually.

```java
// Access from anywhere in the app:
GameManager gm = GameManager.getInstance();
```

---

### 3. Observer Pattern — `Board` + `GameObserver`

`Board` is the **Subject**. After every move it calls `notifyObservers()`, which triggers all registered `GameObserver` implementations.

| Observer | What it does |
|---|---|
| `MoveLogger` | Records and prints the move to console |
| `CheckDetector` | Checks if the opponent's king is now in check |
| `ChessGUI` | Updates the status label ("White's turn", "Check!", etc.) |

**Why:** Each listener is completely independent. Adding a new feature (e.g. a sound player) only means implementing `GameObserver` and calling `board.addObserver(new SoundPlayer())` — zero changes to existing code.

---

## Diagram 1 — Design Patterns Focus

```mermaid
classDiagram
    %% ══════════════════════════════
    %%  FACTORY PATTERN
    %% ══════════════════════════════
    class PieceFactory {
        <<Factory Pattern>>
        -PieceFactory()
        +create(type,row,col,isWhite)$ Piece
    }
    class Piece {
        <<abstract>>
        +row int
        +col int
        +isWhite bool
        +hasMoved bool
        +getLegalMoves(board)* List~Position~
        +getType()* String
        +setPosition(row,col)
    }
    class King
    class Queen
    class Rook
    class Bishop
    class Knight
    class Pawn

    PieceFactory ..> King    : «creates»
    PieceFactory ..> Queen   : «creates»
    PieceFactory ..> Rook    : «creates»
    PieceFactory ..> Bishop  : «creates»
    PieceFactory ..> Knight  : «creates»
    PieceFactory ..> Pawn    : «creates»
    Piece <|-- King
    Piece <|-- Queen
    Piece <|-- Rook
    Piece <|-- Bishop
    Piece <|-- Knight
    Piece <|-- Pawn

    %% ══════════════════════════════
    %%  SINGLETON PATTERN
    %% ══════════════════════════════
    class GameManager {
        <<Singleton Pattern>>
        -instance$ GameManager
        +VERSION$ String
        -GameManager()
        +getInstance()$ GameManager
        +makeMove(from,to) bool
        +undo()
        +redo()
        +promote(pieceType)
        +resetGame()
        +isCheckmate(white) bool
        +isStalemate(white) bool
        +getLegalMovesFiltered(from) List
    }

    %% ══════════════════════════════
    %%  OBSERVER PATTERN
    %% ══════════════════════════════
    class GameObserver {
        <<interface  Observer Pattern>>
        +moveMade(p,from,to)
    }
    class Board {
        <<Subject — Observer Pattern>>
        -observers List~GameObserver~
        +addObserver(o)
        +removeObserver(o)
        +notifyObservers(p,from,to)
    }
    class MoveLogger {
        <<ConcreteObserver>>
        -movesHistory List~String~
        +moveMade(p,from,to)
        +printMoves()
        +clear()
    }
    class CheckDetector {
        <<ConcreteObserver>>
        +moveMade(p,from,to)
        +isInCheck(isWhiteKing) bool
    }
    class ChessGUI {
        <<ConcreteObserver>>
        +moveMade(p,from,to)
        +updateStatus()
    }

    GameObserver <|.. MoveLogger
    GameObserver <|.. CheckDetector
    GameObserver <|.. ChessGUI
    Board "1" o-- "0..*" GameObserver : notifies ▶
```

---

## Diagram 2 — Full UML Class Diagram

```mermaid
classDiagram
    direction TB

    %% ── board ──────────────────────────────
    class Position {
        +row int
        +col int
        +isValid() bool
        +equals(obj) bool
        +hashCode() int
    }

    class Board {
        -table Piece[][]
        -observers List~GameObserver~
        -enPassantTarget Position
        +getPiece(row,col) Piece
        +setPiece(p,row,col)
        +isEmpty(row,col) bool
        +movePiece(from,to)
        +addObserver(o)
        +removeObserver(o)
        +notifyObservers(p,from,to)
        +getEnPassantTarget() Position
        +setEnPassantTarget(p)
    }

    %% ── pieces ─────────────────────────────
    class Piece {
        <<abstract>>
        +row int
        +col int
        +isWhite bool
        +hasMoved bool
        +getLegalMoves(board)* List~Position~
        +getType()* String
        +isWhite() bool
        +getRow() int
        +getCol() int
        +setPosition(row,col)
        +setHasMoved(b)
        +isHasMoved() bool
        +isSameColor(other) bool
    }

    class King { +getLegalMoves(board) List~Position~ }
    class Queen { +getLegalMoves(board) List~Position~ }
    class Rook { +getLegalMoves(board) List~Position~ }
    class Bishop { +getLegalMoves(board) List~Position~ }
    class Knight { +getLegalMoves(board) List~Position~ }
    class Pawn { +getLegalMoves(board) List~Position~ }

    Piece <|-- King
    Piece <|-- Queen
    Piece <|-- Rook
    Piece <|-- Bishop
    Piece <|-- Knight
    Piece <|-- Pawn

    %% ── factory ────────────────────────────
    class PieceFactory {
        <<Factory>>
        -PieceFactory()
        +create(type,row,col,white)$ Piece
    }

    %% ── observer ───────────────────────────
    class GameObserver {
        <<interface>>
        +moveMade(p,from,to)
    }
    class MoveLogger {
        -movesHistory List~String~
        +moveMade(p,from,to)
        +printMoves()
        +getHistory() List~String~
        +clear()
    }
    class CheckDetector {
        -board Board
        +moveMade(p,from,to)
        +isInCheck(isWhiteKing) bool
    }

    GameObserver <|.. MoveLogger
    GameObserver <|.. CheckDetector

    %% ── manager ────────────────────────────
    class MoveRecord {
        +piece Piece
        +from Position
        +to Position
        +captured Piece
        +hadMoved bool
        +epPawn Piece
        +epRow int
        +epCol int
        +prevEnPassantTarget Position
    }

    class GameManager {
        <<Singleton>>
        -instance$ GameManager
        +VERSION$ String
        -board Board
        -isWhiteTurn bool
        -gameOver bool
        -pendingPromotion bool
        -promotionSquare Position
        -doneStack Stack~MoveRecord~
        -undoStack Stack~MoveRecord~
        -logger MoveLogger
        -checkDetector CheckDetector
        -GameManager()
        +getInstance()$ GameManager
        +makeMove(from,to) bool
        +promote(pieceType)
        +undo()
        +redo()
        +resetGame()
        +getLegalMovesFiltered(from) List
        +isCheckmate(white) bool
        +isStalemate(white) bool
        +isGameOver() bool
        +isWhiteTurn() bool
    }

    %% ── gui ─────────────────────────────────
    class ChessGUI {
        -boardPanel BoardPanel
        -statusLabel JLabel
        -gameOverShown bool
        +moveMade(p,from,to)
        +updateStatus()
        +startNewGame()
    }
    class BoardPanel {
        -board Board
        -gm GameManager
        -selected Position
        -highlights List~Position~
        +handleClick(row,col)
        +clearSelection()
        +paintComponent(g)
        +drawPromotionOverlay(g)
        +setOnPromotionComplete(r)
    }
    class Main {
        +main(args)$
    }

    GameObserver <|.. ChessGUI

    %% ── relationships ───────────────────────
    Board "1" *-- "0..32" Piece         : contains
    Board "1" o-- "0..*" GameObserver   : notifies

    GameManager "1" *-- "1" Board
    GameManager "1" *-- "1" MoveLogger
    GameManager "1" *-- "1" CheckDetector
    GameManager "1" *-- "0..*" MoveRecord
    GameManager ..> PieceFactory        : «uses»

    MoveRecord --> Piece
    MoveRecord --> Position
    CheckDetector --> Board             : reads

    ChessGUI "1" *-- "1" BoardPanel
    BoardPanel --> GameManager          : «uses»
    BoardPanel --> Board                : reads

    Main ..> GameManager                : «instantiates»
    Main ..> ChessGUI                   : «instantiates»
```

---

## How to Run

1. Clone the repo
2. Open in Eclipse (or any Java IDE)
3. Run `src/gui/Main.java`

**Requirements:** Java 17+, no external dependencies

---

## Console Output Reference

| Event | Console |
|---|---|
| Launch | `VERSION: 1.2.2` |
| New game | `VERSION: 1.2.2 — New game started` |
| Each move | `White Pawn moved from (1,4) to (3,4)` |
| Check | `Black King is in CHECK!` |
| Promotion | `Pawn promoted to Queen (7,3)` |
| Checkmate | `CHECKMATE! White wins!` |
| Stalemate | `STALEMATE! It's a draw.` |
