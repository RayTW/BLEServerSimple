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
    private static final String SERVICE_UUID = "82C447C1-D914-4259-A8B7-2A9B042348BC";
    private static final String CHAR_UUID_READ = "9ca2f07a-6cb9-4fc7-b168-f83662bc5abb";
    private static final String CHAR_UUID_WRITE = "45d9d1b2-594c-49e2-b1c8-964dbe886e40";
    private static final String CHAR_UUID_NOTIFY = "a03301da-c375-4dd5-854e-d2d28f00f82e";

    //Advertiser設定
    private static final boolean CONNECTABLE = true;
    private static final int TIMEOUT = 0;

    private BluetoothLeAdvertiser advertiser;
    private BluetoothGattServer gattServer;
    private Context mContext;

    //設定Advertiser & 開始
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startAdvertise(Context context) {
        mContext = context;
        BLEManager.getInstance(context).setBLELog("startAdvertise--");
        BLEManager.getInstance(context).setBLELog("serviceUUID-"+SERVICE_UUID);
        //BLE設定,Advertiser負責廣播被其他裝置搜尋,GattServer負責連線上後的資料傳輸
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        advertiser = getAdvertiser(adapter);
        gattServer = getGattServer(context, manager);

        Log.d(TAG, "gattServer=>" + gattServer);
        //初始化gatt server底下的service、service下放入Characteristic
        initService();

        //Advertiser開始
        advertiser.startAdvertising(makeAdvertiseSetting(), makeAdvertiseData(), this);

    }

    //Advertiser停止
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopAdvertise() {
        if(mContext != null){
            BLEManager.getInstance(mContext).setBLELog("stopAdvertise--");
        }
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
        BLEServerAdaptor bleserver = new BLEServerAdaptor(context);
        BluetoothGattServer server = manager.openGattServer(context, bleserver);
        bleserver.setBluetoothGattServer(server);
        return server;
    }

    private void initService() {
        //serviceUUID設定
        BluetoothGattService service = new BluetoothGattService(
                UUID.fromString(SERVICE_UUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //characteristicUUID設定
        BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_UUID_READ),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
        service.addCharacteristic(characteristicRead);

        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_UUID_WRITE),
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);

        service.addCharacteristic(characteristicWrite);

        BluetoothGattCharacteristic characteristicNotify = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_UUID_NOTIFY),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);

        service.addCharacteristic(characteristicNotify);

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
        builder.addServiceUuid(new ParcelUuid(UUID.fromString(SERVICE_UUID)));

        //設定用device name顯示為ble的local name
        builder.setIncludeDeviceName(true);

        return builder.build();
    }
}
