package com.raytw.android.ble.bleserversimple.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.raytw.android.ble.bleserversimple.BLEManager;
import com.raytw.android.ble.bleserversimple.R;
import com.raytw.android.ble.bleserversimple.util.PermissionsRequest;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private PermissionsRequest mPermissionsRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPermissionsRequest = buildPermissionsRequest();
        mPermissionsRequest.doCheckPermission(false);

        findViewById(R.id.startAdvertise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BLEManager.getInstance(MainActivity.this).getAdvertiseAdaptor().stopAdvertise();
                BLEManager.getInstance(MainActivity.this).getAdvertiseAdaptor().startAdvertise();
            }
        });

        findViewById(R.id.stopAdvertise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BLEManager.getInstance(MainActivity.this).getAdvertiseAdaptor().stopAdvertise();
            }
        });

        findViewById(R.id.startScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BLEManager.getInstance(MainActivity.this).getLEScan().stopLeScan();
                BLEManager.getInstance(MainActivity.this).getLEScan().startLeScan();
            }
        });

        findViewById(R.id.stopScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BLEManager.getInstance(MainActivity.this).getLEScan().stopLeScan();
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
        BLEManager.getInstance(MainActivity.this).getAdvertiseAdaptor().stopAdvertise();
    }

    private PermissionsRequest buildPermissionsRequest() {
        return new PermissionsRequest(this) {
            // 要請求權限時的callback
            @Override
            public List<String> getCheckPeremission() {
                ArrayList<String> permissionsNeeded = new ArrayList<String>();
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);

                return permissionsNeeded;
            }

            // 檢查權限完成，不論是否有取得授權，onCheckPeremissionCompleted一定會執行
            @Override
            public void onCheckPeremissionCompleted() {
            }
        };
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode,
                                                 String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mPermissionsRequest.onRequestPermissionsResult(requestCode,
                permissions, grantResults);
    }
}
