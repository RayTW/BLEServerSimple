package com.raytw.android.ble.bleserversimple.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.burns.android.ancssample.ANCSListener;
import com.burns.android.ancssample.BleStatus;
import com.burns.android.ancssample.IOSNotification;
import com.burns.android.ancssample.OnIOSNotificationListener;
import com.burns.android.ancssample.util.Utility;
import com.raytw.android.ble.bleserversimple.BLEManager;
import com.raytw.android.ble.bleserversimple.R;

public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private TextView mShowBleLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        findViewById(R.id.startAdvertise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BLEManager.getInstance(MainActivity.this).stopAdvertise();
                BLEManager.getInstance(MainActivity.this).startAdvertise();
            }
        });

        findViewById(R.id.stopAdvertise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BLEManager.getInstance(MainActivity.this).stopAdvertise();
            }
        });

        mShowBleLogs = (TextView)findViewById(R.id.uuids);
        mShowBleLogs.setText("");

        initBLEListener();
    }

    //註冊接收到ancs通知
    private void initBLEListener(){
        BLEManager.getInstance(this).addOnIOSNotificationListener(new OnIOSNotificationListener() {
            @Override
            public void onIOSNotificationAdd(IOSNotification noti) {
                appendText("add,title["+noti.title+"],msg["+noti.message+"]");

                NotificationCompat.Builder build = new NotificationCompat.Builder(MainActivity.this)
		.setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(noti.title)
                        .setContentText(noti.message);
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(noti.uid, build.build());
                Utility.showText(MainActivity.this, "title["+noti.title+"],msg["+noti.message+"]", Toast.LENGTH_SHORT);
            }

            @Override
            public void onIOSNotificationRemove(int uid) {
                appendText("remove,noti,uid["+uid+"]");
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(uid);
            }
        });

        BLEManager.getInstance(this).addANCSListener(new ANCSListener() {
            @Override
            public void onStateChanged(BleStatus state) {
                appendText("BleStatus,["+state+"]");
            }
        });
    }

    private void appendText(final String text){
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
        BLEManager.getInstance(MainActivity.this).stopAdvertise();
        BLEManager.getInstance(this).removeAllListeners();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
