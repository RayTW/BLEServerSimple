package com.burns.android.ancssample;

/**
 * Created by ray.lee on 2016/11/8.
 */
public enum BleStatus {
    DISCONNECT,
    ANCS_CONNECTED,
    BUILD_START,
    BUILD_CONNECTED_GATT,
    BUILD_DISCOVER_SERVICE,
    BUILD_DISCOVER_FINISH,
    BUILD_SETING_ANCS,
    BUILD_NOTIFY,
    BUILD_FIND_ANCS_SERVICE,
    BUILD_ANCS_DESCRIPTOR_WRITE_ERROR;
}