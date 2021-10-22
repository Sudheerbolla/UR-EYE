package com.ureye.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
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

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.ureye.R;
import com.ureye.databinding.ActivityFaceRecognitionBinding;
import com.ureye.utils.Constants;
import com.ureye.utils.StaticUtils;
import com.ureye.utils.UREyeAppStorage;
import com.ureye.utils.common.BitmapUtils;
import com.ureye.utils.facerecognition.SimilarityClassifier;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FaceRecognitionActivity extends BaseActivity implements View.OnClickListener {

    FaceDetector detector;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    Interpreter tfLite;
    CameraSelector cameraSelector;
    private boolean start = true;
    private final boolean flipX = false;

    int[] intValues;

    float[][] embeedings;
    ProcessCameraProvider cameraProvider;

    private HashMap<String, SimilarityClassifier.Recognition> savedFacesList;
    private ActivityFaceRecognitionBinding activityFaceRecognitionBinding;
    private UREyeAppStorage urEyeAppStorage;

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

        activityFaceRecognitionBinding.actions.setOnClickListener(this);
        activityFaceRecognitionBinding.recognize.setOnClickListener(this);
        activityFaceRecognitionBinding.addFace.setOnClickListener(this);
        activityFaceRecognitionBinding.actions.setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actions:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Action:");
                String[] names = {"View Recognition List", "Update Recognition List", "Save Recognitions", "Load Recognitions", "Clear All Recognitions"};

                builder.setItems(names, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            displaynameListview();
                            break;
                        case 1:
                            updatenameListview();
                            break;
                        case 2:
                            urEyeAppStorage.insertFacesToSP(savedFacesList, false);
                            break;
                        case 3:
                            savedFacesList.putAll(urEyeAppStorage.readSavedFacesFromSP());
                            break;
                        case 4:
                            clearnameList();
                            break;
                    }
                });

                builder.setPositiveButton("OK", (dialog, which) -> {

                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.recognize:
                if (activityFaceRecognitionBinding.recognize.getText().toString().equals("Recognize")) {
                    start = true;
                    activityFaceRecognitionBinding.recognize.setText("Add Face");
                    activityFaceRecognitionBinding.addFace.setVisibility(View.INVISIBLE);
                    activityFaceRecognitionBinding.recoName.setVisibility(View.VISIBLE);
                    activityFaceRecognitionBinding.facePreview.setVisibility(View.INVISIBLE);
                    activityFaceRecognitionBinding.previewInfo.setText("\n    Recognized Face:");
                } else {
                    activityFaceRecognitionBinding.recognize.setText("Recognize");
                    activityFaceRecognitionBinding.addFace.setVisibility(View.VISIBLE);
                    activityFaceRecognitionBinding.recoName.setVisibility(View.INVISIBLE);
                    activityFaceRecognitionBinding.facePreview.setVisibility(View.VISIBLE);
                    activityFaceRecognitionBinding.previewInfo.setText("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.");
                }
                break;
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
        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(context, input.getText().toString(), Toast.LENGTH_SHORT).show();

                //Create and Initialize new object with Face embeddings and Name.
                SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition("0", "", -1f);
                result.setExtra(embeedings);

                savedFacesList.put(input.getText().toString(), result);
                start = true;

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                start = true;
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void clearnameList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to delete all Recognitions?");
        builder.setPositiveButton("Delete All", (dialog, which) -> {
            savedFacesList.clear();
            StaticUtils.showToast(FaceRecognitionActivity.this, "Recognitions Cleared");
        });
        urEyeAppStorage.insertFacesToSP(savedFacesList, true);
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updatenameListview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (savedFacesList.isEmpty()) {
            builder.setTitle("No Faces Added!!");
            builder.setPositiveButton("OK", null);
        } else {
            builder.setTitle("Select Recognition to delete:");

            // add a checkbox list
            String[] names = new String[savedFacesList.size()];
            boolean[] checkedItems = new boolean[savedFacesList.size()];
            int i = 0;
            for (Map.Entry<String, SimilarityClassifier.Recognition> entry : savedFacesList.entrySet()) {
                //System.out.println("NAME"+entry.getKey());
                names[i] = entry.getKey();
                checkedItems[i] = false;
                i = i + 1;

            }

            builder.setMultiChoiceItems(names, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    // user checked or unchecked a box
                    //Toast.makeText(MainActivity.this, names[which], Toast.LENGTH_SHORT).show();
                    checkedItems[which] = isChecked;

                }
            });


            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // System.out.println("status:"+ Arrays.toString(checkedItems));
                    for (int i = 0; i < checkedItems.length; i++) {
                        //System.out.println("status:"+checkedItems[i]);
                        if (checkedItems[i]) {
//                                Toast.makeText(MainActivity.this, names[i], Toast.LENGTH_SHORT).show();
                            savedFacesList.remove(names[i]);
                        }

                    }
                    StaticUtils.showToast(FaceRecognitionActivity.this, "Recognitions Updated");
                }
            });
            builder.setNegativeButton("Cancel", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void displaynameListview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // System.out.println("Registered"+registered);
        builder.setTitle(savedFacesList.isEmpty() ? "No Faces Added!!" : "Recognitions:");

        // add a checkbox list
        String[] names = new String[savedFacesList.size()];
        boolean[] checkedItems = new boolean[savedFacesList.size()];
        int i = 0;
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : savedFacesList.entrySet()) {
            //System.out.println("NAME"+entry.getKey());
            names[i] = entry.getKey();
            checkedItems[i] = false;
            i = i + 1;

        }
        builder.setItems(names, null);

        builder.setPositiveButton("OK", (dialog, which) -> {

        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Bind camera and preview view
    private void cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this in Future.
                // This should never be reached.
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
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                .build();

        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            InputImage image = null;
            @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
            // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)
            Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            }
            //Process acquired image to detect faces
            if (image != null) {
                Task<List<Face>> result = detector.process(image)
                        .addOnSuccessListener(
                                faces -> {
                                    if (faces.size() != 0) {
                                        Face face = faces.get(0); //Get first face from detected faces
                                        System.out.println(face);

                                        //mediaImage to Bitmap
                                        Bitmap frame_bmp = BitmapUtils.toBitmap(mediaImage);

                                        int rot = imageProxy.getImageInfo().getRotationDegrees();

                                        //Adjust orientation of Face
                                        Bitmap frame_bmp1 = BitmapUtils.rotateBitmap(frame_bmp, rot, false, false);

                                        //Get bounding box of face
                                        RectF boundingBox = new RectF(face.getBoundingBox());

                                        //Crop out bounding box from whole Bitmap(image)
                                        Bitmap cropped_face = BitmapUtils.getCropBitmapByCPU(frame_bmp1, boundingBox);

                                        if (flipX)
                                            cropped_face = BitmapUtils.rotateBitmap(cropped_face, 0, flipX, false);
                                        //Scale the acquired Face to 112*112 which is required input for model
                                        Bitmap scaled = BitmapUtils.getResizedBitmap(cropped_face, 112, 112);

                                        if (start)
                                            recognizeImage(scaled); //Send scaled bitmap to create face embeddings.
                                        System.out.println(boundingBox);
                                        try {
                                            Thread.sleep(10);  //Camera preview refreshed every 10 millisec(adjust as required)
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
        // set Face to Preview
        activityFaceRecognitionBinding.facePreview.setImageBitmap(bitmap);

        //Create ByteBuffer to store normalized image

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * Constants.INPUT_SIZE * Constants.INPUT_SIZE * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[Constants.INPUT_SIZE * Constants.INPUT_SIZE];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < Constants.INPUT_SIZE; ++i) {
            for (int j = 0; j < Constants.INPUT_SIZE; ++j) {
                int pixelValue = intValues[i * Constants.INPUT_SIZE + j];
                if (Constants.IS_MODEL_QUANTIZED) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else {
                    // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - Constants.IMAGE_MEAN) / Constants.IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - Constants.IMAGE_MEAN) / Constants.IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - Constants.IMAGE_MEAN) / Constants.IMAGE_STD);

                }
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();
        embeedings = new float[1][Constants.OUTPUT_SIZE]; //output of model will be stored in this variable
        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model
        float distance;
        //Compare new face with saved Faces.
        if (savedFacesList.size() > 0) {
            final Pair<String, Float> nearest = findNearest(embeedings[0]);//Find closest matching face
            if (nearest != null) {
                final String name = nearest.first;
                distance = nearest.second;
                //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                activityFaceRecognitionBinding.recoName.setText(distance < 1.000f ? name : "Unknown");
                System.out.println("nearest: " + name + " - distance: " + distance);
            }
        }
    }

    //Compare Faces by distance between face embeddings
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

}