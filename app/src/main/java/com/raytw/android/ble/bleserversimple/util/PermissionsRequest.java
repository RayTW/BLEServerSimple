package com.raytw.android.ble.bleserversimple.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by leeray on 16/1/26.
 *
 * @version 1.0.0
 */
public abstract class PermissionsRequest {
    private final static String TAG = PermissionsRequest.class.getSimpleName();
    ;
    private boolean isDebug = false;

    static final int REQUEST_MY_PERMISSIONS_REQUEST_MULTIPLE_PERMISSIONS = 1000;

    private Activity mActivity;
    private HashMap<String, Integer> mPermissionsRequestResult = new HashMap<String, Integer>();

    public PermissionsRequest(Activity activity) {

        mActivity = activity;
    }

    private void log(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    /**
     * 此處需回傳要請求系統允許的權限, 例如:
     * <p/>
     * <pre>
     * {@code
     *
     *  public List<String> getCheckPeremission() {
     *      ArrayList<String> permissionsNeeded = new ArrayList<String>();
     *      permissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
     *      permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
     *      permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
     *
     *      return permissionsNeeded;
     *  }
     * </pre>
     *
     * @return
     */
    public abstract List<String> getCheckPeremission();

    /**
     * 檢查權限完成
     */
    public abstract void onCheckPeremissionCompleted();

    public void onShouldShowRequestPermissionRationale(
            List<String> permissions, List<String> permissionsRequest) {
        // TODO 使用者拒絕彈提示授權dialog
    }

    public void onRequestPermissionsResult(
            Map<String, Integer> permissionsWithGrantResults) {
        /**
         * TODO 請override此method，並自行決定授權有哪些允許的流程處理,
         * 使用者缺定是否允許授權，必須一樣一樣權限取出來判斷請求的權限是否有取得，範例如下:
         * if(permissionsWithGrantResults.get(Manifest.permission.GET_ACCOUNTS)
         * == PackageManager.PERMISSION_GRANTED){ //有取得”GET_ACCOUNTS”權限 }
         */
    }

    /**
     * 檢查目前是否有允許取得指定授權
     *
     * @param activity
     * @param permission
     * @return
     */
    public static boolean isPermissionGranted(@NonNull Activity activity,
                                              @NonNull String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            return !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, permission);
        } else {
            return true;
        }
    }

    /**
     * 檢查android 6.0以後手機是否有授權權限給app
     *
     * @param everytime 是否每次問 <b></>note : 但若是使用者勾選”不再提醒”，每次問會失效<b/>
     */
    public void doCheckPermission(boolean everytime) {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissionsNeeded = getCheckPeremission();
            List<String> permissionsRequest = new ArrayList<String>();
            List<String> permissionsShowRequest = new ArrayList<String>();
            mPermissionsRequestResult.clear();

            for (String permission : permissionsNeeded) {
                mPermissionsRequestResult.put(permission,
                        PackageManager.PERMISSION_DENIED);

                if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {

                    log("檢查是否此app有取得允許permission[" + permission + "][false]");

                    log("檢查是否此app有取得允許shouldShowRequestPermissionRationale["
                            + ActivityCompat
                            .shouldShowRequestPermissionRationale(
                                    mActivity, permission) + "]");

                    if (everytime) {
                        permissionsRequest.add(permission);
                    } else {
                        // 使用者拒絕過的授權，但可再次請求的授權
                        if (ActivityCompat
                                .shouldShowRequestPermissionRationale(
                                        mActivity, permission)) {
                            permissionsShowRequest.add(permission);
                        } else {
                            permissionsRequest.add(permission);
                        }
                    }
                } else {
                    log("檢查是否此app有取得允許permission[" + permission + "][true]");
                }
            }

            log("需要的權限=>" + permissionsNeeded);
            log("請求權限=>" + permissionsRequest);
            log("已拒絕權限=>" + permissionsShowRequest);

            if (permissionsShowRequest.size() > 0) {
                onShouldShowRequestPermissionRationale(permissionsShowRequest,
                        permissionsRequest);
            }

            if (permissionsRequest.size() > 0) {
                log("shouldShowRequestPermissionRationale,false");
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(mActivity, permissionsRequest
                                .toArray(new String[permissionsRequest.size()]),
                        REQUEST_MY_PERMISSIONS_REQUEST_MULTIPLE_PERMISSIONS);
                return;
            }
        }
        onCheckPeremissionCompleted();
    }

    // 請求app權限授權的response
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MY_PERMISSIONS_REQUEST_MULTIPLE_PERMISSIONS: {
                log("onRequestPermissionsResult,REQUEST_MY_PERMISSIONS_REQUEST_READ_CONTACTS,grantResults.length["
                        + grantResults.length + "]");

                for (int i = 0; i < grantResults.length; i++) {
                    mPermissionsRequestResult.put(permissions[i], grantResults[i]);
                }
                log("權限result=>" + mPermissionsRequestResult);

                onRequestPermissionsResult(mPermissionsRequestResult);
                onCheckPeremissionCompleted();
                return;
            }
        }
    }
}
