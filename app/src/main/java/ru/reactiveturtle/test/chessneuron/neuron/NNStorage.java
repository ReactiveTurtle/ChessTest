package ru.reactiveturtle.test.chessneuron.neuron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public abstract class NNStorage {

    private NNStorage() {

    }

    public static float[][] loadWeights(String path, int outputsCount, int weightsCount) {
        System.out.println(path);
        File file = new File(path);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String buffer;
            float[][] weights = new float[outputsCount][weightsCount];
            for (int i = 0; i < outputsCount; i++) {
                buffer = bufferedReader.readLine();
                String[] stringWeights = buffer.split(" ", weightsCount);
                weights[i] = toFloat(stringWeights);
            }
            bufferedReader.close();
            return weights;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveWeightsFile(String path, float[][] weights) {
        File file = new File(path);
        try {
            if (file.exists() || file.createNewFile()) {
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    for (int i = 0; i < weights.length; i++) {
                        for (int j = 0; j < weights[i].length; j++) {
                            fileWriter.write(weights[i][j] + (j == weights[i].length - 1 ? "" : " "));
                        }
                        fileWriter.write("\n");
                    }
                    fileWriter.close();
                    System.out.println("Веса сохранены в " + path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createWeightsFile(File file, int outputsCount, int weightsCount) {
        try {
            if (file.exists() || (file.getParentFile().exists() || file.getParentFile().mkdirs())
                    && file.createNewFile()) {
                try {
                    FileWriter fileWriter = new FileWriter(file);
                    for (int i = 0; i < outputsCount; i++) {
                        for (int j = 0; j < weightsCount; j++) {
                            fileWriter.write((Math.random() - 0.5) * 1 + (j == weightsCount - 1 ? "" : " "));
                        }
                        fileWriter.write("\n");
                    }
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static float[] toFloat(String[] splitted) {
        float[] weights = new float[splitted.length];
        for (int i = 0; i < splitted.length; i++) {
            weights[i] = (float) Double.parseDouble(splitted[i]);
        }
        return weights;
    }

    public static float[] max(float[] array) {
        float max = 0;
        float index = 0;
        for (int j = 0; j < array.length; j++) {
            if (array[j] > max) {
                max = array[j];
                index = j;
            }
        }
        return new float[]{index, max};
    }
}
