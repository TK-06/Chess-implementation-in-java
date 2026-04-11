# Chess Project UML and Design Patterns

This document contains:

- A full UML-style class diagram for the chess project
- A focused diagram showing how the project implements the Observer, Factory, and Singleton patterns

## Full UML Class Diagram

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

    Board "1" *-- "0..32" Piece : stores
    Board "1" o-- "0..*" GameObserver : notifies
    Board --> Position : uses

    MoveRecord --> Piece
    MoveRecord --> Position

    CheckDetector --> Board : reads
    MoveLogger --> Piece : logs

    GameManager *-- Board
    GameManager *-- MoveLogger
    GameManager *-- CheckDetector
    GameManager o-- MoveRecord
    GameManager ..> PieceFactory : creates pieces through
    GameManager --> Position : uses

    BoardPanel --> GameManager : uses
    BoardPanel --> Board : renders
    ChessGUI *-- BoardPanel

    Main ..> GameManager : gets singleton
    Main ..> ChessGUI : starts GUI
```

## Design Patterns Diagram

```mermaid
flowchart TD
    subgraph Singleton["Singleton Pattern"]
        M1["GameManager\n- private constructor\n- static instance\n- getInstance()"]
        MAIN["Main"]
        GUI["ChessGUI"]
        PANEL["BoardPanel"]
        MAIN -->|"GameManager.getInstance()"| M1
        GUI -->|"GameManager.getInstance()"| M1
        PANEL -->|"GameManager.getInstance()"| M1
    end

    subgraph Factory["Factory Pattern"]
        PF["PieceFactory.create(type, row, col, isWhite)"]
        GM["GameManager.initBoard()\nand promote()"]
        P["Piece"]
        K["King"]
        Q["Queen"]
        R["Rook"]
        B["Bishop"]
        N["Knight"]
        PA["Pawn"]
        GM --> PF
        PF --> K
        PF --> Q
        PF --> R
        PF --> B
        PF --> N
        PF --> PA
        K --> P
        Q --> P
        R --> P
        B --> P
        N --> P
        PA --> P
    end

    subgraph Observer["Observer Pattern"]
        BOARD["Board\nSubject"]
        OBS["GameObserver\nInterface"]
        LOG["MoveLogger"]
        CHECK["CheckDetector"]
        CGUI["ChessGUI"]
        BOARD -->|"notifyObservers(move)"| OBS
        LOG -.implements .-> OBS
        CHECK -.implements .-> OBS
        CGUI -.implements .-> OBS
    end

    M1 --> BOARD
    M1 --> PF
```

## How Each Pattern Is Implemented

### 1. Observer Pattern

- `Board` is the subject because it stores a list of `GameObserver` objects.
- `Board.addObserver()` and `Board.removeObserver()` manage subscribers.
- `Board.notifyObservers()` sends move updates after a move is completed.
- `MoveLogger`, `CheckDetector`, and `ChessGUI` implement `GameObserver`.
- This keeps logging, check detection, and GUI updates separate from the core board storage logic.

### 2. Factory Pattern

- `PieceFactory.create(...)` is the single construction point for chess pieces.
- `GameManager.initBoard()` uses the factory to create all starting pieces.
- `GameManager.promote(...)` also uses the factory to create the promoted piece.
- The rest of the program can ask for a `"Queen"` or `"Knight"` without directly calling constructors everywhere.

### 3. Singleton Pattern

- `GameManager` has a private constructor and a private static `instance`.
- `GameManager.getInstance()` returns the same object throughout the app.
- `Main`, `ChessGUI`, and `BoardPanel` all rely on that shared game state.
- This ensures the GUI, board interactions, turn state, and undo/redo history all stay synchronized through one central controller.

## Suggested Use

- Use the full UML diagram for class-relationship documentation or assignment submission.
- Use the design patterns diagram when you want to explain architecture at a higher level.
- If you want, these Mermaid diagrams can also be converted into HTML or image exports later.
