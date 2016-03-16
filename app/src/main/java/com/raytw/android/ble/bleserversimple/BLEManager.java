package com.raytw.android.ble.bleserversimple;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import com.raytw.android.ble.bleserversimple.central.LEScan;
import com.raytw.android.ble.bleserversimple.peripheral.AdvertiseAdaptor;

/**
 * Created by leeray on 16/3/16.
 */
public class BLEManager {
    private static BLEManager instance;
    private AdvertiseAdaptor mAdvertiseAdaptorJP = new AdvertiseAdaptor();
    private LEScan mLEScan;
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
        }else{
            mContext = context;
        }
        initialize();
    }

    private void initialize() {
        mLEScan = new LEScan(mContext);
    }

    public void startAdvertiseJP(){
        mAdvertiseAdaptorJP.startAdvertise(mContext);
    }

    public LEScan getLEScan(){
        return mLEScan;
    }

    public void stopAdvertiseJP(){
        mAdvertiseAdaptorJP.stopAdvertise();
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

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
