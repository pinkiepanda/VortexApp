package com.example.android.vortexapp;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.bluetoothlegatt.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private static Context c;

    public static final String LOG_TAG = "AndroidLibSvm";
    public static String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static String appFolderPath = externalPath +"libsvm/";
    //public static String systemPath = getAppContext().getFilesDir().getPath() + "/";
    public static String dataTrainPath = "libsvm_trainingData";
    public static String dataPredictPath = "libsvm_sensorData";
    public static String modelPath = "libsvm_model";
    public static String outputPath = "libsvm_predict";

    Button vortexPairBtn, trainBtn, monitorBtn;
    private String SPPaddress = "";
    private String BLEaddress = null;
    private String BLEname = null;
    private static boolean activated = false;
    public static String TARGET_ACTIVITY = "target_activity";
    public static String EXTRA_ADDRESS = "device_address";
    private boolean trained = false;

    //private BluetoothLeService mBluetoothLeService;
    //public connectionStateEnum mConnectionState;
    //private static BluetoothGattCharacteristic mSCharacteristic;

    public static Context getAppContext() {
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.c = getApplicationContext();

        //create necessary folder to save model files
        CreateAppFolderIfNeed();
        copyAssetsDataIfNeed();

        vortexPairBtn = (Button)findViewById(R.id.vortexPairBtn);
        vortexPairBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickStartBLEScan(v);      //method to turn on
            }
        });
        vortexPairBtn.setEnabled(false);
        trainBtn = (Button)findViewById(R.id.fmgTrainBtn);
        trainBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickStartSPPScan(v);      //method to turn on
            }
        });
        monitorBtn = (Button)findViewById(R.id.fmgMonitorBtn);
        monitorBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                goToTrain(v);      //method to turn on
            }
        });


        //String systemPath = MainActivity.getAppContext().getFilesDir().getPath();
        String systemPath = getAppContext().getFilesDir().getPath() + "/";
        //msg(systemPath);
        //msg(dataTrainPath);
        String fulldataTrainPath = systemPath + MainActivity.dataTrainPath;
        //msg(fulldataTrainPath);
        String fullmodelPath = systemPath + MainActivity.modelPath;
        //msg(fullmodelPath);

    }

    public void onClickStartBLEScan(View view)
    {
        //resetAll();
        startActivityForResult(new Intent(this, DeviceScanActivity.class), 1);
    }

    public void onClickStartSPPScan(View view)
    {
        //resetAll();
        startActivityForResult(new Intent(this, DeviceListSPP.class), 2);
    }

    public void goToTrain(View view){
        startActivityForResult(new Intent(this, DeviceListSPP.class), 4);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1){
            //msg("back from BLE");
            if(resultCode == RESULT_OK){
                BLEaddress = data.getStringExtra("BLEaddress");
                BLEname = data.getStringExtra("BLEname");
                //msg("Device \"" + BLEname + "\" at: " + BLEaddress);
                //initializeBLE();
            }
            else{
                //msg("BLE failed");
            }
        }
        else if (requestCode == 2){
            //msg("back from SPP");
            if(resultCode == RESULT_OK){
                SPPaddress = data.getStringExtra("SPPaddress");
                //msg("SPP address is: " + SPPaddress);
                Intent i = new Intent(MainActivity.this, TrainActivity.class);
                i.putExtra(EXTRA_ADDRESS, SPPaddress);
                startActivityForResult(i, 3);
            }
            else{
                //msg("SPP failed");
            }
        }
        else if (requestCode == 3){
            //msg("back from SPP");
            if(resultCode == RESULT_OK){
                trained = true;
                msg("Train successful.");
            }
            else{
                //msg("Train failed.");
            }
        }
        else if (requestCode == 4){
            if(resultCode == RESULT_OK){
                SPPaddress = data.getStringExtra("SPPaddress");
                //msg("SPP address is: " + SPPaddress);
                Intent i = new Intent(MainActivity.this, MonitorActivity.class);
                i.putExtra(EXTRA_ADDRESS, SPPaddress);
                startActivity(i);
            }
            else{
                //msg("SPP failed");
            }
        }

    }

    protected void onRestart(){
        super.onRestart();
        if(activated){
            //msg("back to main!");
        }

    }

    public void onBackPressed(){
        finish();
    }

    protected void onDestroy(){
        super.onDestroy();
        finish();
    }

    // ----- from AndroidLibSVM by yctung (jnilibsvm library source) ------
    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    /*
        * Some utility functions
        * */
    private void CreateAppFolderIfNeed(){
        // 1. create app folder if necessary
        File folder = new File(appFolderPath);

        if (!folder.exists()) {
            folder.mkdir();
            Log.d(LOG_TAG,"Appfolder is not existed, create one");
        } else {
            Log.w(LOG_TAG,"WARN: Appfolder has not been deleted");
        }


    }

    private void copyAssetsDataIfNeed(){
        String assetsToCopy[] = {"heart_scale_predict","heart_scale_train","heart_scale"};
        //String targetPath[] = {C.externalPath+C.INPUT_FOLDER+C.INPUT_PREFIX+AudioConfigManager.inputConfigTrain+".wav", C.externalPath+C.INPUT_FOLDER+C.INPUT_PREFIX+AudioConfigManager.inputConfigPredict+".wav",C.externalPath+C.INPUT_FOLDER+"SomeoneLikeYouShort.mp3"};

        for(int i=0; i<assetsToCopy.length; i++){
            String from = assetsToCopy[i];
            String to = appFolderPath+from;

            // 1. check if file exist
            File file = new File(to);
            if(file.exists()){
                Log.d(LOG_TAG, "copyAssetsDataIfNeed: file exist, no need to copy:"+from);
            } else {
                // do copy
                boolean copyResult = copyAsset(getAssets(), from, to);
                Log.d(LOG_TAG, "copyAssetsDataIfNeed: copy result = "+copyResult+" of file = "+from);
            }
        }
    }

    private boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "[ERROR]: copyAsset: unable to copy file = "+fromAssetPath);
            return false;
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }


}
