package com.raytw.android.ble.bleserversimple; /**
 * Created by leeray on 16/3/16.
 */
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.UUID;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AdvertiseAdaptor extends AdvertiseCallback {
    private static final String TAG = AdvertiseAdaptor.class.getSimpleName();

    //UUID
    private static final String SERVICE_UUID_YOU_CAN_CHANGE = "5275fef2-72fb-4275-84d7-7fd44a160161";
    private static final String CHAR_UUID_YOU_CAN_CHANGE = "9ca2f07a-6cb9-4fc7-b168-f83662bc5abb";

    //Advertiser設定
    private static final boolean CONNECTABLE = true;
    private static final int TIMEOUT = 0;

    private BluetoothLeAdvertiser advertiser;
    private BluetoothGattServer gattServer;

    //設定Advertiser & 開始
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startAdvertise(Context context) {

        //BLE設定,Advertiser負責廣播被其他裝置搜尋,GattServer負責連線上後的資料傳輸
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        advertiser = getAdvertiser(adapter);
        gattServer = getGattServer(context, manager);
        Log.d(TAG, "gattServer=>" + gattServer);
        //初始化gatt server底下的service、service下放入Characteristic
        initService();

        //Advertiser開始
        advertiser.startAdvertising(makeAdvertiseSetting(),makeAdvertiseData(),this);
    }

    //Advertiser停止
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopAdvertise() {

        //關閉gatt server
        if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
        }

        //停止Advertising
        if (advertiser != null) {
            advertiser.stopAdvertising(this);
            advertiser = null;
        }
    }

    //Advertiser
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private BluetoothLeAdvertiser getAdvertiser(BluetoothAdapter adapter) {
        return adapter.getBluetoothLeAdvertiser();
    }

    //GattServer
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothGattServer getGattServer(Context context, BluetoothManager manager) {
        BLEServerAdaptor bleserver = new BLEServerAdaptor();
        BluetoothGattServer server = manager.openGattServer(context, bleserver);
        bleserver.setBluetoothGattServer(server);
        return server;
    }

    private void initService() {
        //serviceUUID設定
        BluetoothGattService service = new BluetoothGattService(
                UUID.fromString(SERVICE_UUID_YOU_CAN_CHANGE),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //characteristicUUID設定
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_UUID_YOU_CAN_CHANGE),
                BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        service.addCharacteristic(characteristic);

        gattServer.addService(service);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseSettings makeAdvertiseSetting() {

        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();

        //Set advertise mode to control the advertising power and latency.
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        //Set advertise TX power level to control the transmission power level for the advertising.
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW);
        //Set whether the advertisement type should be connectable or non-connectable.
        builder.setConnectable(CONNECTABLE);
        //Limit advertising to a given amount of time.
        builder.setTimeout(TIMEOUT);

        return builder.build();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseData makeAdvertiseData() {

        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        //加入自定的service uuid
        builder.addServiceUuid(new ParcelUuid(UUID.fromString(SERVICE_UUID_YOU_CAN_CHANGE)));

        //設定用device name顯示為ble的local name
        builder.setIncludeDeviceName(true);

        return builder.build();
    }
}