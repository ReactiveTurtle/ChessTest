package ru.reactiveturtle.test.chessneuron.chessengine;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.reactiveturtle.test.chessneuron.Step;

interface IBoardModel {
    void setWhiteStep(boolean whiteStep);

    boolean isWhiteStep();

    void setSelectedFigure(@Nullable Figure figure);

    @Nullable
    Figure getSelectedFigure();

    boolean isFigureSelected();

    void setSelectedX(int x);

    int getSelectedX();

    void setSelectedY(int y);

    int getSelectedY();

    void addStep(Step lastStep);

    Step getStep(int position);

    Step getLastStep();

    int getStepsCount();

    void clearStepsHistory();

    void setFigure(Figure figure, int x, int y);

    @NonNull
    Figure getFigure(int x, int y);

    @NonNull
    Figure[][] getFigures();

    void setCellColor(int color, int x, int y);

    int getCellColor(int x, int y);

    void setCellColors(@NonNull int[][] colors);

    void clearColors();

    void resetFigureCells();

    @NonNull
    Bitmap getPicture(int position);

    int[][] getDefaultBoard();
}
