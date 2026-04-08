# Chess — Java Swing Implementation

A fully-featured two-player chess game built in Java with a Swing GUI.
Current version: **1.2.1**

---

## Features

- Full chess rules: legal move filtering, check, checkmate, stalemate
- En passant
- Pawn promotion with chess.com-style overlay picker
- Undo / Redo
- New Game button on game over
- Console move log with check / checkmate / stalemate messages

---

## How to Run

1. Clone the repo
2. Open in Eclipse (or any Java IDE)
3. Run `src/gui/Main.java`

**Requirements:** Java 17+, no external dependencies

---

## Project Structure

```
src/
├── board/
│   ├── Board.java          # 8×8 grid — Observer subject, holds all pieces
│   └── Position.java       # Immutable (row, col) value object
├── factory/
│   └── PieceFactory.java   # Factory Pattern — creates any piece by name
├── observer/
│   ├── GameObserver.java   # Observer interface
│   ├── MoveLogger.java     # Logs every move to console / history list
│   └── CheckDetector.java  # Scans for check after every move
├── pieces/
│   ├── Piece.java          # Abstract base — shared fields & move contract
│   ├── King.java
│   ├── Queen.java
│   ├── Rook.java
│   ├── Bishop.java
│   ├── Knight.java
│   └── Pawn.java           # Includes en passant logic
├── manager/
│   ├── GameManager.java    # Singleton Pattern — central game controller
│   └── MoveRecord.java     # Immutable snapshot for undo / redo stack
└── gui/
    ├── Main.java           # Entry point
    ├── ChessGUI.java       # JFrame + Observer — status bar & game-over popup
    └── BoardPanel.java     # JPanel — renders board, handles clicks & promotion
```

---

## Design Patterns

### 1. Factory Pattern — `PieceFactory`

`PieceFactory.create(type, row, col, isWhite)` is the **single place** where piece objects are constructed. Callers pass a string name (`"Queen"`, `"Knight"`, etc.) and never reference concrete subclasses directly.

**Why:** Adding a new piece type only requires creating the class and one new `case` in the factory — zero changes elsewhere.

```
PieceFactory.create("Queen", 7, 3, true)
    → new Queen(7, 3, true)
```

---

### 2. Singleton Pattern — `GameManager`

`GameManager` owns the entire game state (board, turn, undo/redo stacks, promotion state). It is constructed once and shared globally via `GameManager.getInstance()`. The constructor is `private` to prevent duplicate instances.

**Why:** Both `ChessGUI` and `BoardPanel` need the same live game state. A singleton guarantees they always read and write the same object without manually passing a reference.

```java
// Access from anywhere in the app — always the same instance:
GameManager gm = GameManager.getInstance();
```

---

### 3. Observer Pattern — `Board` + `GameObserver`

`Board` is the **Subject**. After every move it calls `notifyObservers()`, which triggers every registered `GameObserver` independently.

| Observer | Responsibility |
|---|---|
| `MoveLogger` | Records and prints each move to the console |
| `CheckDetector` | Checks whether the opponent's king is now in check |
| `ChessGUI` | Updates the status label ("White's turn", "CHECK!", etc.) |

**Why:** Each listener is fully independent. Adding a new feature (e.g. a sound player) only means implementing `GameObserver` and calling `board.addObserver(new SoundPlayer())` — no existing code changes.

---

## Diagram 1 — Design Patterns

```mermaid
flowchart TD
    subgraph Singleton["🔒 Singleton Pattern"]
        M1["GameManager\n— private constructor\n— static instance\n— getInstance()"]
        MAIN["Main"]
        GUI["ChessGUI"]
        PANEL["BoardPanel"]
        MAIN  -->|"GameManager.getInstance()"| M1
        GUI   -->|"GameManager.getInstance()"| M1
        PANEL -->|"GameManager.getInstance()"| M1
    end

    subgraph Factory["🏭 Factory Pattern"]
        PF["PieceFactory\n.create(type, row, col, isWhite)"]
        GM["GameManager\ninitBoard() & promote()"]
        P["Piece\n«abstract»"]
        K["King"]
        Q["Queen"]
        R["Rook"]
        B["Bishop"]
        N["Knight"]
        PA["Pawn"]
        GM --> PF
        PF --> K & Q & R & B & N & PA
        K & Q & R & B & N & PA --> P
    end

    subgraph Observer["👁️ Observer Pattern"]
        BOARD["Board\n«Subject»"]
        OBS["GameObserver\n«interface»"]
        LOG["MoveLogger"]
        CHECK["CheckDetector"]
        CGUI["ChessGUI"]
        BOARD -->|"notifyObservers(move)"| OBS
        LOG   -. implements .-> OBS
        CHECK -. implements .-> OBS
        CGUI  -. implements .-> OBS
    end

    M1    --> BOARD
    M1    --> PF
```

---

## Diagram 2 — Full UML Class Diagram

```mermaid
classDiagram
    direction TB

    class Position {
        +row : int
        +col : int
        +Position(row, col)
        +isValid() boolean
        +equals(obj) boolean
        +hashCode() int
    }

    class Board {
        -table : Piece[][]
        -observers : ArrayList~GameObserver~
        -enPassantTarget : Position
        +getEnPassantTarget() Position
        +setEnPassantTarget(p : Position) void
        +addObserver(o : GameObserver) void
        +removeObserver(o : GameObserver) void
        +notifyObservers(p : Piece, from : Position, to : Position) void
        +movePiece(from : Position, to : Position) void
        +setPiece(p : Piece, row : int, col : int) void
        +getPiece(row : int, col : int) Piece
        +isEmpty(row : int, col : int) boolean
    }

    class Piece {
        <<abstract>>
        +row : int
        +col : int
        +isWhite : boolean
        +hasMoved : boolean
        +Piece(r, c, isW)
        +getLegalMoves(board : Board) ArrayList~Position~
        +getType() String
        +isWhite() boolean
        +getRow() int
        +getCol() int
        +setPosition(row : int, col : int) void
        +isSameColor(other : Piece) boolean
        +setHasMoved(hasMoved : boolean) void
        +isHasMoved() boolean
    }

    class King
    class Queen
    class Rook
    class Bishop
    class Knight
    class Pawn

    class PieceFactory {
        -PieceFactory()
        +create(type : String, row : int, col : int, isWhite : boolean) Piece
    }

    class GameObserver {
        <<interface>>
        +moveMade(p : Piece, from : Position, to : Position) void
    }

    class MoveLogger {
        -movesHistory : ArrayList~String~
        +moveMade(p : Piece, from : Position, to : Position) void
        +printMoves() void
        +getHistory() ArrayList~String~
        +clear() void
    }

    class CheckDetector {
        -board : Board
        +CheckDetector(board : Board)
        +moveMade(p : Piece, from : Position, to : Position) void
        +isInCheck(isWhiteKing : boolean) boolean
    }

    class MoveRecord {
        +piece : Piece
        +from : Position
        +to : Position
        +captured : Piece
        +hadMoved : boolean
        +epPawn : Piece
        +epRow : int
        +epCol : int
        +prevEnPassantTarget : Position
        +MoveRecord(piece, from, to, captured, hadMoved, epPawn, epRow, epCol, prevEP)
    }

    class GameManager {
        <<singleton>>
        +VERSION : String
        -instance : GameManager
        -board : Board
        -isWhiteTurn : boolean
        -logger : MoveLogger
        -checkDetector : CheckDetector
        -doneStack : Stack~MoveRecord~
        -undoStack : Stack~MoveRecord~
        -gameOver : boolean
        -pendingPromotion : boolean
        -promotionSquare : Position
        -promotionIsWhite : boolean
        -GameManager()
        +getInstance() GameManager
        +makeMove(from : Position, to : Position) boolean
        +promote(pieceType : String) void
        +undo() void
        +redo() void
        +getLegalMovesFiltered(from : Position) List~Position~
        +isCheckmate(white : boolean) boolean
        +isStalemate(white : boolean) boolean
        +resetGame() void
        +isGameOver() boolean
        +getBoard() Board
        +isWhiteTurn() boolean
        +getLogger() MoveLogger
        +getCheckDetector() CheckDetector
    }

    class BoardPanel {
        -TILE_SIZE : int
        -board : Board
        -gm : GameManager
        -selected : Position
        -highlights : List~Position~
        -onPromotionComplete : Runnable
        +BoardPanel()
        +setOnPromotionComplete(r : Runnable) void
        +clearSelection() void
    }

    class ChessGUI {
        -boardPanel : BoardPanel
        -statusLabel : JLabel
        -gameOverShown : boolean
        +ChessGUI()
        +moveMade(p : Piece, from : Position, to : Position) void
    }

    class Main {
        +main(args : String[]) void
    }

    Piece <|-- King
    Piece <|-- Queen
    Piece <|-- Rook
    Piece <|-- Bishop
    Piece <|-- Knight
    Piece <|-- Pawn

    GameObserver <|.. MoveLogger
    GameObserver <|.. CheckDetector
    GameObserver <|.. ChessGUI

    Board "1" *-- "0..32" Piece         : stores
    Board "1" o-- "0..*" GameObserver   : notifies
    Board --> Position                   : uses

    MoveRecord --> Piece
    MoveRecord --> Position

    CheckDetector --> Board              : reads
    MoveLogger    --> Piece              : logs

    GameManager *-- Board
    GameManager *-- MoveLogger
    GameManager *-- CheckDetector
    GameManager o-- MoveRecord
    GameManager ..> PieceFactory        : creates pieces through
    GameManager --> Position            : uses

    BoardPanel --> GameManager          : uses
    BoardPanel --> Board                : renders
    ChessGUI   *-- BoardPanel

    Main ..> GameManager                : gets singleton
    Main ..> ChessGUI                   : starts GUI
```

---

## Console Output Reference

| Event | Console |
|---|---|
| Launch | `VERSION: 1.2.1` |
| New game | `VERSION: 1.2.1 — New game started` |
| Each move | `White Pawn moved from (1,4) to (3,4)` |
| Check | `Black King is in CHECK!` |
| Promotion | `Pawn promoted to Queen (7,3)` |
| Checkmate | `CHECKMATE! White wins!` |
| Stalemate | `STALEMATE! It's a draw.` |
