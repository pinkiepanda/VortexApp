package com.example.android.vortexapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class MonitorActivity extends Activity {

    String systemPath;// = MainActivity.getAppContext().getFilesDir().getPath() + "/";
    String fulldataTrainPath;// = systemPath + MainActivity.dataTrainPath;
    String fulldataPredictPath;// = systemPath + MainActivity.dataPredictPath;
    String fullmodelPath;// = systemPath + MainActivity.modelPath;
    String fulloutputPath;// = systemPath + MainActivity.outputPath;

    TextView dataViewLive, predictView;
    RadioButton forwIndicator, backIndicator, leftIndicator, rightIndicator, stopIndicator;
    Switch activateSwitch;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    BufferedReader mBufferedReader = null;
    SVMClass svmclass;

    private boolean activated;
    private boolean wait;

    int i = 0;
    private final static int DO_UPDATE_TEXT = 1;
    private final static int DO_NOTHING = 0;
    private final Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch(what) {
                case DO_UPDATE_TEXT: doUpdate();  break;
                case DO_NOTHING: break;
            }
        }
    };

    private void doUpdate(){
        //dataViewLive.setText("Num is: " + i);
        //i++;
        readDataLine();
        wait = false;
    }

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        svmclass = new SVMClass();

        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

        dataViewLive = (TextView)findViewById(R.id.dataViewLive);
        predictView = (TextView)findViewById(R.id.predictView);
        //forwIndicator = (RadioButton)findViewById(R.id.forwIndicator);
        //backIndicator = (RadioButton)findViewById(R.id.backIndicator);
        //leftIndicator = (RadioButton)findViewById(R.id.leftIndicator);
        //rightIndicator = (RadioButton)findViewById(R.id.rightIndicator);
        //stopIndicator = (RadioButton)findViewById(R.id.stopIndicator);

        activated = false;

        Thread dataCollectorThread = new Thread(new Runnable(){
            @Override
            public void run(){

                while(true){
                    while(activated){
                        //readDataLine();
                        while(wait);
                        wait = true;
                        myHandler.sendEmptyMessage(DO_UPDATE_TEXT);
                        /*try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                            msg("Timer messed up?");
                        }*/
                    }
                }
            }
        });
        dataCollectorThread.start();
        activateSwitch = (Switch)findViewById(R.id.switch2);
        activateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    activated = true;
                } else {
                    activated = false;
                }
            }
        });

        new ConnectSPP().execute(); //Call the class to connect

        systemPath = MainActivity.getAppContext().getFilesDir().getPath() + "/";
        fulldataTrainPath = systemPath + MainActivity.dataTrainPath;
        fulldataPredictPath = systemPath + MainActivity.dataPredictPath;
        fullmodelPath = systemPath + MainActivity.modelPath;
        fulloutputPath = systemPath + MainActivity.outputPath;

        //boolean worked = trainProblem();
        //msg("Train has worked: " + worked);

    }

    private void readDataLine(){
        boolean sent, read, wrote;
        String rawdataline;

        sent = true;
        if (btSocket != null){
            try{
                btSocket.getOutputStream().write("d".getBytes());
            }
            catch (IOException e)
            {
                msg("Error writing");
                sent = false;
            }
        }
        read = true;
        rawdataline = "null";
        if(sent){
            try{
                rawdataline = mBufferedReader.readLine();
            }
            catch (IOException e){
                msg("Error reading");
                read = false;
            }
        }
        else{
            read = false;
        }
        String existingText = dataViewLive.getText().toString();
        String fsrarray = svmclass.processData(rawdataline);
        wrote = true;
        if(read){
            dataViewLive.setText(fsrarray);
            //totalData += selectedFunction + " " + fsrarray + "\n";

            try {
                File tempPath = MainActivity.getAppContext().getFilesDir();
                File tempFile = new File(tempPath,MainActivity.dataPredictPath+".txt");
                FileOutputStream stream = new FileOutputStream(tempFile);
                stream.write(("0 "+ fsrarray).getBytes());
                stream.close();
                predictView.setText("wrote predict data successfully");
            }catch(FileNotFoundException fnfe){
                dataViewLive.setText("failed file");
                wrote = false;
            }catch(IOException e){
                dataViewLive.setText("failed write");
                wrote = false;
            }

            if(wrote){
                boolean predicted = true;
                try{
                    svmclass.predict();
                    //jniSvmPredict(fulldataPredictPath+".txt "+fullmodelPath+".txt "+fulloutputPath+".txt");
                }catch (Exception e){
                    predictView.setText("failed predict");
                    predicted = false;
                }
                if(predicted){
                    File tempPath = MainActivity.getAppContext().getFilesDir();
                    File datafile = new File(tempPath,MainActivity.outputPath+".txt");
                    if (datafile.exists())
                    {
                        BufferedReader br;
                        String line;
                        try{
                            br = new BufferedReader(new FileReader(datafile));
                            line = br.readLine();
                            predictView.setText(line);
                        }catch(FileNotFoundException fnfe){
                            predictView.setText("failed file");
                        }catch (IOException ioe){
                            predictView.setText("failed read");
                        }
                    }
                }
            }
        }
        else{
            dataViewLive.setText("did not read");
        }
    }

    public void onBackPressed(){
        Disconnect();
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }

        //mainActivity.setSPPaddress(address);
        Intent data = new Intent();
        setResult(RESULT_OK,data);
        finish(); //return to the first layout
    }

    private class ConnectSPP extends AsyncTask<Void, Void, Void>  // UI dataCollectorThread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MonitorActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                InputStream aStream = null;
                InputStreamReader aReader = null;
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection

                    aStream = btSocket.getInputStream();
                    aReader = new InputStreamReader(aStream);
                    mBufferedReader = new BufferedReader(aReader);
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }
}
