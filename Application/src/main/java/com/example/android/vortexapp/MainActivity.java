package com.example.android.vortexapp;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.example.android.bluetoothlegatt.R;

public class MainActivity extends Activity {

    Button vortexPairBtn, fmgConnectBtn;

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
                openBLEScanActivity(v);      //method to turn on
            }
        });
        fmgConnectBtn = (Button)findViewById(R.id.fmgConnectBtn);
        fmgConnectBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openSPPSCanActivity(v);      //method to turn on
            }
        });

    }

    private void openBLEScanActivity(View view){
        Intent i = new Intent(MainActivity.this, DeviceScanActivity.class);
        //Change the activity.

        startActivity(i);
    }

    private void openSPPSCanActivity(View view){
        Intent j = new Intent(MainActivity.this, DeviceListSPP.class);
        //Change the activity.
        startActivity(j);
    }

}
