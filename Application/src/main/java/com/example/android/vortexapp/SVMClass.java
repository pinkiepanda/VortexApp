package com.example.android.vortexapp;

import android.app.Activity;
import android.app.IntentService;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.JsonToken;
import android.util.Log;

import com.example.android.bluetoothlegatt.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import java.util.ArrayList;

/**
 * Created by Yesung on 11/25/2016.
 */

public class SVMClass extends Activity {

    // link jni library
    static {
        System.loadLibrary("jnilibsvm");
    }

    // connect the native functions
    private native void jniSvmTrain(String cmd);
    private native void jniSvmPredict(String cmd);

    public String processData(String rawdataline){
        String stringtoreturn = "null";
        String cleanedrawdataline = rawdataline.substring(2);
        JSONObject jObject = null;
        JSONArray jArray = null;
        JsonToken jToken = null;
        boolean JSONValid = true, JarrayValid = true;
        try{
            jObject = new JSONObject(cleanedrawdataline);
        }catch (JSONException jex){
            JSONValid = false;
            stringtoreturn = "jobject failed";
        }
        if(JSONValid){
            try{
                //jToken = jObject.
                jArray = jObject.getJSONArray("fsr");
                //stringtoreturn = jArray.toString();
            }catch (JSONException jex){
                JarrayValid = false;
                stringtoreturn = "jarray failed";
            }
        }
        if(JarrayValid){
            stringtoreturn = "";
            int length = jArray.length();
            for(int i = 1; i <= length; i++){
                int num = jArray.optInt(i-1);
                stringtoreturn += i + ":" + num + " ";
            }
            //int[] nums = Integer.parseInt(jArray)
        }
        return stringtoreturn;
    }

    public void trainProblem(String fulldataset){

    }

}
