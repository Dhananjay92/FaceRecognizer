package com.abi.facerecognizer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;

import java.nio.ByteBuffer;

public class ImageProcessor {

    private static final String TAG = "ImageProcessor";
    public static final int IMAGE_SIZE = 128;

    private ImageProcessor() {

    }

    public static Bitmap process(Bitmap bitmap) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        bitmap = ImageProcessor.convertToRGB_565(bitmap);

        FaceDetector.Face face = ImageProcessor.detectFace(bitmap, width, height);

        if (face != null) {

            float confidence = face.confidence();
            Log.i(TAG, "Detected a face with a confidence " + confidence);

            float eyeDistance = face.eyesDistance();
            PointF midPoint = new PointF();
            face.getMidPoint(midPoint);


            int x = (int) (midPoint.x - (eyeDistance));
            x = x < 0 ? 0 : x;

            int y = (int) (midPoint.y - (eyeDistance / 2));
            y = y < 0 ? 0 : y;

            int newWidth = (int) (eyeDistance * 2);
            newWidth = x + newWidth > width ? width - x : newWidth;

            int newHeight = (int) (eyeDistance * 1.5);
            newHeight = y + newHeight > height ? height - y : newHeight;

            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, newWidth, newHeight);
            return ImageProcessor.convertToBW(croppedBitmap);
        }
        return null;
    }

    public static double[] convertImage(Bitmap bitmap, int sizeX, int sizeY) {

        int bytes = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);
        byte[] pixels = buffer.array();
        int size = pixels.length;
        double[] data = new double[size];

        for (int i = 0; i < size; i++) {
            if (pixels[0] > 128) {
                data[i] = 0.0;
            } else {
                data[i] = 1.0;
            }
        }

        return data;
    }

    private static FaceDetector.Face detectFace(Bitmap bitmap, int width, int height) {

        FaceDetector.Face[] face = new FaceDetector.Face[1];
        FaceDetector faceDetector = new FaceDetector(width, height, 1);
        int noOfFaces = faceDetector.findFaces(bitmap, face);

        if (noOfFaces == 1) {
            return face[0];
        } else {
            return null;
        }
    }

    private static Bitmap convertToRGB_565(Bitmap bitmap) {
        Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(convertedBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return convertedBitmap;
    }

    private static Bitmap convertToBW(Bitmap bitmap) {
        int width, height;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        Bitmap grayScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayScale);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return grayScale;
    }

}
