package com.ureye.activities;

import android.annotation.SuppressLint;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.databinding.DataBindingUtil;

import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.camera.CameraSourceConfig;
import com.google.mlkit.vision.camera.CameraXSource;
import com.google.mlkit.vision.camera.DetectionTaskCallback;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.ureye.R;
import com.ureye.databinding.ActivityCameraDetectionBinding;
import com.ureye.utils.Constants;
import com.ureye.utils.StaticUtils;
import com.ureye.utils.objectdetector.ObjectGraphic;

import java.util.List;

@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class ObjectDetectionActivity extends BaseActivity implements OnRequestPermissionsResultCallback {

    private static final String TAG = "ObjectDetectionActivity";

    private LocalModel localModel;

    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private DetectionTaskCallback<List<DetectedObject>> detectionTaskCallback;
    private CameraXSource cameraXSource;
    private CustomObjectDetectorOptions customObjectDetectorOptions;
    private ActivityCameraDetectionBinding cameraDetectionBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraDetectionBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera_detection);
    }

    @Override
    public void initComponents() {
        localModel = new LocalModel.Builder().setAssetFilePath(Constants.OBJECT_MODEL_TFLITE).build();
        detectionTaskCallback = detectionTask -> detectionTask.addOnSuccessListener(this::onDetectionTaskSuccess).addOnFailureListener(this::onDetectionTaskFailure);
        if (StaticUtils.allPermissionsGranted(this)) {
            createThenStartCameraXSource();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        if (cameraXSource != null) {
            cameraXSource.start();
        } else {
            if (StaticUtils.allPermissionsGranted(this)) {
                createThenStartCameraXSource();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraXSource != null) {
            cameraXSource.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraXSource != null) {
            cameraXSource.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (StaticUtils.allPermissionsGranted(this)) {
            createThenStartCameraXSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("MissingPermission")
    private void createThenStartCameraXSource() {
        if (cameraXSource != null) {
            cameraXSource.close();
        }
        customObjectDetectorOptions = StaticUtils.getCustomObjectDetectorOptions(localModel, CustomObjectDetectorOptions.STREAM_MODE);
        ObjectDetector objectDetector = ObjectDetection.getClient(customObjectDetectorOptions);
        CameraSourceConfig.Builder builder = new CameraSourceConfig.Builder(getApplicationContext(), objectDetector, detectionTaskCallback)
                .setFacing(CameraSourceConfig.CAMERA_FACING_BACK);
        cameraXSource = new CameraXSource(builder.build(), cameraDetectionBinding.previewView);
        needUpdateGraphicOverlayImageSourceInfo = true;
        cameraXSource.start();
    }

    private void onDetectionTaskSuccess(List<DetectedObject> results) {
        cameraDetectionBinding.graphicOverlay.clear();
        if (needUpdateGraphicOverlayImageSourceInfo) {
            Size size = cameraXSource.getPreviewSize();
            if (size != null) {
                boolean isImageFlipped = cameraXSource.getCameraFacing() == CameraSourceConfig.CAMERA_FACING_FRONT;
                if (StaticUtils.isPortraitMode(this)) {
                    cameraDetectionBinding.graphicOverlay.setImageSourceInfo(size.getHeight(), size.getWidth(), isImageFlipped);
                } else {
                    cameraDetectionBinding.graphicOverlay.setImageSourceInfo(size.getWidth(), size.getHeight(), isImageFlipped);
                }
                needUpdateGraphicOverlayImageSourceInfo = false;
            }
        }
        Log.v(TAG, "Number of object been detected: " + results.size());
        for (DetectedObject object : results) {
            cameraDetectionBinding.graphicOverlay.add(new ObjectGraphic(cameraDetectionBinding.graphicOverlay, object));
        }
//        cameraDetectionBinding.graphicOverlay.add(new InferenceInfoGraphic(cameraDetectionBinding.graphicOverlay));
        cameraDetectionBinding.graphicOverlay.postInvalidate();
    }

    private void onDetectionTaskFailure(Exception e) {
        cameraDetectionBinding.graphicOverlay.clear();
        cameraDetectionBinding.graphicOverlay.postInvalidate();
        String error = "Failed to process. Error: " + e.getLocalizedMessage();
        StaticUtils.showToast(this, error + "\nCause: " + e.getCause());
        Log.d(TAG, error);
    }

}
