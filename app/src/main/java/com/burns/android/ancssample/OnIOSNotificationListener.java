package com.burns.android.ancssample;

/**
 * Created by ray.lee on 2016/11/8.
 */
public interface OnIOSNotificationListener {
    void onIOSNotificationAdd(IOSNotification n);
    void onIOSNotificationRemove(int uid);
}
