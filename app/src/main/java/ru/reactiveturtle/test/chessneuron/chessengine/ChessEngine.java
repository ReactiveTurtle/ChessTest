package ru.reactiveturtle.test.chessneuron.chessengine;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class ChessEngine {
    private IBoardModel mModel;

    public ChessEngine(int[][] colors, Bitmap bitmap) {
        mModel = new BoardModel(colors, bitmap);
    }

    public boolean click(int x, int y) {
        mModel.clearColors();
        Figure figure = mModel.getFigure(x, y);

        boolean isFigureWalk = false;
        boolean isNewCellSelected = mModel.getSelectedFigure() != null
                && !figure.equals(mModel.getSelectedFigure());
        boolean isUnselectCell = mModel.getSelectedFigure() != null
                && figure.equals(mModel.getSelectedFigure());

        if (isNewCellSelected && mModel.getSelectedFigure() != null
                && mModel.getSelectedFigure().type != Figure.EMPTY
                && mModel.getSelectedFigure().isWhite == mModel.isWhiteStep()) {
            int[][] moves = searchFinalMoves(
                    Objects.requireNonNull(mModel.getSelectedFigure()),
                    mModel.getSelectedX(), mModel.getSelectedY());
            if (CellColor.isWalkColor(moves[y][x])) {
                isFigureWalk = true;
            }
        }

        if (isFigureWalk) {
            mModel.setWhiteStep(!mModel.isWhiteStep());

            mModel.setFigure(mModel.getSelectedFigure(), x, y);
            mModel.setFigure(Figure.getEmpty(), mModel.getSelectedX(), mModel.getSelectedY());

            if (mModel.getSelectedFigure().type == Figure.PAWN) {
                mModel.getSelectedFigure().isFirstStep = false;
            }
            mModel.setSelectedFigure(null);
            mModel.setSelectedX(-1);
            mModel.setSelectedY(-1);
        } else {
            mModel.setSelectedFigure(isUnselectCell ? null : figure);
            Figure selectedFigure = mModel.getSelectedFigure();

            boolean isFigureCanWalk = mModel.isFigureSelected()
                    && mModel.isWhiteStep() == selectedFigure.isWhite;

            if (mModel.isFigureSelected()) {
                mModel.setCellColor(CellColor.COLOR_SELECTED, x, y);
                mModel.setSelectedX(x);
                mModel.setSelectedY(y);
            } else {
                mModel.clearColors();
            }
            if (isFigureCanWalk) {
                int[][] moves = searchFinalMoves(selectedFigure, x, y);
                mModel.setCellColors(moves);
            }
        }
        return isFigureWalk;
    }

    public int[][] searchFinalMoves(Figure figure, int x, int y) {
        int[][] steps = searchValidMoves(figure, x, y, true);
        if (figure.type != Figure.KING) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (CellColor.isWalkColor(steps[i][j])) {
                        Figure buffer = mModel.getFigure(j, i);
                        mModel.setFigure(figure, j, i);
                        mModel.setFigure(Figure.getEmpty(), x, y);
                        if (isCheck(figure.isWhite)) {
                            steps[i][j] = Color.TRANSPARENT;
                        }
                        mModel.setFigure(figure, x, y);
                        mModel.setFigure(buffer, j, i);
                    }
                }
            }
        }
        return steps;
    }

    private int[][] searchValidMoves(Figure figure, int x, int y, boolean isBreak) {
        int[][] steps = mModel.getDefaultBoard();
        switch (figure.type) {
            case Figure.PAWN:
                searchPawnSteps(figure, x, y, steps);
                break;
            case Figure.ROOK:
                searchRookSteps(figure, x, y, steps, isBreak);
                break;
            case Figure.HORSE:
                searchHorseSteps(figure, x, y, steps);
                break;
            case Figure.BISHOP:
                searchBishopSteps(figure, x, y, steps, isBreak);
                break;
            case Figure.QUEEN:
                searchRookSteps(figure, x, y, steps, isBreak);
                searchBishopSteps(figure, x, y, steps, isBreak);
                break;
            case Figure.KING:
                searchKingSteps(figure, x, y, steps);
                break;
        }
        return steps;
    }

    private void searchKingSteps(Figure figure, int x, int y, int[][] steps) {
        int[] bias = new int[]{-1, 0};
        for (int i = 0; i < 4; i++) {
            int sx, sy;
            if (isBoard(sx = x + bias[0], sy = y + bias[1])) {
                steps[sy][sx] = getValidKingStep(
                        calcCellColor(figure.isWhite, sx, sy),
                        figure.isWhite, sx, sy);
            }
            bias[1] *= -1;
            int buffer = bias[1];
            bias[1] = bias[0];
            bias[0] = buffer;
        }

        bias = new int[]{1, 1};
        for (int i = 0; i < 4; i++) {
            int sx, sy;
            if (isBoard(sx = x + bias[0], sy = y + bias[1])) {
                steps[sy][sx] = getValidKingStep(
                        calcCellColor(figure.isWhite, sx, sy),
                        figure.isWhite, sx, sy);
            }
            bias[1] *= -1;
            int buffer = bias[1];
            bias[1] = bias[0];
            bias[0] = buffer;
        }
    }

    private int getValidKingStep(int color, boolean isWhite, int x, int y) {
        if (color == CellColor.COLOR_ATTACK) {
            if (isFigureProtected(mModel.getFigure(x, y), x, y)) {
                return Color.TRANSPARENT;
            }
        } else if (color == CellColor.COLOR_FIGHT_AND_STEP) {
            if (isCellFight(isWhite, x, y)) {
                return Color.TRANSPARENT;
            }
        }
        return color;
    }

    private boolean isFigureProtected(Figure figure, int x, int y) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Figure protector = mModel.getFigure(j, i);
                if (figure.isWhite == protector.isWhite
                        && !protector.equals(figure)) {
                    int[][] moves = searchValidMoves(protector, j, i, false);
                    if (moves[y][x] == CellColor.COLOR_PROTECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isCellFight(boolean isWhite, int x, int y) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Figure fighter = mModel.getFigure(j, i);
                if (isWhite != fighter.isWhite) {
                    int[][] moves;
                    if (fighter.type != Figure.KING) {
                        moves = searchValidMoves(fighter, j, i, false);
                    } else {
                        moves = getBaseKingSteps(fighter, j, i);
                    }
                    if (CellColor.isFightColor(moves[y][x])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int[][] getBaseKingSteps(Figure king, int x, int y) {
        int[][] steps = mModel.getDefaultBoard();
        int[] bias = new int[]{-1, 0};
        for (int i = 0; i < 4; i++) {
            int sx, sy;
            if (isBoard(sx = x + bias[0], sy = y + bias[1])) {
                steps[sy][sx] = calcCellColor(king.isWhite, sx, sy);
            }
            bias[1] *= -1;
            int buffer = bias[1];
            bias[1] = bias[0];
            bias[0] = buffer;
        }

        bias = new int[]{1, 1};
        for (int i = 0; i < 4; i++) {
            int sx, sy;
            if (isBoard(sx = x + bias[0], sy = y + bias[1])) {
                steps[sy][sx] = calcCellColor(king.isWhite, sx, sy);
            }
            bias[1] *= -1;
            int buffer = bias[1];
            bias[1] = bias[0];
            bias[0] = buffer;
        }
        return steps;
    }

    private void searchPawnSteps(Figure figure, int x, int y, int[][] steps) {
        int yFactor = figure.isWhite ? 1 : -1;

        int[] fightBiases = new int[]{-1, 1};
        for (int i = 0; i < (figure.isFirstStep ? 2 : 1); i++) {
            int sy;
            if (isBoard(x, sy = y - (i + 1) * yFactor)) {
                if (mModel.getFigure(x, sy).type == Figure.EMPTY) {
                    steps[sy][x] = CellColor.COLOR_STEP;
                } else {
                    break;
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            int sx, sy;
            if (isBoard(sx = x + fightBiases[i], sy = y - yFactor)) {
                if (mModel.getFigure(sx, sy).type == Figure.EMPTY) {
                    steps[sy][sx] = CellColor.COLOR_FIGHT;
                } else if (mModel.getFigure(sx, sy).isWhite != figure.isWhite) {
                    steps[sy][sx] = CellColor.COLOR_ATTACK;
                } else {
                    if (mModel.getFigure(sx, sy).type != Figure.KING) {
                        steps[sy][sx] = CellColor.COLOR_PROTECTED;
                    }
                }
            }
        }
    }

    private void searchRookSteps(Figure figure, int x, int y, int[][] steps, boolean isBreak) {
        int[] bias = new int[]{-1, 0};
        for (int i = 0; i < 4; i++) {
            int p = 1;
            int sx, sy;
            while (isBoard(sx = x + bias[0] * p, sy = y + bias[1] * p)) {
                Figure cellFigure = mModel.getFigure(sx, sy);
                int color = (steps[sy][sx] = calcCellColor(figure.isWhite, sx, sy));
                if (!CellColor.isStepColor(color) && isBreak
                        || !isBreak && cellFigure.type != Figure.KING
                        && cellFigure.type != Figure.EMPTY) {
                    break;
                }
                p++;
            }
            bias[1] *= -1;
            int buffer = bias[1];
            bias[1] = bias[0];
            bias[0] = buffer;
        }
    }

    private void searchHorseSteps(Figure figure, int x, int y, int[][] steps) {
        int[] bias = new int[]{2, -1};
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                int sx, sy;
                if (isBoard(sx = x + bias[0], sy = y + bias[1])) {
                    steps[sy][sx] = calcCellColor(figure.isWhite, sx, sy);
                }
                bias[1] *= -1;
            }
            bias[1] *= -1;
            int buffer = bias[1];
            bias[1] = bias[0];
            bias[0] = buffer;
        }
    }

    private void searchBishopSteps(Figure figure, int x, int y, int[][] steps, boolean isBreak) {
        int[] bias = new int[]{1, 1};
        for (int i = 0; i < 4; i++) {
            int p = 1;
            int sx, sy;
            while (isBoard(sx = x + bias[0] * p, sy = y + bias[1] * p)) {
                Figure cellFigure = mModel.getFigure(sx, sy);
                int color = (steps[sy][sx] = calcCellColor(figure.isWhite, sx, sy));
                if (!CellColor.isStepColor(color) && isBreak
                        || !isBreak && cellFigure.type != Figure.KING
                        && cellFigure.type != Figure.EMPTY) {
                    break;
                }
                p++;
            }
            bias[1] *= -1;
            int buffer = bias[1];
            bias[1] = bias[0];
            bias[0] = buffer;
        }
    }

    private boolean isBoard(int x, int y) {
        return x < 8 && x > -1 && y < 8 && y > -1;
    }

    public boolean isCheck(boolean isWhite) {
        Figure[][] figures = mModel.getFigures();
        for (int i = 0; i < figures.length; i++) {
            for (int j = 0; j < figures[i].length; j++) {
                Figure figure = figures[i][j];
                if (figure.isWhite != isWhite
                        && figure.type != Figure.KING) {
                    int[][] steps = searchValidMoves(figure, j, i, true);
                    for (int k = 0; k < steps.length; k++) {
                        for (int l = 0; l < steps[k].length; l++) {
                            if (mModel.getFigure(l, k).type == Figure.KING
                                    && steps[k][l] == CellColor.COLOR_ATTACK) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isPat() {
        return isPat(true) || isPat(false);
    }

    private boolean isPat(boolean isWhite) {
        if (!isCheck(isWhite)) {
            Figure[][] figures = mModel.getFigures();
            Figure king = null;
            int x = 0, y = 0;
            for (int i = 0; i < figures.length; i++) {
                for (int j = 0; j < figures[i].length; j++) {
                    Figure figure = figures[i][j];
                    if (figure.isWhite == isWhite && figure.type == Figure.KING) {
                        king = figure;
                        x = j;
                        y = i;
                        break;
                    }
                }
            }
            Objects.requireNonNull(king);
            int[][] steps = searchValidMoves(king, x, y, true);
            for (int k = 0; k < steps.length; k++) {
                for (int l = 0; l < steps[k].length; l++) {
                    if (CellColor.isWalkColor(steps[k][l])) {
                        return false;
                    }
                }
            }
            for (int i = 0; i < figures.length; i++) {
                for (int j = 0; j < figures[i].length; j++) {
                    Figure figure = figures[i][j];
                    if (figure.isWhite == isWhite) {
                        if (figure.type != Figure.KING) {
                            steps = searchValidMoves(figure, j, i, true);
                            for (int k = 0; k < 8; k++) {
                                for (int l = 0; l < 8; l++) {
                                    if (CellColor.isWalkColor(steps[k][l])) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isMate(boolean isWhite) {
        if (isCheck(isWhite)) {
            Figure[][] figures = mModel.getFigures();
            for (int i = 0; i < figures.length; i++) {
                for (int j = 0; j < figures[i].length; j++) {
                    Figure figure = figures[i][j];
                    if (figure.isWhite == isWhite) {
                        int[][] steps = searchValidMoves(figure, j, i, true);
                        if (figure.type == Figure.KING) {
                            for (int k = 0; k < steps.length; k++) {
                                for (int l = 0; l < steps[k].length; l++) {
                                    if (CellColor.isWalkColor(steps[k][l])) {
                                        return false;
                                    }
                                }
                            }
                        } else {
                            for (int k = 0; k < 8; k++) {
                                for (int l = 0; l < 8; l++) {
                                    if (CellColor.isWalkColor(steps[k][l])) {
                                        Figure buffer = mModel.getFigure(l, k);
                                        mModel.setFigure(figure, l, k);
                                        mModel.setFigure(Figure.getEmpty(), j, i);
                                        boolean isCheck = isCheck(figure.isWhite);
                                        mModel.setFigure(figure, j, i);
                                        mModel.setFigure(buffer, l, k);
                                        if (!isCheck) {
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isEnd() {
        if (isCheck(true)) {
            if (isMate(true)) {
                return true;
            } else {
                return false;
            }
        } else if (isCheck(false)) {
            if (isMate(false)) {
                return true;
            } else {
                return false;
            }
        } else return isPat();
    }

    @ColorInt
    private int calcCellColor(boolean isWhite, int x, int y) {
        if (mModel.getFigure(x, y).type == Figure.EMPTY) {
            return CellColor.COLOR_FIGHT_AND_STEP;
        } else if (mModel.getFigure(x, y).isWhite != isWhite) {
            return CellColor.COLOR_ATTACK;
        } else {
            if (mModel.getFigure(x, y).type != Figure.KING) {
                return CellColor.COLOR_PROTECTED;
            }
        }
        return Color.TRANSPARENT;
    }

    public Bitmap getPicture(int x, int y) {
        Figure[][] figures = mModel.getFigures();
        if (figures[y][x] != null) {
            if (figures[y][x].isWhite) {
                return mModel.getPicture(figures[y][x].type);
            } else {
                return mModel.getPicture(figures[y][x].type + 6);
            }
        } else {
            return mModel.getPicture(12);
        }
    }

    public void resetGame() {
        mModel.setWhiteStep(true);
        mModel.clearColors();
        mModel.resetFigureCells();
    }

    public boolean isWhiteStep() {
        return mModel.isWhiteStep();
    }

    @Nullable
    public Figure getSelectedFigure() {
        return mModel.getSelectedFigure();
    }

    public int getSelectedX() {
        return mModel.getSelectedX();
    }

    public int getSelectedY() {
        return mModel.getSelectedY();
    }

    @NonNull
    public Figure getFigure(int x, int y) {
        return mModel.getFigure(x, y);
    }

    @NonNull
    public Figure[][] getFigures() {
        return mModel.getFigures();
    }
}