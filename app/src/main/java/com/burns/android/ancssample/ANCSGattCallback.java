package com.burns.android.ancssample;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;


public class ANCSGattCallback extends BluetoothGattCallback {

    private static final String TAG = "ANCSGattCallback";
    private static ANCSParser sANCSHandler;
    private Context mContext;
    private BleStatus mBleState;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mANCSservice;
    private boolean mWritedNS;
    private boolean mWriteNSDespOk;
    private ArrayList<ANCSListener> mStateListeners = new ArrayList<ANCSListener>();

    public ANCSGattCallback(Context context) {
        mContext = context;
        sANCSHandler = ANCSParser.getDefault(context);
    }

    public void addStateListen(ANCSListener sl) {
        if (!mStateListeners.contains(sl)) {
            mStateListeners.add(sl);
            sl.onStateChanged(mBleState);
        }
    }

    public void removeStateListen(ANCSListener sl) {
        if (!mStateListeners.contains(sl)) {
            mStateListeners.remove(sl);
        }
    }

    public void removeAllListeners(){
        mStateListeners.clear();
        sANCSHandler.removeAllListener();
    }

    public void addOnIOSNotificationListener(OnIOSNotificationListener listener){
        sANCSHandler.addOnIOSNotificationListener(listener);
    }

    public void removeOnIOSNotificationListener(OnIOSNotificationListener listener){
        sANCSHandler.removeOnIOSNotificationListener(listener);
    }

    public void stop() {
        Log.i(TAG, "stop connectGatt..");
        mBleState = BleStatus.DISCONNECT;
        for (ANCSListener sl : mStateListeners) {
            sl.onStateChanged(mBleState);
        }
        if (null != mBluetoothGatt) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;
        mANCSservice = null;
        mStateListeners.clear();
    }

    public BleStatus getBleState() {
        return mBleState;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic cha) {
        UUID uuid = cha.getUuid();
        if (uuid.equals(GattConstant.Apple.sUUIDChaNotify)) {

            Log.i(TAG, "Notify uuid");
            byte[] data = cha.getValue();
            sANCSHandler.onNotification(data);

            mBleState = BleStatus.BUILD_NOTIFY;//6
            for (ANCSListener sl : mStateListeners) {
                sl.onStateChanged(mBleState);
            }
        } else if (uuid.equals(GattConstant.Apple.sUUIDDataSource)) {

            byte[] data = cha.getValue();
            sANCSHandler.onDSNotification(data);
            Log.i(TAG, "datasource uuid");
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                        int newState) {
        Log.i(TAG, "onConnectionStateChange" + "newState " + newState + "status:" + status);
        try{
            mBleState = BleStatus.values()[newState];
        }catch(Exception e){
            e.printStackTrace();
        }
        notifyListeners();

        if (newState == BluetoothProfile.STATE_CONNECTED
                && status == BluetoothGatt.GATT_SUCCESS) {
            mBluetoothGatt = gatt;
            mBleState = BleStatus.BUILD_DISCOVER_SERVICE;
            notifyListeners();
            mBluetoothGatt.discoverServices();
        } else if (0 == newState/* && mDisconnectReq*/ && mBluetoothGatt != null) {
        }
    }

    @Override    // New services discovered
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.i(TAG, "onServicesDiscovered " + "status:" + status);
        mBleState = BleStatus.BUILD_DISCOVER_FINISH;
        notifyListeners();

        if (status != 0) {
            return;
        }
        BluetoothGattService ancs = gatt.getService(GattConstant.Apple.sUUIDANCService);

        printLog("find ancs=" + ancs);

        if (ancs == null) {
            Log.i(TAG, "cannot find ANCS uuid");
            return;
        }

        mBleState = BleStatus.BUILD_FIND_ANCS_SERVICE;
        notifyListeners();

        BluetoothGattCharacteristic DScha = ancs.getCharacteristic(GattConstant.Apple.sUUIDDataSource);
        if (DScha == null) {
            Log.i(TAG, "cannot find DataSource(DS) characteristic");
            return;
        }
        boolean registerDS = mBluetoothGatt.setCharacteristicNotification(DScha, true);
        if (!registerDS) {
            Log.i(TAG, " Enable (DS) notifications failed. ");
            return;
        }
        BluetoothGattDescriptor descriptor = DScha.getDescriptor(GattConstant.DESCRIPTOR_UUID);
        if (null != descriptor) {
            boolean r = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean rr = mBluetoothGatt.writeDescriptor(descriptor);

            Log.i(TAG, "Descriptoer setvalue " + r + "writeDescriptor() " + rr);
        } else {
            Log.i(TAG, "can not find descriptor from (DS)");
        }
        mWriteNSDespOk = mWritedNS = false;
        DScha = ancs.getCharacteristic(GattConstant.Apple.sUUIDControl);
        if (DScha == null) {
            Log.i(TAG, "can not find ANCS's ControlPoint cha ");
        }

        mANCSservice = ancs;
        sANCSHandler.setService(ancs, mBluetoothGatt);
        ANCSParser.get().reset();
        Log.i(TAG, "found ANCS service & set DS character,descriptor OK !");
    }

    @Override//the result of a descriptor write operation.
    public void onDescriptorWrite(BluetoothGatt gatt,
                                  BluetoothGattDescriptor descriptor, int status) {
        Log.i(TAG, "onDescriptorWrite" + "status:" + status);

        if (15 == status || 5 == status) {
            mBleState = BleStatus.BUILD_SETING_ANCS;
            notifyListeners();
            return;
        }
        if (status != BluetoothGatt.GATT_SUCCESS){
            return;
        }

        //for some ble device, writedescriptor on sUUIDDataSource will return 133. fixme.
        // status is 0, SUCCESS.
        if (mWritedNS && mWriteNSDespOk) {
            mBleState = BleStatus.ANCS_CONNECTED;
            notifyListeners();
        }

        if (mANCSservice != null && !mWritedNS) {    // set NS
            mWritedNS = true;
            BluetoothGattCharacteristic cha = mANCSservice
                    .getCharacteristic(GattConstant.Apple.sUUIDChaNotify);

            if (cha == null) {
                Log.i(TAG, "can not find ANCS's NS cha");
                return;
            }
            boolean registerNS = mBluetoothGatt.setCharacteristicNotification(
                    cha, true);
            if (!registerNS) {
                Log.i(TAG, " Enable (NS) notifications failed  ");
                return;
            }

            BluetoothGattDescriptor desp = cha.getDescriptor(GattConstant.DESCRIPTOR_UUID);
            if (null != desp) {
                boolean r = desp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                boolean rr = mBluetoothGatt.writeDescriptor(desp);
                mWriteNSDespOk = rr;
                Log.i(TAG, "(NS)Descriptor.setValue(): " + r + ",writeDescriptor(): " + rr);
            } else {
                Log.i(TAG, "null descriptor");
            }
        }
    }

    private void printLog(String text) {
        Log.d(TAG, text);
    }

    private void notifyListeners(){
        for (ANCSListener sl : mStateListeners) {
            sl.onStateChanged(mBleState);
        }
    }
}