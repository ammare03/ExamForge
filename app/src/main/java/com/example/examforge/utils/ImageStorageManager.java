package com.example.examforge.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageStorageManager {
    private static final String TAG = "ImageStorageManager";
    private static final String PROFILE_IMAGES_DIR = "profile_images";

    // Save image to internal storage and return the file path
    public static String saveImageToInternalStorage(Context context, Uri imageUri, String userId) {
        if (imageUri == null) {
            return null;
        }

        // Create directory for profile images if it doesn't exist
        File directory = new File(context.getFilesDir(), PROFILE_IMAGES_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create a unique filename with user ID
        String filename = "profile_" + userId + ".jpg";
        File outputFile = new File(directory, filename);

        try {
            // Copy image data from URI to internal storage file
            try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                
                if (inputStream == null) {
                    return null;
                }
                
                // Copy the image data
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                
                return outputFile.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving image: " + e.getMessage());
            return null;
        }
    }

    // Delete image from internal storage
    public static boolean deleteImageFromInternalStorage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        File file = new File(imagePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
} 