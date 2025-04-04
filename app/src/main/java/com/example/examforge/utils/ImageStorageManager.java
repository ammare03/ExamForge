package com.example.examforge.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageStorageManager {
    private static final String TAG = "ImageStorageManager";
    private static final String PROFILE_IMAGES_DIR = "profile_images";

    // Save image to internal storage and return the file path
    public static String saveImageToInternalStorage(Context context, Uri imageUri, String userId) {
        if (imageUri == null) {
            Log.e(TAG, "saveImageToInternalStorage: imageUri is null");
            return null;
        }

        // Create directory for profile images if it doesn't exist
        File directory = new File(context.getFilesDir(), PROFILE_IMAGES_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            Log.d(TAG, "Created directory: " + created + " at " + directory.getAbsolutePath());
        } else {
            Log.d(TAG, "Directory already exists at " + directory.getAbsolutePath());
        }

        // Create a unique filename with user ID
        String filename = "profile_" + userId + ".jpg";
        File outputFile = new File(directory, filename);
        Log.d(TAG, "Saving image to: " + outputFile.getAbsolutePath());

        try {
            // Copy image data from URI to internal storage file
            try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                
                if (inputStream == null) {
                    Log.e(TAG, "Could not open input stream for URI: " + imageUri);
                    return null;
                }
                
                // Copy the image data
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytes = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                outputStream.flush();
                
                Log.d(TAG, "Successfully wrote image file: " + outputFile.getAbsolutePath() + 
                      " (" + totalBytes + " bytes, exists: " + outputFile.exists() + ")");
                
                return outputFile.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage(), e);
            return null;
        }
    }

    // Load Bitmap from internal storage path
    public static Bitmap loadImageFromInternalStorage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.e(TAG, "loadImageFromInternalStorage: imagePath is null or empty");
            return null;
        }

        try {
            File file = new File(imagePath);
            if (!file.exists()) {
                Log.e(TAG, "Image file does not exist: " + imagePath);
                return null;
            }
            
            Log.d(TAG, "Loading image from: " + imagePath + 
                  " (size: " + file.length() + " bytes, readable: " + file.canRead() + ")");

            return BitmapFactory.decodeFile(imagePath);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage(), e);
            return null;
        }
    }

    // Delete image from internal storage
    public static boolean deleteImageFromInternalStorage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.e(TAG, "deleteImageFromInternalStorage: imagePath is null or empty");
            return false;
        }

        File file = new File(imagePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "Deleted file: " + imagePath + " - success: " + deleted);
            return deleted;
        }
        Log.d(TAG, "File does not exist, cannot delete: " + imagePath);
        return false;
    }
} 