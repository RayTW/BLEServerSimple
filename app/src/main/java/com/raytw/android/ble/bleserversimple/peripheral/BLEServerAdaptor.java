package com.raytw.android.ble.bleserversimple.peripheral; /**
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
import android.os.Build;
import android.util.Log;

import com.raytw.android.ble.bleserversimple.BLEManager;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEServerAdaptor extends BluetoothGattServerCallback {
    private static String TAG = BLEServerAdaptor.class.getSimpleName();

    private BluetoothGattServer bluetoothGattServer;
    public BLEServerAdaptor() {

    }

    public void setBluetoothGattServer(BluetoothGattServer gattServer){
        this.bluetoothGattServer = gattServer;
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);
        Log.d(TAG, "onConnectionStateChange,newState=" + newState);
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
        Log.d(TAG, "onServiceAdded,status=" + status);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicReadRequest");
        characteristic.setValue(new byte[]{0xa, 0xb, 0xc, 0xd});
        bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                characteristic.getValue());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
        Log.d(TAG, "onCharacteristicWriteRequest,value=" + BLEManager.bytesToHex(value));

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


}
