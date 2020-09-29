package ru.reactiveturtle.test.chessneuron.chessengine;

public class Figure {
    public static final int EMPTY = 12;
    public static final int KING = 0;
    public static final int QUEEN = 1;
    public static final int BISHOP = 2;
    public static final int HORSE = 3;
    public static final int ROOK = 4;
    public static final int PAWN = 5;

    public int type;
    public boolean isWhite;
    public boolean isFirstStep = true;

    public Figure(int type, boolean isWhite) {
        this.type = type;
        this.isWhite = isWhite;
    }

    public static Figure getEmpty() {
        return new Figure(EMPTY, true);
    }
}
