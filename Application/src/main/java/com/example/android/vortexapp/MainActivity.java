package com.example.android.vortexapp;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.bluetoothlegatt.R;

public class MainActivity extends Activity {

    Button vortexPairBtn, fmgConnectBtn;
    Switch activateBtn;
    public static BluetoothSocket btSocket = null;
    private String SPPaddress = null;
    private String BLEaddress = null;
    private String BLEname = null;
    private static boolean activated = false;
    public static String TARGET_ACTIVITY = "target_activity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vortexPairBtn = (Button)findViewById(R.id.vortexPairBtn);
        vortexPairBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onClickStartbLEScan(v);      //method to turn on
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
        activateBtn = (Switch)findViewById(R.id.switch1);
        activateBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    activated = true;// The toggle is enabled
                    //msg("activated");
                } else {
                    activated = false;// The toggle is disabled
                    //msg("deactivated");
                }
            }
        });

    }

    public void onClickStartbLEScan(View view)
    {
        startActivityForResult(new Intent(this, DeviceScanActivity.class), 1);
    }

    public void onClickStartSPPScan(View view)
    {
        startActivityForResult(new Intent(this, DeviceListSPP.class), 2);
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
            }
            else{
                msg("SPP failed");
            }
            //ActiviyFinishedNowDoSomethingAmazing();
        }

    }

    protected void onRestart(){
        super.onRestart();
        if(activated){
            msg("back to main!");
        }

    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    /*private void openBLEScanActivity(View view){
        Intent i = new Intent(MainActivity.this, DeviceScanActivity.class);
        //Change the activity.

        startActivity(i);
    }

    private void openSPPSCanActivity(View view){
        Intent j = new Intent(MainActivity.this, DeviceListSPP.class);
        //Change the activity.
        startActivity(j);
    }*/
}
