package com.example.android.vortexapp;

import android.app.Activity;
import android.app.IntentService;
import android.util.JsonToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Yesung on 11/25/2016.
 */

public class SVMClass extends Activity {

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

}
