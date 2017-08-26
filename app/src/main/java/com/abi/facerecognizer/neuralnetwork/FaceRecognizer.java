package com.abi.facerecognizer.neuralnetwork;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.abi.facerecognizer.ImageProcessor;
import com.abi.facerecognizer.db.DatabaseAccess;
import com.abi.facerecognizer.entity.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static com.abi.facerecognizer.ImageProcessor.IMAGE_SIZE;

/**
 * Created by gobinath on 8/23/17.
 */

public class FaceRecognizer {

    private static final int ITERATION = 5;
    private static final int MAX_USERS = 10;
    private static final String TAG = "FaceRecognizer";
    private DatabaseAccess databaseAccess;
    private Context context;
    private MultiLayerPerceptron perceptron;

    public void open(Context context) {
        this.context = context;
        this.databaseAccess = DatabaseAccess.getInstance(context);
        this.databaseAccess.open();
    }

    public void close() {
        this.databaseAccess.close();
    }

    public double train(String username, Bitmap bitmap) throws IOException, ClassNotFoundException {

        double error = 0.0;

        if (this.perceptron == null) {
            this.perceptron = this.openPerceptron();
        }
        User user = this.databaseAccess.getUser(username);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            this.databaseAccess.insert(user);
            user = this.databaseAccess.getUser(username);

            if (user == null) {
                throw new RuntimeException("Failed to insert new user: " + username);
            }
        }

        double[] output = new double[MAX_USERS];
        double[] inputs = ImageProcessor.convertImage(bitmap, IMAGE_SIZE, IMAGE_SIZE);

        // Learning
        for (int i = 0; i < ITERATION; i++) {
            output[user.getId()] = 1.0;
            error = perceptron.backPropagate(inputs, output);
        }

        return error;
    }

    public void save() throws IOException {
        this.write(this.context, this.perceptron);
    }

    public User find(Bitmap bitmap) throws IOException, ClassNotFoundException {

        // Face recognition
        double[] inputs = ImageProcessor.convertImage(bitmap, IMAGE_SIZE, IMAGE_SIZE);

        MultiLayerPerceptron perceptron = this.openPerceptron();
        double[] output = perceptron.execute(inputs);

        int id = 0;
        for (int i = 0; i < MAX_USERS; i++) {
            if (output[i] > output[id]) {
                id = i;
            }
        }

        Log.i(TAG, String.format("Face %d recognized with accuracy %.2f", id, output[id]));

        return this.databaseAccess.getUser(id);
    }

    private MultiLayerPerceptron openPerceptron() throws IOException, ClassNotFoundException {

        MultiLayerPerceptron perceptron = this.read(context);

        if (perceptron == null) {
            // IMAGE_SIZE x IMAGE_SIZE -> IMAGE_SIZE -> MAX_USERS
            int[] layers = new int[]{IMAGE_SIZE * IMAGE_SIZE, IMAGE_SIZE, MAX_USERS};
            perceptron = new MultiLayerPerceptron(layers, 0.6);
            this.write(context, perceptron);
        }
        return perceptron;
    }

    private void write(Context context, MultiLayerPerceptron obj) throws IOException {
        File dir = context.getExternalFilesDir(null);
        File filePath = new File(dir.getPath() + "/" + "perceptron");
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filePath));
        outputStream.writeObject(obj);
        outputStream.flush();
        outputStream.close();
    }

    private MultiLayerPerceptron read(Context context) {
        File dir = context.getExternalFilesDir(null);
        File filePath = new File(dir.getPath() + "/" + "perceptron");
        if (!filePath.exists()) {
            return null;
        }
        MultiLayerPerceptron perceptron = null;
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(filePath));
            perceptron = (MultiLayerPerceptron) inputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return perceptron;
    }
}
