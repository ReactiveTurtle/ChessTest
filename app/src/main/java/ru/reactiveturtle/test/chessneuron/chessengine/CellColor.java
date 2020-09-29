package ru.reactiveturtle.test.chessneuron.chessengine;

import android.graphics.Color;

public class CellColor {
    public static final int COLOR_EMPTY_WHITE = Color.WHITE;
    public static final int COLOR_EMPTY_BLACK = Color.DKGRAY;
    public static final int COLOR_STEP = Color.YELLOW;
    public static final int COLOR_PROTECTED = Color.CYAN;
    public static final int COLOR_FIGHT = Color.MAGENTA;
    public static final int COLOR_FIGHT_AND_STEP = Color.parseColor("#ff6090");

    public static final int COLOR_ATTACK = Color.parseColor("#ff7d47");
    public static final int COLOR_KING_ATTACK = Color.RED;
    public static final int COLOR_SELECTED = Color.GREEN;

    public static boolean isStepColor(int color) {
        return color == COLOR_STEP || color == COLOR_FIGHT_AND_STEP;
    }

    public static boolean isFightColor(int color) {
        return color == COLOR_FIGHT || color == COLOR_FIGHT_AND_STEP;
    }

    public static boolean isWalkColor(int color) {
        return isStepColor(color) || color == COLOR_ATTACK;
    }
}
