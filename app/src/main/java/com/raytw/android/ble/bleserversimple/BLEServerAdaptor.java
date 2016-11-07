package com.raytw.android.ble.bleserversimple; /**
 * Created by leeray on 16/3/16.
 */

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.burns.android.ancssample.ANCSGattCallback;
import com.burns.android.ancssample.ANCSParser;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEServerAdaptor extends BluetoothGattServerCallback {
    private static String TAG = BLEServerAdaptor.class.getSimpleName();

    private Context mContext;
    private ANCSGattCallback mANCSGattCallback;
    private ANCSParser mANCSHandler;

    private BluetoothGattServer bluetoothGattServer;
    public BLEServerAdaptor(Context context) {
        mContext = context;
        mANCSHandler = ANCSParser.getDefault(context);
        mANCSGattCallback = new ANCSGattCallback(context, mANCSHandler);
    }



    public void setBluetoothGattServer(BluetoothGattServer gattServer){
        this.bluetoothGattServer = gattServer;
    }

    @Override
    public void onConnectionStateChange(final BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);

        BluetoothGatt gatt = device.connectGatt(mContext, true, mANCSGattCallback);
//        device.connectGatt(mContext, true, new BluetoothGattCallback(){
//
//            @Override
//            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                super.onConnectionStateChange(gatt, status, newState);
//
//                if (newState == BluetoothProfile.STATE_CONNECTED
//                        && status == BluetoothGatt.GATT_SUCCESS) {
//                    printLog("STATE_CONNECTED");
//                    printLog("start discover service");
//
//                    gatt.discoverServices();
//
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
//                    printLog("STATE_DISCONNECTED");
//                }
//            }
//
//            @Override
//            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                printLog("onServicesDiscovered---gatt--->" + gatt);
//
//                if(gatt != null){
//                    List<BluetoothGattService> list =  gatt.getServices();
//                    printLog("onServicesDiscovered---serviceSize--->" + list.size());
//                    printLog("onServicesDiscovered---begin---");
//                    for(BluetoothGattService service : list){
//                        printLog( "suuid->" + service.getUuid());
//
//                        if(service.getUuid().toString().toUpperCase().equals("7905F431-B5CE-4E99-A40F-4B1E122D00D0")){
//                            printLog("find ancs");
//                            mANCSGattCallback.setBluetoothGatt(gatt);
//                            mANCSGattCallback.setStateStart();
//                        }
//                    }
//                    printLog("onServicesDiscovered---end---");
//                }
//
//            }
//        });
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicReadRequest");
        characteristic.setValue(new byte []{0xa,0xb,0xc,0xd});
        bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                characteristic.getValue());

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
        Log.d(TAG, "onCharacteristicWriteRequest,value=" + bytesToHex(value));

        bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        Log.d(TAG, "onDescriptorReadRequest");
    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        Log.d(TAG, "onDescriptorWriteRequest");
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
        Log.d(TAG, "onExecuteWrite");
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        super.onNotificationSent(device, status);
        Log.d(TAG, "onNotificationSent,status["+status+"]");
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
        super.onMtuChanged(device, mtu);
        Log.d(TAG, "onMtuChanged,mtu["+mtu+"]");
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

    private void printLog(String text){
        BLEManager.getInstance(mContext).setBLELog(text);
    }
}
