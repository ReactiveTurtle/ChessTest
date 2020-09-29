package ru.reactiveturtle.test.chessneuron;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.reactiveturtle.test.chessneuron.chessengine.CellColor;
import ru.reactiveturtle.test.chessneuron.chessengine.ChessEngine;
import ru.reactiveturtle.test.chessneuron.chessengine.Figure;
import ru.reactiveturtle.test.chessneuron.chessengine.MainContract;
import ru.reactiveturtle.test.chessneuron.neuron.NNStorage;
import ru.reactiveturtle.test.chessneuron.neuron.NeuronNetwork;

public class MainPresenter implements MainContract.Presenter {
    private MainContract.View mView;
    private ChessEngine mEngine;
    private NeuronNetwork mNN1, mNN2;

    public MainPresenter(MainContract.View view, ChessEngine model, NeuronNetwork nn1, NeuronNetwork nn2) {
        mView = view;
        mEngine = model;
        mNN1 = nn1;
        mNN2 = nn2;
        updateChessboard();
    }

    private float[] getInput() {
        float[] input = new float[64 * 10];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int index = (i * 8 + j) * 10;
                Figure figure = mEngine.getFigure(j, i);
                input[index] = figure.isWhite ? 1 : 0;
                input[index + 1] = figure.equals(mEngine.getSelectedFigure()) ? 1 : 0;
                input[index + 2] = figure.isFirstStep ? 1 : 0;
                input[index + 3] = figure.type == Figure.EMPTY ? 1 : 0;
                input[index + 4] = figure.type == Figure.KING ? 1 : 0;
                input[index + 5] = figure.type == Figure.QUEEN ? 1 : 0;
                input[index + 6] = figure.type == Figure.BISHOP ? 1 : 0;
                input[index + 7] = figure.type == Figure.HORSE ? 1 : 0;
                input[index + 8] = figure.type == Figure.ROOK ? 1 : 0;
                input[index + 9] = figure.type == Figure.PAWN ? 1 : 0;
            }
        }
        return input;
    }


    private float[] getCorrectResult(boolean isWhitePlayer) {
        int[] selectedPosition = selectFigure(isWhitePlayer);
        Figure selectedFigure = mEngine.getFigure(selectedPosition[0], selectedPosition[1]);
        int[] stepPosition;

        while ((stepPosition = getStepPosition(selectedFigure,
                selectedPosition[0], selectedPosition[1])) == null) {
            selectedPosition = selectFigure(isWhitePlayer);
            selectedFigure = mEngine.getFigure(selectedPosition[0], selectedPosition[1]);
        }
        return new float[]{selectedPosition[0] / 8f, selectedPosition[1] / 8f,
                stepPosition[0] / 8f, stepPosition[1] / 8f};
    }

    private Thread mThread = null;
    private boolean isStop = false;

    @Override
    public void onResetGame() {
        mEngine.resetGame();
        mView.resetSteps();
        pauseGame();
        startGame();
    }

    private int[][] prevPositions = new int[][]{
            new int[]{0, 0},
            new int[]{0, 0}
    };

    @Override
    public void startGame() {
        mThread = new Thread() {
            @Override
            public void run() {
                while (isStop) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (mView != null && !isStop && !mEngine.isEnd()) {
                    NeuronNetwork neuronNetwork;
                    if (mEngine.isWhiteStep()) {
                        neuronNetwork = mNN1;
                    } else {
                        neuronNetwork = mNN2;
                    }
                    neuronNetwork.setInput(MainPresenter.this.getInput());
                    float[] predict = neuronNetwork.predict();
                    int[][] positions = new int[][]{new int[]{Math.round(predict[0] * 8), Math.round(predict[1] * 8)},
                            new int[]{Math.round(predict[2] * 8), Math.round(predict[3] * 8)}};
                    mView.showPredict("(" + (positions[0][0] + 1) + ", " + (positions[0][1] + 1) + ")"
                            + "(" + (positions[1][0] + 1) + ", " + (positions[1][1] + 1) + ")", predict);
                    neuronNetwork.correct(MainPresenter.this.getCorrectResult(mEngine.isWhiteStep()));
                    if (positions[0][0] > 0 && positions[0][1] > 0) {
                        onChessboardClicked(positions[0][0] - 1, positions[0][1] - 1);
                        if (positions[1][0] > 0 && positions[1][1] > 0) {
                            onChessboardClicked(positions[1][0] - 1, positions[1][1] - 1);
                        }
                    }
                    String text = "";
                    if (mEngine.isCheck(true)) {
                        if (mEngine.isMate(true)) {
                            text = "Мат белым";
                        }
                    } else if (mEngine.isCheck(false)) {
                        if (mEngine.isMate(false)) {
                            text = "Мат чёрным";
                        }
                    } else if (mEngine.isPat()) {
                        text = "Ничья";
                    }
                    if (mView != null) {
                        if (!text.equals("")) {
                            mView.addState(text);
                        }
                    }
                }
                isStop = false;
            }
        };
        mThread.start();
    }

    @Override
    public void pauseGame() {
        if (mThread != null) {
            isStop = true;
        }
    }

    @Override
    public void onChessboardClicked(int x, int y) {
        if (mEngine.click(x, y)) {
            mView.addStep();
        }
        updateChessboard();
    }

    @Override
    public void onView(MainContract.View view) {
        mView = view;
    }

    @Override
    public void onStop(String dir) {
        saveWeights(dir);
        mView = null;
    }

    @Override
    public void saveWeights(String dir) {
        saveWeights(mNN1, dir + "/first/player_weight");
        saveWeights(mNN2, dir + "/second/player_weight");
    }

    private void saveWeights(NeuronNetwork neuronNetwork, String filePath) {
        for (int i = 0; i < neuronNetwork.getWeights().size(); i++) {
            File layerWeightsFile = new File(filePath + "_" + i);
            try {
                if (!layerWeightsFile.exists() && layerWeightsFile.createNewFile()) {
                    NNStorage.saveWeightsFile(layerWeightsFile.getAbsolutePath(), neuronNetwork.getWeights().get(i));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int[] selectFigure(boolean isWhitePlayer) {
        List<Integer> positions = new ArrayList<>();
        Figure[][] figures = mEngine.getFigures();
        for (int i = 0; i < figures.length; i++) {
            for (int j = 0; j < figures[i].length; j++) {
                Figure figure = figures[i][j];
                if (figure.type == Figure.PAWN && isWhitePlayer == figure.isWhite) {
                    int[][] steps = mEngine.searchFinalMoves(figure, j, i);
                    for (int k = 0; k < steps.length; k++) {
                        for (int l = 0; l < steps[k].length; l++) {
                            if (CellColor.isWalkColor(steps[k][l])) {
                                positions.add(j);
                                positions.add(i);
                                k = steps.length;
                                break;
                            }
                        }
                    }
                }
            }
        }
        int randomPosition = (int) (Math.random() * (positions.size() / 2 - 1));
        return new int[]{positions.get(randomPosition),
                positions.get(randomPosition + 1)};
    }

    @Nullable
    private int[] getStepPosition(Figure selectedFigure, int x, int y) {
        List<Integer> positions = new ArrayList<>();
        int[][] steps = mEngine.searchFinalMoves(selectedFigure, x, y);
        for (int i = 0; i < steps.length; i++) {
            for (int j = 0; j < steps[i].length; j++) {
                if (CellColor.isWalkColor(steps[i][j])) {
                    positions.add(j);
                    positions.add(i);
                }
            }
        }
        if (positions.size() == 0) {
            return null;
        }
        int randomPosition = (int) (Math.random() * (positions.size() / 2 - 1));
        return new int[]{positions.get(randomPosition),
                positions.get(randomPosition + 1)};
    }

    @NonNull
    private String positionToBinary(@IntRange(from = 0, to = 7) int x,
                                    @IntRange(from = 0, to = 7) int y) {
        StringBuilder stringX = new StringBuilder(Integer.toBinaryString(x));
        StringBuilder stringY = new StringBuilder(Integer.toBinaryString(y));
        for (int i = 0; i < 3 - stringX.length(); i++) {
            stringX.insert(0, 0);
        }
        for (int i = 0; i < 3 - stringY.length(); i++) {
            stringY.insert(0, 0);
        }
        return stringX.toString() + stringY.toString();
    }

    @NonNull
    private int[][] predictToPositions(float[] predict) {
        int[] predictInt = new int[predict.length];
        for (int i = 0; i < predict.length; i++) {
            predictInt[i] = Math.round(predict[i]);
        }
        StringBuilder start = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            start.append(predictInt[i]);
        }
        StringBuilder end = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            end.append(predictInt[i + 6]);
        }
        return new int[][]{
                binaryToPosition(start.toString()),
                binaryToPosition(end.toString())
        };
    }

    private int[] binaryToPosition(String binary) {
        return new int[]{
                Integer.parseInt(binary.substring(0, 3), 2),
                Integer.parseInt(binary.substring(3, 6), 2)
        };
    }

    private boolean isUpdated = true;

    private void updateChessboard() {
        if (mView != null && isUpdated) {
            isUpdated = false;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    mView.setCellPicture(mEngine.getPicture(j, i), j, i);
                }
            }
            isUpdated = true;
        }
    }
}
