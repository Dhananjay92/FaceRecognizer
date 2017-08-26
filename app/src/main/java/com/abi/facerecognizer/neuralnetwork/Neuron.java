package com.abi.facerecognizer.neuralnetwork;

import java.io.Serializable;

public class Neuron implements Serializable {

    public double value;

    /**
     * Weight is equal to the number of outputs from the previous layer.
     */
    public double[] weights;
    public double bias;
    public double delta;

    public Neuron(int prevLayerSize) {
        weights = new double[prevLayerSize];
        bias = Math.random() / 10000000000000.0;
        delta = Math.random() / 10000000000000.0;
        value = Math.random() / 10000000000000.0;

        for (int i = 0; i < weights.length; i++) {
            weights[i] = Math.random() / 10000000000000.0;
        }
    }
}
