package ru.reactiveturtle.test.chessneuron.neuron;

import java.util.ArrayList;
import java.util.List;

public class NeuronNetwork {
    private List<float[]> mInputs = new ArrayList<>();
    private List<float[][]> mWeights = new ArrayList<>();

    private float mLearningRate = 0.01f;

    public NeuronNetwork() {
        mInputs.add(new float[0]);
    }

    public void setInput(float[] input) {
        mInputs.set(0, input);
    }

    public void addWeights(float[][] hiddenLayerWeights) {
        if (mInputs.size() == mWeights.size() + 1) {
            mInputs.add(new float[0]);
        }
        mWeights.add(hiddenLayerWeights);
    }

    public void setLearningRate(float learningRate) {
        mLearningRate = learningRate;
    }

    public float getLearningRate() {
        return mLearningRate;
    }

    /*
        * Predict functions
    */
    public float[] predict() {
        float[] result = mInputs.get(0);
        for (int i = 0; i < mWeights.size(); i++) {
            result = getNextLayerInputs(result, mWeights.get(i));
            mInputs.set(i + 1, result);
        }
        return result;
    }

    private static float[] getNextLayerInputs(float[] input, float[][] allWeights) {
        float[] result = new float[allWeights.length];
        for (int i = 0; i < allWeights.length; i++) {
            result[i] = activate(multiply(input, allWeights[i]));
        }
        return result;
    }

    private static float activate(float x) {
        return (float) (1 / (1 + Math.exp(-x)));
    }

    private static float multiply(float[] input, float[] weights) {
        if (input.length != weights.length) {
            throw new IllegalArgumentException("The number of inputs must match the number of weights. " +
                    "Input size: " + input.length + ". Weights size: " + weights.length + ".");
        } else {
            float result = 0;
            for (int i = 0; i < input.length; i++) {
                result += input[i] * weights[i];
            }
            return result;
        }
    }

    /*
     * Back Propagation functions
     */
    public void correct(float[] expectedResult) {
        float[] errors = new float[0];
        for (int i = 0; i < mWeights.size(); i++) {
            float[] layerInputs = mInputs.get(mWeights.size() - i - 1);
            float[] layerOutputs = mInputs.get(mWeights.size() - i);
            float[][] layerWeights = mWeights.get(mWeights.size() - i - 1);
            if (i == 0) {
                errors = new float[layerWeights.length];
            }
            for (int j = 0; j < layerWeights.length; j++) {
                float[] neuronWeights = layerWeights[j];
                if (i == 0) {
                    errors[j] = expectedResult[j] - layerOutputs[j];
                }

                float weightsDelta = errors[j] * layerOutputs[j] * (1 - layerOutputs[j]);
                for (int k = 0; k < neuronWeights.length; k++) {
                    neuronWeights[k] = neuronWeights[k] + layerInputs[k] * weightsDelta * mLearningRate;
                }
            }
            if (i < mWeights.size() - 1) {
                float[] newErrors = new float[layerInputs.length];
                for (int j = 0; j < layerInputs.length; j++) {
                    for (int k = 0; k < layerWeights.length; k++) {
                        newErrors[j] += layerWeights[k][j] * errors[k];
                    }
                }
                errors = newErrors;
            }
        }
        //System.out.println("Corrected");
    }

    public List<float[][]> getWeights() {
        return mWeights;
    }
}
