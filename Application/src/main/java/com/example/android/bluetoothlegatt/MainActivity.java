package com.example.android.bluetoothlegatt;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openBLEScanActivity();      //method to turn on
            }
        });

    }

    private void openBLEScanActivity(){
        Intent i = new Intent(MainActivity.this, DeviceScanActivity.class);
        //Change the activity.

        startActivity(i);
    }

}
