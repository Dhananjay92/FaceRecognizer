package com.abi.facerecognizer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.abi.facerecognizer.entity.User;
import com.abi.facerecognizer.neuralnetwork.MultiLayerPerceptron;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    /**
     * Private constructor to aboid object creation from outside classes.
     *
     * @param context
     */
    private DatabaseAccess(Context context) {
        this.openHelper = new DatabaseOpenHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    public void insert(User user) {
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        database.insert("User", null, values);
    }

    public User getUser(int id) {
        User user = null;
        Cursor cursor = database.rawQuery("SELECT * FROM User WHERE id = " + id, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setUsername(cursor.getString(1));
        }
        cursor.close();
        return user;
    }

    public User getUser(String username) {
        User user = null;
        Cursor cursor = database.rawQuery("SELECT * FROM User WHERE username = ?", new String[]{username});
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setUsername(username);
        }
        cursor.close();
        return user;
    }

    public void insert(MultiLayerPerceptron perceptron) throws IOException {

        ContentValues values = new ContentValues();
        values.put("id", 0);
        values.put("network", serialize(perceptron));
        database.insertWithOnConflict("NeuralNetwork", null, values, CONFLICT_REPLACE);
    }

    public MultiLayerPerceptron getPerceptron() throws IOException, ClassNotFoundException {
        MultiLayerPerceptron perceptron = null;
        Cursor cursor = database.rawQuery("SELECT * FROM NeuralNetwork", null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            int id = cursor.getInt(0);
            Log.i("DatabaseAccess", "ID " + id);
            byte[] data = cursor.getBlob(1);
            perceptron = (MultiLayerPerceptron) deserialize(data);
        }
        cursor.close();
        return perceptron;
    }


    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        byte[] array = out.toByteArray();
        os.close();
        return array;
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        Object object = is.readObject();
        is.close();
        return object;
    }


}
