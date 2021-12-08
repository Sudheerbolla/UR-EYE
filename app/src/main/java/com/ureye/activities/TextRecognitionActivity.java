package com.ureye.activities;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ureye.BaseApplication;
import com.ureye.R;
import com.ureye.databinding.ActivityCameraDetectionBinding;
import com.ureye.utils.Constants;
import com.ureye.utils.StaticUtils;
import com.ureye.utils.camerautils.CameraXViewModel;
import com.ureye.utils.common.VisionImageProcessor;
import com.ureye.utils.textrecognition.TextRecognitionProcessor;

@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class TextRecognitionActivity extends BaseActivity implements OnRequestPermissionsResultCallback {
    private static final String TAG = "CameraXLivePreview";
    private static final int PERMISSION_REQUESTS = 1;
    private ActivityCameraDetectionBinding cameraDetectionBinding;
    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;
    @Nullable
    private ImageAnalysis analysisUseCase;
    @Nullable
    private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private CameraSelector cameraSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraDetectionBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera_detection);
    }

    @Override
    public void initComponents() {

        cameraSelector = new CameraSelector.Builder().requireLensFacing(Constants.CAM_FACE).build();

        new ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(this,
                        provider -> {
                            cameraProvider = provider;
                            if (StaticUtils.allPermissionsGranted(this)) {
                                bindAllCameraUseCases();
                            }
                        });

        if (!StaticUtils.allPermissionsGranted(this))
            ActivityCompat.requestPermissions(this, StaticUtils.getRuntimePermissions(this).toArray(new String[0]), PERMISSION_REQUESTS);

    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        BaseApplication.getInstance().stopSpeaking();
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase() {
        /*if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return;
        }*/
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        /*Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }*/
        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(cameraDetectionBinding.previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase);
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        try {
            imageProcessor = new TextRecognitionProcessor(this, new TextRecognizerOptions.Builder().build());
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor for Text Recog Latin: ", e);
            StaticUtils.showToast(this, "Can not create image processor: " + e.getLocalizedMessage());
            return;
        }

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
       /* Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }*/
        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = Constants.CAM_FACE == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            cameraDetectionBinding.graphicOverlay.setImageSourceInfo(imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            cameraDetectionBinding.graphicOverlay.setImageSourceInfo(imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        imageProcessor.processImageProxy(imageProxy, cameraDetectionBinding.graphicOverlay);
                    } catch (MlKitException e) {
                        StaticUtils.showToast(this, e.getLocalizedMessage());
                    }
                });

        cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (StaticUtils.allPermissionsGranted(this)) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
