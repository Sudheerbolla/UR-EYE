package com.ureye.utils;

import androidx.camera.core.CameraSelector;

public class Constants {

    public static final int PERMISSION_REQUESTS = 1;
    public static final String FACE_MODEL_TFLITE = "ureye_face_model.tflite";
    public static final String OBJECT_MODEL_TFLITE = "ureye_object_labeler.tflite";

    public static final int CAM_FACE = CameraSelector.LENS_FACING_BACK; //CameraSelector.LENS_FACING_FRONT

    // for object detection
    public static final int INPUT_SIZE = 112;  //Input size for model
    public static final int OUTPUT_SIZE = 192; //Output size of model
    public static final float IMAGE_MEAN = 128.0f, IMAGE_STD = 128.0f;
    public static final boolean IS_MODEL_QUANTIZED = false;

    public static final int SELECTION_GENERAL_CATEGORY = 0;
    public static final int SELECTION_CATEGORY_OBJECT = 1;
    public static final int SELECTION_CATEGORY_TEXT = 2;
    public static final int SELECTION_CATEGORY_FACE = 3;
    public static final int SELECTION_CATEGORY_SAVED = 4;

    public static final String[] OBJECT_DETECTION_KEYWORDS = new String[]{"one", "1", "object", "motion","travel"};
    public static final String[] TEXT_DETECTION_KEYWORDS = new String[]{"two", "2", "text", "label", "heading","reader"};
    public static final String[] FACE_DETECTION_KEYWORDS = new String[]{"three", "3", "face", "friends", "people","meeting"};
    public static final String[] SAVED_DETECTION_KEYWORDS = new String[]{"four", "4", "localdata", "saved"};
    public static final String[] GENERIC_KEYWORDS = new String[]{"close", "stop", "back", "apphelp", "help", "emergency", "quit"};

}
