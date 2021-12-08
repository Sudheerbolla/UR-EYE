package com.ureye.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.ureye.utils.common.LocationsModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class StaticUtils {

    public static boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static String[] getRequiredPermissions(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    public static boolean allPermissionsGranted(Context context) {
        for (String permission : StaticUtils.getRequiredPermissions(context)) {
            if (!StaticUtils.isPermissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isKeyWordPresent(String word, String[] list) {
        for (String string : list) {
            if (word.contains(string)) return true;
        }
        return false;
    }

    public static void showSavedLocations(Context context) {
        ArrayList<LocationsModel> locationsModelArrayList = UREyeAppStorage.getInstance(context).readSavedLocationsFromSP();
        if (locationsModelArrayList.isEmpty()) {
            showToast(context, "No Saved Locations Found.");
            return;
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle("Saved Locations");

        final ArrayAdapter<LocationsModel> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.addAll(locationsModelArrayList);

        builderSingle.setNegativeButton("cancel", (dialog, which) -> dialog.dismiss());

        builderSingle.setAdapter(arrayAdapter, (dialog, which) -> {
        });
        builderSingle.show();
    }

    public static int getCatFromSpeech(String data) {
        int result = -1;
        if (isKeyWordPresent(data, Constants.GENERIC_KEYWORDS)) result = 0;
        else if (isKeyWordPresent(data, Constants.OBJECT_DETECTION_KEYWORDS)) result = 1;
        else if (isKeyWordPresent(data, Constants.TEXT_DETECTION_KEYWORDS)) result = 2;
        else if (isKeyWordPresent(data, Constants.FACE_DETECTION_KEYWORDS)) result = 3;
        else if (isKeyWordPresent(data, Constants.SAVED_DETECTION_KEYWORDS)) result = 4;
        return result;
    }

    public static void turnOnGPSInSystem(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", (dialog, which) -> context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton("No", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    public static boolean isPortraitMode(Context context) {
        return context.getApplicationContext().getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE;
    }

    public static CustomObjectDetectorOptions getCustomObjectDetectorOptions(LocalModel localModel, @ObjectDetectorOptionsBase.DetectorMode int mode) {
        CustomObjectDetectorOptions.Builder builder = new CustomObjectDetectorOptions.Builder(localModel).setDetectorMode(mode);
//        builder.enableMultipleObjects();
        builder.enableClassification().setMaxPerObjectLabelCount(1);
        return builder.build();
    }

    public static List<String> getRuntimePermissions(Context context) {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions(context)) {
            if (!isPermissionGranted(context, permission)) {
                allNeededPermissions.add(permission);
            }
        }
        return allNeededPermissions;
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context, int message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public static ByteBuffer loadModelFileA(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength).compact();
    }
}
