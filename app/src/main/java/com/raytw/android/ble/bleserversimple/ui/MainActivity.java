package com.raytw.android.ble.bleserversimple.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.raytw.android.ble.bleserversimple.BLEManager;
import com.raytw.android.ble.bleserversimple.R;

public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private TextView mShowBleLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.startAdvertise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BLEManager.getInstance(MainActivity.this).stopAdvertiseJP();
                BLEManager.getInstance(MainActivity.this).startAdvertiseJP();
            }
        });

        findViewById(R.id.stopAdvertise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BLEManager.getInstance(MainActivity.this).stopAdvertiseJP();
            }
        });

        mShowBleLogs = (TextView)findViewById(R.id.uuids);
        mShowBleLogs.setText("");

        BLEManager.getInstance(this).setOnBLELogListener(new BLEManager.OnBLELogListener() {
            @Override
            public void onBLELog(final String text) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mShowBleLogs.getText().length() != 0){
                            mShowBleLogs.append("\n" + text);
                        }else{
                            mShowBleLogs.append(text);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        BLEManager.getInstance(this).checkBLE(this, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BLEManager.getInstance(MainActivity.this).stopAdvertiseJP();
    }
}
