package ru.reactiveturtle.test.chessneuron.chessengine;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.reactiveturtle.test.chessneuron.Step;

public class BoardModel implements IBoardModel {

    private int[][] colors;

    private Figure[][] figures = new Figure[8][8];
    private Figure selectedFigure = null;

    private Bitmap[] bitmaps = new Bitmap[13];
    private List<Step> stepList = new ArrayList<>();
    private boolean isWhiteStep = true;

    public BoardModel(int[][] colors, Bitmap chessBitmap) {
        this.colors = colors;
        loadBitmaps(chessBitmap);
        clearColors();
        resetFigureCells();
    }

    private void loadBitmaps(Bitmap chessBitmap) {
        int sectorWidth = chessBitmap.getWidth() / 6;
        int sectorHeight = chessBitmap.getHeight() / 2;
        System.out.println(sectorWidth + ", " + sectorHeight);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 6; j++) {
                bitmaps[i * 6 + j] = Bitmap.createBitmap(chessBitmap, sectorWidth * j, sectorHeight * i, sectorWidth, sectorHeight);
            }
        }
        bitmaps[12] = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void setWhiteStep(boolean whiteStep) {
        isWhiteStep = whiteStep;
    }

    @Override
    public boolean isWhiteStep() {
        return isWhiteStep;
    }

    @Override
    public void setSelectedFigure(Figure figure) {
        selectedFigure = figure;
    }

    @Override
    public void addStep(Step lastStep) {
        stepList.add(lastStep);
    }

    @Override
    public Step getStep(int position) {
        return stepList.get(position);
    }

    @Override
    public Step getLastStep() {
        return stepList.get(getStepsCount() - 1);
    }

    @Override
    public int getStepsCount() {
        return stepList.size();
    }

    @Override
    public void clearStepsHistory() {
        stepList.clear();
    }

    @Nullable
    @Override
    public Figure getSelectedFigure() {
        return selectedFigure;
    }

    @Override
    public boolean isFigureSelected() {
        return selectedFigure != null;
    }

    private int selectedX = -1, selectedY = -1;
    @Override
    public void setSelectedX(int x) {
        this.selectedX = x;
    }

    @Override
    public int getSelectedX() {
        return selectedX;
    }

    @Override
    public void setSelectedY(int y) {
        this.selectedY = y;
    }

    @Override
    public int getSelectedY() {
        return selectedY;
    }

    @Override
    public void setFigure(@NonNull Figure figure, int x, int y) {
        figures[y][x] = figure;
    }

    @NonNull
    @Override
    public Figure getFigure(int x, int y) {
        return figures[y][x];
    }

    @NonNull
    @Override
    public Figure[][] getFigures() {
        return figures;
    }

    @Override
    public void setCellColor(int color, int x, int y) {
        if (x <= 7 && x >= 0 && y <= 7 && y >= 0) {
            colors[y][x] = color;
        }
    }

    @Override
    public int getCellColor(int x, int y) {
        return colors[y][x];
    }

    @Override
    public void setCellColors(@NonNull int[][] colors) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (colors[i][j] != Color.TRANSPARENT) {
                    setCellColor(colors[i][j], j, i);
                }
            }
        }
    }

    @Override
    public void clearColors() {
        int firstColor = ChessboardView.DEFAULT_FIRST_COLOR;
        int secondColor = ChessboardView.DEFAULT_SECOND_COLOR;
        boolean isFirst = true;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                colors[i][j] = isFirst ? firstColor : secondColor;
                isFirst = !isFirst;
            }
            isFirst = !isFirst;
        }
    }

    @Override
    public void resetFigureCells() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                figures[i][j] = Figure.getEmpty();
            }
        }
        for (int i = 0; i < 8; i++) {
            figures[1][i] = new Figure(Figure.PAWN, false);
        }
        for (int j = 0; j < 3; j++) {
            figures[0][j] = new Figure(Figure.ROOK - j, false);
        }
        for (int j = 0; j < 3; j++) {
            figures[0][7 - j] = new Figure(Figure.ROOK - j, false);
        }
        figures[0][3] = new Figure(Figure.QUEEN, false);
        figures[0][4] = new Figure(Figure.KING, false);

        for (int i = 0; i < 8; i++) {
            figures[6][i] = new Figure(Figure.PAWN, true);
        }
        for (int j = 0; j < 3; j++) {
            figures[7][j] = new Figure(Figure.ROOK - j, true);
        }
        for (int j = 0; j < 3; j++) {
            figures[7][7 - j] = new Figure(Figure.ROOK - j, true);
        }
        figures[7][3] = new Figure(Figure.QUEEN, true);
        figures[7][4] = new Figure(Figure.KING, true);
    }

    @NonNull
    @Override
    public Bitmap getPicture(int position) {
        return bitmaps[position];
    }

    @NonNull
    @Override
    public int[][] getDefaultBoard() {
        int[][] board = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = Color.TRANSPARENT;
            }
        }
        return board;
    }
}
