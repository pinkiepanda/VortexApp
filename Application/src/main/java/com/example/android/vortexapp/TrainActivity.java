package com.example.android.vortexapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.example.android.bluetoothlegatt.R;
import com.example.android.vortexapp.SVMClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;


public class TrainActivity extends Activity {

    Button btnOn, btnOff, btnDis;
    TextView lumn, textView4;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    BufferedReader mBufferedReader = null;
    SVMClass svmclass = new SVMClass();

    private int collectTime;
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
        btnOn = (Button)findViewById(R.id.button2);
        btnOff = (Button)findViewById(R.id.button3);
        btnDis = (Button)findViewById(R.id.button4);
        lumn = (TextView)findViewById(R.id.lumn);
        textView4 = (TextView)findViewById(R.id.textView4);
        textView4.setMovementMethod(new ScrollingMovementMethod());
        textView4.setText(null);

        new ConnectSPP().execute(); //Call the class to connect

        //commands to be sent to bluetooth
        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                textView4.setText(null);
                collectTime = 3;
                collectRun = true;
                dataCollectorThread.start();
                getDataSet();
                //turnOnLed();      //method to turn on
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffLed();   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
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
        String existingText = textView4.getText().toString();
        String fsrarray = svmclass.processData(rawdataline);
        if(read){
            textView4.setText(existingText + "\n" + fsrarray);
        }
        else{
            textView4.setText(existingText + "\n" + "failed somewhere");
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
