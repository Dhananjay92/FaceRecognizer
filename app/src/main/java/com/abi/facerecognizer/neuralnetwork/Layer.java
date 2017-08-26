package com.abi.facerecognizer.neuralnetwork;

import java.io.Serializable;

public class Layer implements Serializable {

    public final Neuron[] neurons;
    public final int length;

    public Layer(int noOfNeurons, int prev) {

        length = noOfNeurons;
        neurons = new Neuron[noOfNeurons];

        for (int j = 0; j < length; j++) {
            neurons[j] = new Neuron(prev);
        }
    }
}
