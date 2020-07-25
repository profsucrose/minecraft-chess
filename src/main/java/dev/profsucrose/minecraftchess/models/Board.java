package dev.profsucrose.minecraftchess.models;

public class Board {

    final private int BOARD_WIDTH = 8;
    final private int BOARD_HEIGHT = 8;

    final public Piece[][] board = new Piece[BOARD_HEIGHT][BOARD_WIDTH];

    public void set(int x, int y, PieceType type) {
        board[y][x] = type != null ? new Piece(x, y, type) : null;
    }

    private void resetBoard() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                set(x, y, null);
            }
        }

        // init black
        set(0, 7, PieceType.ROOK_BLACK);
        set(1, 7, PieceType.KNIGHT_BLACK);
        set(2, 7, PieceType.BISHOP_BLACK);
        set(3, 7, PieceType.QUEEN_BLACK);
        set(4, 7, PieceType.KING_BLACK);
        set(5, 7, PieceType.BISHOP_BLACK);
        set(6, 7, PieceType.KNIGHT_BLACK);
        set(7, 7, PieceType.ROOK_BLACK);

        for (int i = 0; i < 8; i++)
            set(i, 6, PieceType.PAWN_BLACK);

        // init white
        set(0, 0, PieceType.ROOK_WHITE);
        set(1, 0, PieceType.KNIGHT_WHITE);
        set(2, 0, PieceType.BISHOP_WHITE);
        set(3, 0, PieceType.QUEEN_WHITE);
        set(4, 0, PieceType.KING_WHITE);
        set(5, 0, PieceType.BISHOP_WHITE);
        set(6, 0, PieceType.KNIGHT_WHITE);
        set(7, 0, PieceType.ROOK_WHITE);

        for (int i = 0; i < 8; i++)
            set(i, 1, PieceType.PAWN_WHITE);

    }

    public Piece get(int x, int y) {
        return board[y][x];
    }

    public static boolean outOfBounds(int x, int y) {
        return x < 0 || x >= 8 || y < 0 || y >= 8;
    }

    public boolean filled(int x, int y) { return !outOfBounds(x, y) && get(x, y) != null; }

    public void move(int x1, int y1, int x2, int y2, boolean moveEntity) {
        Piece piece = get(x1, y1);
        if (moveEntity)
            piece.moveEntity(x2, y2);
        set(x1, y1, null);
        board[y2][x2] = piece;
    }

    public Board() {
        resetBoard();
    }


}
