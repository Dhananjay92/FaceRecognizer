package com.abi.facerecognizer.neuralnetwork;

import java.io.Serializable;

public class MultiLayerPerceptron implements Serializable {

    private double learningRate = 0.6;
    private final Layer[] layers;


    public MultiLayerPerceptron(int[] layers, double learningRate) {

        this.learningRate = learningRate;

        this.layers = new Layer[layers.length];

        for (int i = 0; i < layers.length; i++) {
            if (i != 0) {
                this.layers[i] = new Layer(layers[i], layers[i - 1]);
            } else {
                this.layers[i] = new Layer(layers[i], 0);
            }
        }
    }


    public double[] execute(double[] input) {
        double new_value;

        double output[] = new double[layers[layers.length - 1].length];

        // Put input
        for (int i = 0; i < layers[0].length; i++) {
            layers[0].neurons[i].value = input[i];
        }

        // Execute - hiddens + output
        for (int k = 1; k < layers.length; k++) {
            for (int i = 0; i < layers[k].length; i++) {
                new_value = 0.0;
                for (int j = 0; j < layers[k - 1].length; j++)
                    new_value += layers[k].neurons[i].weights[j] * layers[k - 1].neurons[j].value;

                new_value += layers[k].neurons[i].bias;

                layers[k].neurons[i].value = this.sigmoid(new_value);
            }
        }


        // Get output
        for (int i = 0; i < layers[layers.length - 1].length; i++) {
            output[i] = layers[layers.length - 1].neurons[i].value;
        }

        return output;
    }


    public double backPropagate(double[] input, double[] output) {


        // Forward propagation
        double new_output[] = execute(input);

        double error;


        // Calculate the error and update the delta of the last hidden layer
        for (int i = 0; i < layers[layers.length - 1].length; i++) {
            error = output[i] - new_output[i];
            layers[layers.length - 1].neurons[i].delta = error * this.derivativeSigmoid(new_output[i]);
        }


        // For all the layers from second last hidden layer to input layer
        for (int k = layers.length - 2; k >= 0; k--) {
            // For all neurons in the layer
            for (int i = 0; i < layers[k].length; i++) {
                error = 0.0;
                for (int j = 0; j < layers[k + 1].length; j++) {
                    error += layers[k + 1].neurons[j].delta * layers[k + 1].neurons[j].weights[i];
                }

                layers[k].neurons[i].delta = error * this.derivativeSigmoid(layers[k].neurons[i].value);
            }

            // Update the next layer neurons
            for (int i = 0; i < layers[k + 1].length; i++) {
                for (int j = 0; j < layers[k].length; j++) {
                    layers[k + 1].neurons[i].weights[j] += learningRate * layers[k + 1].neurons[i].delta *
                            layers[k].neurons[j].value;
                }
                layers[k + 1].neurons[i].bias += learningRate * layers[k + 1].neurons[i].delta;
            }
        }

        // Calculate the error
        error = 0.0;
        for (int i = 0; i < output.length; i++) {
            error += Math.abs(new_output[i] - output[i]);
        }

        error = error / output.length;
        return error;
    }

    // Transfer function
    public double sigmoid(double value) {
        return 1 / (1 + Math.pow(Math.E, -value));
    }

    public double derivativeSigmoid(double value) {
        return (value - Math.pow(value, 2));
    }
}

