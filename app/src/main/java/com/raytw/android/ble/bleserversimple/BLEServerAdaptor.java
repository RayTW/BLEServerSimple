package com.raytw.android.ble.bleserversimple; /**
 * Created by leeray on 16/3/16.
 */

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.burns.android.ancssample.ANCSGattCallback;
import com.burns.android.ancssample.util.Utility;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEServerAdaptor extends BluetoothGattServerCallback {
    private static String TAG = BLEServerAdaptor.class.getSimpleName();

    private Context mContext;
    private ANCSGattCallback mANCSGattCallback;
    private BluetoothGatt mBluetoothGatt;

    public BLEServerAdaptor(Context context) {
        mContext = context;
        mANCSGattCallback = new ANCSGattCallback(context);
        mANCSGattCallback.setOnGattDisconnectListener(new ANCSGattCallback.OnGattDisconnectListener() {
            @Override
            public void onGattDisconnectListener(BluetoothGatt gatt) {
                if(mBluetoothGatt == gatt){
                    closeGatt();
                }
            }
        });
    }

    public ANCSGattCallback getANCSGattCallback(){
        return mANCSGattCallback;
    }

    @Override
    public void onConnectionStateChange(final BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);

        if (newState == BluetoothProfile.STATE_CONNECTED
                && status == BluetoothGatt.GATT_SUCCESS) {
            printLog("STATE_CONNECTED");
            mBluetoothGatt = device.connectGatt(mContext, false, mANCSGattCallback);
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
            printLog("STATE_DISCONNECTED");

            if(mBluetoothGatt != null && mBluetoothGatt.getDevice() == device){
                closeGatt();
            }
        }
    }

    private synchronized void closeGatt(){
        printLog("closeGatt");
        // 防止出現status 133
        if(mBluetoothGatt != null){
            try{
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattCharacteristic characteristic) {
        printLog("onCharacteristicReadRequest");

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
        printLog("onCharacteristicWriteRequest,value=" + Utility.bytesToHex(value));
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        printLog("onDescriptorReadRequest");
    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        printLog("onDescriptorWriteRequest");
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
        printLog("onExecuteWrite");
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        super.onNotificationSent(device, status);
        printLog("onNotificationSent,status[" + status + "]");
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
        super.onMtuChanged(device, mtu);
        printLog("onMtuChanged,mtu["+mtu+"]");
    }

    private void printLog(String text){
//        BLEManager.getInstance(mContext).setBLELog(text);
        Log.d(TAG, text);
    }
}
