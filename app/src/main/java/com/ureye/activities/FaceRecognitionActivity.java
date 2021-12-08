package com.ureye.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.ureye.BaseApplication;
import com.ureye.R;
import com.ureye.databinding.ActivityFaceRecognitionBinding;
import com.ureye.interfaces.VoiceRecognisationListener;
import com.ureye.utils.Constants;
import com.ureye.utils.StaticUtils;
import com.ureye.utils.UREyeAppStorage;
import com.ureye.utils.common.BitmapUtils;
import com.ureye.utils.common.LocationsModel;
import com.ureye.utils.facerecognition.SimilarityClassifier;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FaceRecognitionActivity extends BaseActivity implements View.OnClickListener, VoiceRecognisationListener {

    private FaceDetector detector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Interpreter tfLite;
    private CameraSelector cameraSelector;
    private boolean start = true;
    private final boolean flipX = false;
    private static final String TAG = FaceRecognitionActivity.class.getName();

    private int[] intValues;
    private float[][] embeedings;
    private ProcessCameraProvider cameraProvider;

    private HashMap<String, SimilarityClassifier.Recognition> savedFacesList;
    private ActivityFaceRecognitionBinding activityFaceRecognitionBinding;
    private UREyeAppStorage urEyeAppStorage;
    private boolean isAddFaceActive = false;
    private SpeechRecognizer speechRecognizer;
    private Location currentLocation;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        urEyeAppStorage = UREyeAppStorage.getInstance(this);
        savedFacesList = new HashMap<>();
        savedFacesList = urEyeAppStorage.readSavedFacesFromSP();
        activityFaceRecognitionBinding = DataBindingUtil.setContentView(this, R.layout.activity_face_recognition);
    }

    @Override
    public void initComponents() {
        activityFaceRecognitionBinding.addFace.setVisibility(View.INVISIBLE);
        activityFaceRecognitionBinding.facePreview.setVisibility(View.INVISIBLE);
        activityFaceRecognitionBinding.previewInfo.setText("\n Recognized Face:");

        activityFaceRecognitionBinding.addFace.setOnClickListener(this);

        //Load model
        try {
            tfLite = new Interpreter(StaticUtils.loadModelFile(FaceRecognitionActivity.this, Constants.FACE_MODEL_TFLITE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Initialize Face Detector
        FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build();
        detector = FaceDetection.getClient(highAccuracyOpts);

        cameraBind();
        if (SpeechRecognizer.isRecognitionAvailable(this))
            setUpVoiceRecognition();
    }

    private void setUpVoiceRecognition() {
        BaseApplication.getInstance().stopVoiceRecognizer();
        speechRecognizer = BaseApplication.getInstance().getVoiceRecognizer(this);
        if (speechRecognizer == null)
            StaticUtils.showToast(this, "No Speech Recognizer is available. Please install it in device with Speech Recognition available");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            speechRecognizer.destroy();
            BaseApplication.getInstance().stopSpeaking();
            BaseApplication.getInstance().stopVoiceRecognizer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_face:
                addFace();
                break;
            default:
                break;
        }
    }

    private void addFace() {
        start = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name");

        // Set up the input
        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            //Create and Initialize new object with Face embeddings and Name.
            SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition("0", "", -1f);
            result.setExtra(embeedings);

            savedFacesList.put(input.getText().toString(), result);
            start = true;
            urEyeAppStorage.insertFacesToSP(savedFacesList);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            start = true;
            dialog.cancel();
        });

        builder.show();
    }

    //Bind camera and preview view
    private void cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(Constants.CAM_FACE)
                .build();

        preview.setSurfaceProvider(activityFaceRecognitionBinding.previewView.getSurfaceProvider());
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            InputImage image = null;
            @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
            Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            }
            if (image != null) {
                detector.process(image)
                        .addOnSuccessListener(faces -> {
                            if (faces.size() != 0) {
                                Face face = faces.get(0);
                                Bitmap rotateBitmap = BitmapUtils.rotateBitmap(BitmapUtils.toBitmap(mediaImage), imageProxy.getImageInfo().getRotationDegrees(), false, false);
                                RectF boundingBox = new RectF(face.getBoundingBox());
                                Bitmap croppedFace = BitmapUtils.getCropBitmapByCPU(rotateBitmap, boundingBox);

                                if (flipX)
                                    croppedFace = BitmapUtils.rotateBitmap(croppedFace, 0, flipX, false);
                                //Scale the acquired Face to 112*112 which is required input for model
                                Bitmap scaled = BitmapUtils.getResizedBitmap(croppedFace, 112, 112);

                                if (start) recognizeImage(scaled);
                                try {
                                    Thread.sleep(20);  //Camera preview refreshed every 20 millisec(adjust as required)
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                activityFaceRecognitionBinding.recoName.setText(savedFacesList.isEmpty() ? "Add Face" : "No Face Detected!");
                            }
                        })
                        .addOnFailureListener(e -> {
                        })
                        .addOnCompleteListener(task -> {
                            imageProxy.close();
                        });
            }
        });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    public void recognizeImage(final Bitmap bitmap) {
        activityFaceRecognitionBinding.facePreview.setImageBitmap(bitmap);

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * Constants.INPUT_SIZE * Constants.INPUT_SIZE * 3 * 4);
        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[Constants.INPUT_SIZE * Constants.INPUT_SIZE];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < Constants.INPUT_SIZE; ++i) {
            for (int j = 0; j < Constants.INPUT_SIZE; ++j) {
                int pixelValue = intValues[i * Constants.INPUT_SIZE + j];
                imgData.putFloat((((pixelValue >> 16) & 0xFF) - Constants.IMAGE_MEAN) / Constants.IMAGE_STD);
                imgData.putFloat((((pixelValue >> 8) & 0xFF) - Constants.IMAGE_MEAN) / Constants.IMAGE_STD);
                imgData.putFloat(((pixelValue & 0xFF) - Constants.IMAGE_MEAN) / Constants.IMAGE_STD);
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();
        embeedings = new float[1][Constants.OUTPUT_SIZE];
        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
        float distance;

        if (savedFacesList.size() > 0) {
            final Pair<String, Float> nearest = findNearest(embeedings[0]);
            if (nearest != null) {
                final String name = nearest.first;
                distance = nearest.second;
                //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                if (distance < 1.000f) {
                    activityFaceRecognitionBinding.recoName.setText(name);
                    activityFaceRecognitionBinding.addFace.setVisibility(View.INVISIBLE);
                    activityFaceRecognitionBinding.facePreview.setVisibility(View.INVISIBLE);
                    isAddFaceActive = false;
                    BaseApplication.getInstance().continuousTextToSpeech("Found " + name);
                } else {
                    activityFaceRecognitionBinding.recoName.setText("Unknown");
                    activityFaceRecognitionBinding.addFace.setVisibility(View.VISIBLE);
                    activityFaceRecognitionBinding.facePreview.setVisibility(View.VISIBLE);
                    runFoundAFaceAlert();
                }
            }
        } else {
            activityFaceRecognitionBinding.addFace.setVisibility(View.VISIBLE);
            activityFaceRecognitionBinding.facePreview.setVisibility(View.VISIBLE);
            activityFaceRecognitionBinding.recoName.setText("Unknown");
            runFoundAFaceAlert();
        }
    }

    private void runFoundAFaceAlert() {
        if (!isAddFaceActive) {
            isAddFaceActive = true;
            BaseApplication.getInstance().continuousTextToSpeech(getString(R.string.new_face_found));
        }
    }

    private Pair<String, Float> findNearest(float[] emb) {
        Pair<String, Float> ret = null;
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : savedFacesList.entrySet()) {
            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];
            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff * diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }
        return ret;
    }

    @Override
    public void startListening() {
    }

    @Override
    public void errorDetecting(String message, int errorCode) {
    }

    @Override
    public void completedListening(String data) {
        Log.e(TAG, "voice data from user: " + data);
        if (!TextUtils.isEmpty(data)) {
            performAppropriateAction(data);
        }
    }

    private void performAppropriateAction(String data) {
        if (data.contains("add") || data.contains("yes") || data.contains("ok") || data.contains("okay") || data.contains("s")) {
            BaseApplication.getInstance().runTextToSpeech("Please say a name");
            isAddFaceActive = true;
        } else if (data.contains("quit") || data.contains("close") || data.contains("stop"))
            finishAffinity();
        else if (data.contains("apphelp")) {
            BaseApplication.getInstance().startHelpNotation();
        } else if (data.contains("location") || data.contains("place")) {
            getCurrentLocation();
        } else if (data.contains("help") || data.contains("emergency")) {
            StaticUtils.showToast(this, R.string.emergency_alert);
            BaseApplication.getInstance().stopListening(this);
            BaseApplication.getInstance().runTextToSpeech(getString(R.string.emergency_alert));
        } else if (data.contains("back")) {
            onBackPressed();
        } else {
            if (isAddFaceActive) {
                addFaceWithName(data);
            }
        }
    }

    private void addFaceWithName(String data) {
        start = false;
        SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition("0", "", -1f);
        result.setExtra(embeedings);
        savedFacesList.put(data, result);
        start = true;
        urEyeAppStorage.insertFacesToSP(savedFacesList);
        BaseApplication.getInstance().runTextToSpeech("Added " + data + " to storage");
        isAddFaceActive = false;
    }

    private void getCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean hasGps = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean hasNetwork = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location lastKnownLocationByGps = null, lastKnownLocationByNetwork = null;
        if (hasGps) {
            lastKnownLocationByGps = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            StaticUtils.turnOnGPSInSystem(this);
        }
        if (hasNetwork) {
            lastKnownLocationByNetwork = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastKnownLocationByGps != null && lastKnownLocationByNetwork != null) {
            currentLocation = lastKnownLocationByGps.getAccuracy() > lastKnownLocationByNetwork.getAccuracy() ? lastKnownLocationByGps : lastKnownLocationByNetwork;
        } else if (lastKnownLocationByGps == null && lastKnownLocationByNetwork != null) {
            currentLocation = lastKnownLocationByNetwork;
        } else if (lastKnownLocationByGps != null && lastKnownLocationByNetwork == null) {
            currentLocation = lastKnownLocationByGps;
        }
        if (currentLocation != null) {
            StaticUtils.showToast(this, "Your Location: " + "\n" + "Latitude: " + currentLocation.getLatitude() + "\n" + "Longitude: " + currentLocation.getLongitude());
            BaseApplication.getInstance().runTextToSpeech("Saved your current Location");
            UREyeAppStorage.getInstance(this).insertLocationToSP(new LocationsModel(currentLocation));
        }
    }

}