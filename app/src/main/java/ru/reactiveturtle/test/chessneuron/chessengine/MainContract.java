package ru.reactiveturtle.test.chessneuron.chessengine;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.reactiveturtle.test.chessneuron.MainActivity;
import ru.reactiveturtle.test.chessneuron.Step;

public interface MainContract {
    interface View {
        void updateChessboard();

        void setCellPicture(Bitmap bitmap, int x, int y);

        void addState(String text);

        void showPredict(String predict, float[] floats);

        void addStep();

        void resetSteps();
    }

    interface Presenter {
        void onResetGame();

        void startGame();

        void pauseGame();

        void onChessboardClicked(int x, int y);

        void onView(View view);

        void onStop(String dir);

        void saveWeights(String dir);
    }

    interface Model {
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
}
