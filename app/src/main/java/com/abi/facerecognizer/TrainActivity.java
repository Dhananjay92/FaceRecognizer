package com.abi.facerecognizer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abi.facerecognizer.entity.User;
import com.abi.facerecognizer.neuralnetwork.FaceRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.abi.facerecognizer.ImageProcessor.IMAGE_SIZE;

public class TrainActivity extends AppCompatActivity {

    private static final String TAG = "TrainActivity";
    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1888;

    private TextView txtInfo;
    private ImageView imgCapture;
    private String currentPhotoPath;
    private String username;

    private FaceRecognizer faceRecognizer = new FaceRecognizer();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        this.username = getIntent().getExtras().getString("username");
        this.txtInfo = (TextView) findViewById(R.id.txtInfo);
        this.imgCapture = (ImageView) findViewById(R.id.imgCapture);

        this.txtInfo.setText("Welcome " + this.username);
        this.requestRuntimePermission();
    }

    public void onCaptureClicked(View view) {
        dispatchTakePictureIntent();

//        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.abi_256);
//        this.processImage(image);
    }

    private void requestRuntimePermission() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(TrainActivity.this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(TrainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(TrainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(TrainActivity.this, "Permission Canceled, Terminating the application.", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Toast.makeText(this, "Received", Toast.LENGTH_SHORT).show();

            // Get the dimensions of the View
            int targetW = IMAGE_SIZE;
            int targetH = IMAGE_SIZE;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
            processImage(bitmap);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Error occurred while creating the File", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void processImage(Bitmap bitmap) {

        // Detect face
        Bitmap processedImage = ImageProcessor.process(bitmap);

        if (processedImage != null) {

            imgCapture.setImageBitmap(processedImage);
            txtInfo.setText("Successfully detected your face");
            txtInfo.setTextColor(Color.GREEN);

            progressDialog = ProgressDialog.show(this, "Training", "Please wait...");
            new DetectTask().execute(processedImage);

        } else {
            imgCapture.setImageResource(R.drawable.unknown);
            txtInfo.setText("The photo must contain a single face but found");
            txtInfo.setTextColor(Color.RED);
        }
    }

    private class TrainTask extends AsyncTask<Bitmap, Integer, Double> {
        protected Double doInBackground(Bitmap... bitmaps) {
            // Train
            double error = 0.0;
            faceRecognizer.open(getApplicationContext());
            try {
                error = faceRecognizer.train(username, bitmaps[0]);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            } finally {
                try {
                    faceRecognizer.save();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
                faceRecognizer.close();
            }
            return error;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Double result) {
            Log.i(TAG, "Error: " + result);
            progressDialog.hide();
            progressDialog.cancel();
        }
    }

    private class DetectTask extends AsyncTask<Bitmap, Integer, User> {
        protected User doInBackground(Bitmap... bitmaps) {
            // Train
            User user = null;
            faceRecognizer.open(getApplicationContext());
            try {
                user = faceRecognizer.find(bitmaps[0]);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                faceRecognizer.close();
            }
            return user;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(User result) {
            Log.i(TAG, "User: " + result);
            progressDialog.hide();
            progressDialog.cancel();
        }
    }

}
