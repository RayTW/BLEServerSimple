package com.raytw.android.ble.bleserversimple;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import com.burns.android.ancssample.ANCSListener;
import com.burns.android.ancssample.OnIOSNotificationListener;

/**
 * Created by leeray on 16/3/16.
 */
public class BLEManager {
    private static BLEManager instance;
    private AdvertiseAdaptor mAdvertiseAdaptor;
    private Context mContext;

    public static BLEManager getInstance(Context context){
        if(instance == null){
            synchronized (BLEManager.class){
                if(instance == null){
                    instance = new BLEManager(context);
                }
            }
        }
        return instance;
    }

    public BLEManager(Context context){
        if(context.getApplicationContext() != null){
            mContext = context.getApplicationContext();
            mAdvertiseAdaptor = new AdvertiseAdaptor(mContext);
        }else{
            mContext = context;
        }
        initialize();
    }

    private void initialize() {
    }

    public void startAdvertise(){
        mAdvertiseAdaptor.startAdvertise(mContext);
    }

    public void stopAdvertise(){
        mAdvertiseAdaptor.stopAdvertise();
    }

    public void checkBLE(Activity activity, int requestCode){
        BluetoothManager manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!adapter.isEnabled()) {
            if (!adapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, requestCode);
            }
        }
    }

    public BluetoothAdapter getBluetoothAdapter(){
        BluetoothManager manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter();
    }

    public void addANCSListener(ANCSListener listner){
        mAdvertiseAdaptor.getBLEServerAdaptor().getANCSGattCallback().addStateListen(listner);
    }

    public void removeANCSListener(ANCSListener listner){
        mAdvertiseAdaptor.getBLEServerAdaptor().getANCSGattCallback().removeStateListen(listner);
    }

    public void addOnIOSNotificationListener(OnIOSNotificationListener listner){
        mAdvertiseAdaptor.getBLEServerAdaptor().getANCSGattCallback().addOnIOSNotificationListener(listner);
    }

    public void removeOnIOSNotificationListener(OnIOSNotificationListener listner){
        mAdvertiseAdaptor.getBLEServerAdaptor().getANCSGattCallback().removeOnIOSNotificationListener(listner);
    }

    public void removeAllListeners(){
        mAdvertiseAdaptor.getBLEServerAdaptor().getANCSGattCallback().removeAllListeners();
    }
}
