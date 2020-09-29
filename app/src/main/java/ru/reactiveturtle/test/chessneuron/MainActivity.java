package ru.reactiveturtle.test.chessneuron;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.reactiveturtle.test.chessneuron.chessengine.ChessEngine;
import ru.reactiveturtle.test.chessneuron.chessengine.ChessboardView;
import ru.reactiveturtle.test.chessneuron.chessengine.MainContract;
import ru.reactiveturtle.test.chessneuron.neuron.NNStorage;
import ru.reactiveturtle.test.chessneuron.neuron.NeuronNetwork;

public class MainActivity extends AppCompatActivity implements MainContract.View {
    private MainContract.Presenter mPresenter;
    private ChessboardView mChessboardView;
    private Button mResetGameButton;
    private Button mStartPause;
    private TextView log, info;

    private boolean isStarted = false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mChessboardView = findViewById(R.id.chessboard);
        mStartPause = findViewById(R.id.startPause);
        log = findViewById(R.id.log);
        info = findViewById(R.id.info);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mChessboardView.setLayoutParams(new ConstraintLayout.LayoutParams(dm.widthPixels, dm.widthPixels));
        mChessboardView.setOnTouchListener((v, event) -> {
            float sectorWidth = mChessboardView.getWidth() / 8f;
            float sectorHeight = mChessboardView.getHeight() / 8f;
            int x = (int) (event.getX() / sectorWidth);
            int y = (int) (event.getY() / sectorHeight);
            mPresenter.onChessboardClicked(x, y);
            return false;
        });
        NeuronNetwork neuronNetwork1 = new NeuronNetwork();
        NeuronNetwork neuronNetwork2 = new NeuronNetwork();
        if (getExternalCacheDir() != null) {
            String dir = getExternalCacheDir().getAbsolutePath();
            System.out.println(dir);
            loadWeightsFromCache(neuronNetwork1, dir + "/first/player_weight");
            loadWeightsFromCache(neuronNetwork2, dir + "/second/player_weight");
        }
        try {
            InputStream is = getAssets().open("ic_chess.png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            System.out.println(bitmap.getWidth() + ", " + bitmap.getHeight());
            mPresenter = new MainPresenter(this, new ChessEngine(mChessboardView.getColors(), bitmap),
                    neuronNetwork1, neuronNetwork2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mResetGameButton = findViewById(R.id.reset_game_button);
        mResetGameButton.setOnClickListener(v -> {
            mPresenter.onResetGame();
        });

        mStartPause.setOnClickListener(view -> {
            if (isStarted) {
                mPresenter.pauseGame();
                mStartPause.setText("Продолжить");
            } else {
                mPresenter.startGame();
                mStartPause.setText("Остановить");
            }
            isStarted = !isStarted;
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    showLog();
                    updateChessboard();
                });
            }
        }, 0, 50);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    String dir = getExternalCacheDir().getAbsolutePath();
                    mPresenter.saveWeights(dir);
                });
            }
        }, 0, 60000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onView(this);
    }

    @Override
    protected void onStop() {
        if (getExternalCacheDir() != null) {
            String dir = getExternalCacheDir().getAbsolutePath();
            mPresenter.onStop(dir);
        }
        super.onStop();
    }

    @Override
    public void updateChessboard() {
        mChessboardView.invalidate();
    }

    @Override
    public void setCellPicture(Bitmap bitmap, int x, int y) {
        mChessboardView.setCellPicture(bitmap, x, y);
    }

    List<String> logs = new ArrayList<>();

    @Override
    public void addState(String text) {
        if (logs.size() >= 12) {
            logs.remove(0);
        }
        logs.add(text);
    }

    private String predictString = "";

    @Override
    public void showPredict(String predict, float[] floats) {
        predictString = predict;
        for (int i = 0; i < floats.length / 2; i++) {
            float number = floats[i];
            predictString += "\n" + number;
        }
        predictString += "\n";
        for (int i = 6; i < floats.length; i++) {
            float number = floats[i];
            predictString += "\n" + number;
        }
    }

    private int steps = 0;
    @Override
    public void addStep() {
        steps++;
    }

    @Override
    public void resetSteps() {
        steps = 0;
    }

    private void showLog() {
        StringBuilder builder = new StringBuilder();
        builder.append(predictString).append("\n");
        for (String string : logs) {
            builder.append(string).append("\n");
        }
        log.setText(builder.toString());
        info.setText("Количество ходов: " + steps);
    }

    private void loadWeightsFromCache(NeuronNetwork neuronNetwork, String filePath) {
        for (int i = 0; i < 5; i++) {
            System.out.println("created1" + i);
            File layerWeightsFile = new File(filePath + "_" + i);
            if (!layerWeightsFile.exists()) {
                switch (i) {
                    case 0:
                        NNStorage.createWeightsFile(layerWeightsFile, 256, 640);
                        break;
                    case 1:
                        NNStorage.createWeightsFile(layerWeightsFile, 1024, 256);
                        break;
                    case 2:
                        NNStorage.createWeightsFile(layerWeightsFile, 64, 1024);
                        break;
                    case 3:
                        NNStorage.createWeightsFile(layerWeightsFile, 32, 64);
                        break;
                    case 4:
                        NNStorage.createWeightsFile(layerWeightsFile, 4, 32);
                        break;
                }
            }
            if (layerWeightsFile.exists()) {
                switch (i) {
                    case 0:
                        neuronNetwork.addWeights(NNStorage.loadWeights(layerWeightsFile.getAbsolutePath(), 256, 640));
                        break;
                    case 1:
                        neuronNetwork.addWeights(NNStorage.loadWeights(layerWeightsFile.getAbsolutePath(), 1024, 256));
                        break;
                    case 2:
                        neuronNetwork.addWeights(NNStorage.loadWeights(layerWeightsFile.getAbsolutePath(), 64, 1024));
                        break;
                    case 3:
                        neuronNetwork.addWeights(NNStorage.loadWeights(layerWeightsFile.getAbsolutePath(), 32, 64));
                        break;
                    case 4:
                        neuronNetwork.addWeights(NNStorage.loadWeights(layerWeightsFile.getAbsolutePath(), 4, 32));
                        break;
                }
            }
        }
    }
}
