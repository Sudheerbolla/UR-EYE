package com.ureye.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ureye.BuildConfig;
import com.ureye.utils.common.LocationsModel;
import com.ureye.utils.facerecognition.SimilarityClassifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UREyeAppStorage {

    private static UREyeAppStorage UREyeAppStorage;
    protected Context mContext;
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mSharedPreferencesEditor;

    public static final String SP_IS_FIRST_TIME = "SP_IS_FIRST_TIME";
    public static final String SP_FACES_STORED = "SP_FACES_STORED";
    public static final String SP_SAVED_LOCATIONS = "SP_SAVED_LOCATIONS";

    private UREyeAppStorage(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }

    /**
     * Creates single instance of SharedPreferenceUtils
     *
     * @param context context of Activity or Service
     * @return Returns instance of SharedPreferenceUtils
     */
    public static synchronized UREyeAppStorage getInstance(Context context) {
        if (UREyeAppStorage == null) {
            UREyeAppStorage = new UREyeAppStorage(context.getApplicationContext());
        }
        return UREyeAppStorage;
    }

    /**
     * Stores String value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, String value) {
        mSharedPreferencesEditor.putString(key, value);
        mSharedPreferencesEditor.commit();
    }

    //Save Faces to Shared Preferences.Json String from Recognition object
    public void insertFacesToSP(HashMap<String, SimilarityClassifier.Recognition> jsonMap/*, boolean clear*/) {
        /*if (clear) jsonMap.clear();
        else */
        jsonMap.putAll(readSavedFacesFromSP());
        setValue(SP_FACES_STORED, new Gson().toJson(jsonMap));
    }

    //Load Faces from Shared Preferences.Json String to Recognition object
    public HashMap<String, SimilarityClassifier.Recognition> readSavedFacesFromSP() {
        String defValue = new Gson().toJson(new HashMap<String, SimilarityClassifier.Recognition>());
        String json = getValue(SP_FACES_STORED, defValue);
        TypeToken<HashMap<String, SimilarityClassifier.Recognition>> token = new TypeToken<HashMap<String, SimilarityClassifier.Recognition>>() {
        };
        HashMap<String, SimilarityClassifier.Recognition> retrievedMap = new Gson().fromJson(json, token.getType());
        //During type conversion and save/load procedure,format changes(eg float converted to double).
        //So embeddings need to be extracted from it in required format(eg.double to float).
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : retrievedMap.entrySet()) {
            float[][] output = new float[1][Constants.OUTPUT_SIZE];
            ArrayList arrayList = (ArrayList) entry.getValue().getExtra();
            arrayList = (ArrayList) arrayList.get(0);
            for (int counter = 0; counter < arrayList.size(); counter++) {
                output[0][counter] = ((Double) arrayList.get(counter)).floatValue();
            }
            entry.getValue().setExtra(output);
        }
        return retrievedMap;
    }

    //Save Locations to Shared Preferences.Json String from Location
    public void insertLocationToSP(LocationsModel locationsModel) {
        ArrayList<LocationsModel> locationsModelArrayList = readSavedLocationsFromSP();
//        if (locationsModelArrayList.contains(locationsModel)) {
//            locationsModelArrayList.remove(locationsModel);
//        }
        locationsModelArrayList.add(locationsModel);
        setValue(SP_SAVED_LOCATIONS, new Gson().toJson(locationsModelArrayList));
    }

    //Load Faces from Shared Preferences.Json String to Location object
    public ArrayList<LocationsModel> readSavedLocationsFromSP() {
        ArrayList<LocationsModel> locationsModelArrayList;
        Gson gson = new Gson();
        String defValue = gson.toJson(new ArrayList<LocationsModel>());
        String response = getValue(SP_SAVED_LOCATIONS, defValue);
        locationsModelArrayList = gson.fromJson(response, new TypeToken<List<LocationsModel>>() {
        }.getType());
        return locationsModelArrayList;
    }

    /**
     * Stores int value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, int value) {
        mSharedPreferencesEditor.putInt(key, value);
        mSharedPreferencesEditor.commit();
    }

    /**
     * Stores Double value in String format in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, double value) {
        setValue(key, Double.toString(value));
    }

    /**
     * Stores long value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, long value) {
        mSharedPreferencesEditor.putLong(key, value);
        mSharedPreferencesEditor.commit();
    }

    /**
     * Stores boolean value in preference
     *
     * @param key   key of preference
     * @param value value for that key
     */
    public void setValue(String key, boolean value) {
        mSharedPreferencesEditor.putBoolean(key, value);
        mSharedPreferencesEditor.commit();
    }

    /**
     * Retrieves String value from preference
     *
     * @param key          key of preference
     * @param defaultValue default value if no key found
     */
    public String getValue(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    /**
     * Retrieves int value from preference
     *
     * @param key          key of preference
     * @param defaultValue default value if no key found
     */
    public int getValue(String key, int defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Retrieves long value from preference
     *
     * @param key          key of preference
     * @param defaultValue default value if no key found
     */
    public long getValue(String key, long defaultValue) {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    /**
     * Retrieves boolean value from preference
     *
     * @param keyFlag      key of preference
     * @param defaultValue default value if no key found
     */
    public boolean getValue(String keyFlag, boolean defaultValue) {
        return mSharedPreferences.getBoolean(keyFlag, defaultValue);
    }

    /**
     * Removes key from preference
     *
     * @param key key of preference that is to be deleted
     */
    public void removeKey(String key) {
        if (mSharedPreferencesEditor != null) {
            mSharedPreferencesEditor.remove(key);
            mSharedPreferencesEditor.commit();
        }
    }

    /**
     * Clears all the preferences stored
     */
    public void clear() {
        mSharedPreferencesEditor.clear().commit();
    }

}