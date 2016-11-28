package com.example.android.vortexapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.example.android.bluetoothlegatt.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;


public class TrainActivity extends Activity {

    Button btnTrain, btnFinish, btnCancel;
    TextView dataView, infoView;
    Spinner functionsList, timesList;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    BufferedReader mBufferedReader = null;
    SVMClass svmclass = new SVMClass();

    //private boolean initialDisplay = true;
    String selectedFunction = "0";
    private int collectTime = 1;
    private boolean collectRun = false;

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //btSocket = MainActivity.btSocket;

        //view of the TrainActivity
        setContentView(R.layout.activity_train);

        //call the widgtes
        btnTrain = (Button)findViewById(R.id.buttonTrain);
        btnFinish = (Button)findViewById(R.id.finishButton);
        btnCancel = (Button)findViewById(R.id.cancelButton);
        dataView = (TextView)findViewById(R.id.dataView);
        dataView.setMovementMethod(new ScrollingMovementMethod());
        dataView.setText(null);
        infoView = (TextView)findViewById(R.id.infoView);
        infoView.setText(null);
        functionsList = (Spinner)findViewById(R.id.functionsList);
        timesList = (Spinner)findViewById(R.id.timesList);

        new ConnectSPP().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        btnTrain.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dataView.setText(null);
                collectRun = true;
                Thread dataCollectorThread = new Thread(new Runnable(){
                    @Override
                    public void run(){
                        //code to do the HTTP request
                        try {
                            Thread.sleep(collectTime*1000);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                            msg("Timer messed up?");
                        }
                        collectRun = false;
                    }
                });
                dataCollectorThread.start();
                getDataSet();
                //turnOnLed();      //method to turn on
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffLed();   //method to turn off
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        functionsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selected = functionsList.getSelectedItem().toString();
                if (selected.contains("Stop")){
                    selectedFunction = "0";
                }
                else if (selected.contains("Forw")){
                    selectedFunction = "1";
                }
                else if (selected.contains("Back")){
                    selectedFunction = "2";
                }
                else if (selected.contains("Left")){
                    selectedFunction = "3";
                }
                else if (selected.contains("Right")){
                    selectedFunction = "4";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        timesList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selected = timesList.getSelectedItem().toString();
                if (selected.contains("1")){
                    doMath("1");
                }
                else if (selected.contains("2")){
                    doMath("2");
                }
                else if (selected.contains("3")){
                    doMath("3");
                }
                else if (selected.contains("4")){
                    doMath("4");
                }
                else if (selected.contains("5")){
                    doMath("5");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        finish(); //return to the first layout

    }

    public void onBackPressed(){
        Disconnect();
    }

    private void getDataSet(){
        while(collectRun){
            readDataLine();
        }
    }



    private void readDataLine(){
        boolean wrote, read;
        String rawdataline;

        wrote = true;
        if (btSocket != null){
            try{
                btSocket.getOutputStream().write("d".getBytes());
            }
            catch (IOException e)
            {
                msg("Error writing");
                wrote = false;
            }
        }
        read = true;
        rawdataline = "null";
        if(wrote){
            try{
                rawdataline = mBufferedReader.readLine();
            }
            catch (IOException e){
                msg("Error reading");
                read = false;
            }
        }
        String existingText = dataView.getText().toString();
        String fsrarray = svmclass.processData(rawdataline);
        if(read){
            dataView.setText(existingText + "\n" + selectedFunction + " " + fsrarray);
        }
        else{
            dataView.setText(existingText + "\n" + "failed somewhere");
        }
    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("0".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("1".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void doMath(String selected){
        int sec = Integer.parseInt(selected);
        collectTime = sec;
        int avgDataPerSec = 9;
        double dataPerRun = (double)sec*avgDataPerSec;

        double minData = 50;
        double idealData = 100;
        int minTrain = (int)Math.floor(minData/dataPerRun);
        int recTrain = (int)Math.floor(idealData/dataPerRun);

        String userInfo = "Recommend training each position at least " + minTrain + " times, ideally " + recTrain + " times if possible.";
        userInfo += "\nVary each gesture for better prediction.";
        infoView.setText(userInfo);
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectSPP extends AsyncTask<Void, Void, Void>  // UI dataCollectorThread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(TrainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
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
}
