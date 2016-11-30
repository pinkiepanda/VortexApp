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

    /*private final static String TAG = MainActivity.class.getSimpleName();

    public static final int STOP = 0, FORW = 1, BACK = 2, LEFT = 3, RIGHT = 4, UP = 10, DOWN = 11;
    private int prevCommand = STOP;
    private int[] keyStates = new int[]{0,0,0,0};
    private int alreadyPressedButton = STOP;
    private boolean aButtonAlreadyPressed = false;*/

    public static final String LOG_TAG = "AndroidLibSvm";
    public static String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static String appFolderPath = externalPath +"libsvm/";
    //public static String systemPath = getAppContext().getFilesDir().getPath() + "/";
    public static String dataTrainPath = "libsvm_trainingData";
    public static String dataPredictPath = "libsvm_sensorData";
    public static String modelPath = "libsvm_model";
    public static String outputPath = "libsvm_predict";

    Button vortexPairBtn, fmgConnectBtn, trainBtn;
    Button forwBtn, backBtn, leftBtn, rightBtn, stopBtn;
    Switch activateBtn;
    TextView commandText;
    public static BluetoothSocket btSocket = null;
    private String SPPaddress = "";
    private String BLEaddress = null;
    private String BLEname = null;
    private static boolean activated = false;
    public static String TARGET_ACTIVITY = "target_activity";
    public static String EXTRA_ADDRESS = "device_address";
    private boolean trained = false;

    public String getSPPaddress() {
        return SPPaddress;
    }

    public void setSPPaddress(String str) {
        SPPaddress = str;
    }

    //private BluetoothLeService mBluetoothLeService;
    //public connectionStateEnum mConnectionState;
    //private static BluetoothGattCharacteristic mSCharacteristic;

    public static Context getAppContext() {
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        this.c = getApplicationContext();

        //create necessary folder to save model files
        CreateAppFolderIfNeed();
        copyAssetsDataIfNeed();

        /*try {
            File tempPath = getAppContext().getFilesDir();
            File tempFile = new File(tempPath,dataTrainPath+".txt");
            FileOutputStream stream = new FileOutputStream(tempFile);
            stream.write("testing".getBytes());
            stream.close();
        }catch(FileNotFoundException fnfe){
            msg("failed file");
        }catch(IOException e){
            msg("failed write");
        }*/


        commandText = (TextView)findViewById(R.id.commandText);
        vortexPairBtn = (Button)findViewById(R.id.vortexPairBtn);
        vortexPairBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickStartBLEScan(v);      //method to turn on
            }
        });
        fmgConnectBtn = (Button)findViewById(R.id.fmgConnectBtn);
        fmgConnectBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickStartSPPScan(v);      //method to turn on
            }
        });
        trainBtn = (Button)findViewById(R.id.fmgTrainBtn);
        trainBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                goToTrain(v);      //method to turn on
            }
        });
        activateBtn = (Switch)findViewById(R.id.switch1);
        activateBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    activated = true;// The toggle is enabled
                    //msg("activated");
                } else {
                    //serialSend(STOP);
                    activated = false;// The toggle is disabled
                    //msg("deactivated");
                }
            }
        });

        //String systemPath = MainActivity.getAppContext().getFilesDir().getPath();
        String systemPath = getAppContext().getFilesDir().getPath() + "/";
        //msg(systemPath);
        //msg(dataTrainPath);
        String fulldataTrainPath = systemPath + MainActivity.dataTrainPath;
        msg(fulldataTrainPath);
        String fullmodelPath = systemPath + MainActivity.modelPath;
        msg(fullmodelPath);
        /*forwBtn = (Button)findViewById(R.id.forwardbutton);
        backBtn = (Button)findViewById(R.id.backbutton);
        rightBtn = (Button)findViewById(R.id.rightbutton);
        leftBtn = (Button)findViewById(R.id.leftbutton);
        stopBtn = (Button)findViewById(R.id.stopButton);

        stopBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                resetAll();
            }
        });
        forwBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    keyStates[0] = 1;
                    //msg("FORW");
                    //commandText.setText("FORW");
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    keyStates[0] = 0;
                }
                moveWithLogic();
                return true;
            }
        });
        backBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    keyStates[1] = 1;
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    keyStates[1] = 0;
                }
                moveWithLogic();
                return true;
            }
        });
        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    keyStates[2] = 1;
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    keyStates[2] = 0;
                }
                moveWithLogic();
                return true;
            }
        });
        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    keyStates[3] = 1;
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    keyStates[3] = 0;
                }
                moveWithLogic();
                return true;
            }
        });*/

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
        //msg(SPPaddress);
        /*if(trained){
            Intent i = new Intent(MainActivity.this, MonitorActivity.class);
            //Change the activity.
            i.putExtra(EXTRA_ADDRESS, SPPaddress); //this will be received at TrainActivity (class) Activity
            startActivity(i);
        }*/
        Intent i = new Intent(MainActivity.this,SVMActivity.class);
        startActivity(i);
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
                msg("Device \"" + BLEname + "\" at: " + BLEaddress);
                //initializeBLE();
            }
            else{
                msg("BLE failed");
            }
        }
        else if (requestCode == 2){
            //msg("back from SPP");
            if(resultCode == RESULT_OK){
                SPPaddress = data.getStringExtra("SPPaddress");
                msg("SPP address is: " + SPPaddress);
                Intent i = new Intent(MainActivity.this, MonitorActivity.class);
                i.putExtra(EXTRA_ADDRESS, SPPaddress);
                startActivityForResult(i, 3);
            }
            else{
                msg("SPP failed");
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

    }

    protected void onRestart(){
        super.onRestart();
        if(activated){
            msg("back to main!");
        }

    }

    /*public void moveWithLogic(){
        int target = 1;
        int keysPressed = 0;// = Collections.frequency(Arrays.asList(keyStates),target);
        for(int i = 0; i <= 3; i++){
            if(keyStates[i] == 1){
                keysPressed++;
            }
        }

        if(keysPressed > 1){
            return;
        }
        else{
            //msg("KeysPressed: " + keyStates[0]+keyStates[1]+keyStates[2]+keyStates[3]);
            //commandText.setText("KeysPressed: " + keyStates[0]+keyStates[1]+keyStates[2]+keyStates[3]);
            if(keysPressed == 0){
                serialSend(STOP);
                prevCommand = STOP;
            }
            else{
                int command = 0;// = Arrays.asList(keyStates).indexOf(1) + 1;
                for(int i = 0; i <= 3; i++){
                    if(keyStates[i] == 1){
                        command = i + 1;
                    }
                }
                if(command == prevCommand){
                    return;
                }
                else{
                    serialSend(command);
                    prevCommand = command;
                }
            }
        }
    }

    private void resetAll(){
        serialSend(STOP);
        prevCommand = STOP;
        keyStates = new int[]{0,0,0,0};
    }

    public void serialSend(int command) {
        if (this.mConnectionState == connectionStateEnum.isConnected && activated) {
            this.mBluetoothLeService.writeCustomCharacteristic(command);
            //msg("Sent: " + command);
            if(command == 0)
                commandText.setText("STOP");
            else if(command == 1)
                commandText.setText("FORW");
            else if(command == 2)
                commandText.setText("BACK");
            else if(command == 3)
                commandText.setText("LEFT");
            else if(command == 4)
                commandText.setText("RIGHT");
        }
        else{
            msg("failed!");
        }
    }

    private void initializeBLE(){
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                msg("Unable to initialize Bluetooth.");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(BLEaddress);
            final boolean result = mBluetoothLeService.connect(BLEaddress);
            if (result == true)
                mConnectionState = connectionStateEnum.isConnected;
            else
                mConnectionState = connectionStateEnum.isNull;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };*/

    public void onBackPressed(){
        //resetAll();
        finish();
    }

    protected void onDestroy(){
        super.onDestroy();
        //resetAll();

        /*unbindService(mServiceConnection);
        mBluetoothLeService = null;*/

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
