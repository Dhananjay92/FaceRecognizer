package com.abi.facerecognizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrainActivity extends AppCompatActivity {

    private static final String TAG = "TrainActivity";
    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1888;

    private TextView txtInfo;
    private ImageView imgCapture;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        this.txtInfo = (TextView) findViewById(R.id.txtInfo);
        this.imgCapture = (ImageView) findViewById(R.id.imgCapture);

        this.requestRuntimePermission();
    }

    public void onCaptureClicked(View view) {
        dispatchTakePictureIntent();
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
//        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//
//            // Detect face
//            Bitmap processedImage = ImageProcessor.process(bitmap);
//
//            if (processedImage != null) {
//
//                imgCapture.setImageBitmap(processedImage);
//                txtInfo.setText("Successfully detected your face");
//                txtInfo.setTextColor(Color.GREEN);
//
//            } else {
//                imgCapture.setImageResource(R.drawable.unknown);
//                txtInfo.setText("The photo must contain a single face but found");
//                txtInfo.setTextColor(Color.RED);
//            }
//        }

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Toast.makeText(this, "Received", Toast.LENGTH_SHORT).show();
            processImage();
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

    private void processImage() {
        // Get the dimensions of the View
        int targetW = imgCapture.getWidth();
        int targetH = imgCapture.getHeight();

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

        // Detect face
        Bitmap processedImage = ImageProcessor.process(bitmap);

        if (processedImage != null) {

            imgCapture.setImageBitmap(processedImage);
            txtInfo.setText("Successfully detected your face");
            txtInfo.setTextColor(Color.GREEN);

        } else {
            imgCapture.setImageResource(R.drawable.unknown);
            txtInfo.setText("The photo must contain a single face but found");
            txtInfo.setTextColor(Color.RED);
        }
//        imgCapture.setImageBitmap(bitmap);
    }
}
