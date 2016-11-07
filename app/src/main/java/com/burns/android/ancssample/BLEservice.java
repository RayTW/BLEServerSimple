package com.burns.android.ancssample;


import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.burns.android.ancssample.ANCSGattCallback.StateListener;

public class BLEservice extends Service implements ANCSParser.onIOSNotification
		, ANCSGattCallback.StateListener{
	private static final String TAG="BLEservice";
	private final IBinder mBinder = new MyBinder();
	private ANCSParser mANCSHandler;
	private ANCSGattCallback mANCScb;
	BluetoothGatt mBluetoothGatt;
	BroadcastReceiver mBtOnOffReceiver;
	int mBleANCS_state = 0;
	private static BLEservice sInstance;

    public class MyBinder extends Binder {
    	BLEservice getService() {
            // Return this instance  so clients can call public methods
            return BLEservice.this;
        }
    }

	public static BLEservice getInstance(){
		return sInstance;
	}

    @SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){
			switch (msg.what) {
			case 11:	//bt off, stopSelf()
				stopSelf();

				break;
			}
    	}
    };

    
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
		sInstance = this;
		mANCSHandler = ANCSParser.getDefault(this);
		mANCScb = new ANCSGattCallback(this, mANCSHandler);
		mBtOnOffReceiver = new BroadcastReceiver() {
			public void onReceive(Context arg0, Intent i) {
				// action must be bt on/off .
				int state = i.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						BluetoothAdapter.ERROR);
				if (state == BluetoothAdapter.STATE_OFF) {
					Log.i(TAG,"bluetooth OFF !");
					mHandler.sendEmptyMessageDelayed(11, 500);
				}
			}
		};
		IntentFilter filter= new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// bt on/off
		registerReceiver(mBtOnOffReceiver, filter);
		Log.i(TAG,"onCreate()");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG,"onStartCommand() flags="+flags+",stardId="+startId);
		return START_STICKY;
		//return startId;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG," onDestroy()");
		mANCScb.stop();
		unregisterReceiver(mBtOnOffReceiver);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent i) {
		Log.i(TAG," onBind()thread id ="+android.os.Process.myTid());
		return mBinder;
	}

	//** when ios notification changed
	@Override
	public void onIOSNotificationAdd(IOSNotification noti) {
		NotificationCompat.Builder build = new
		NotificationCompat.Builder(this)
		.setContentTitle(noti.title)
		.setContentText(noti.message);
		//通知用預設聲音
		Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		build.setSound(uri);
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(noti.uid, build.build());
	}

	@Override
	public void onIOSNotificationRemove(int uid) {
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(uid);
	}
	
	//** public method , for client to call
	public void startBleConnect(String addr, boolean auto) {
		Log.i(TAG,"startBleConnect");
		if (mBleANCS_state != 0) {
			Log.i(TAG,"stop ancs,then restart it");
			mANCScb.stop();
		}
		BluetoothDevice dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
		mANCSHandler.listenIOSNotification(this);
		mBluetoothGatt = dev.connectGatt(this, auto, mANCScb);

		mANCScb.setStateStart();
	}


	public void registerStateChanged(StateListener sl) {
		Log.i(TAG,"registerStateChanged");
		if (null != sl)
			mANCScb.addStateListen(sl);
		mANCScb.addStateListen(this);
	}
	public void connect(){
			mBluetoothGatt.connect();
	}
	
	public String getStateDes(){
		return mANCScb.getState();
	}
	
	public int getmBleANCS_state() {
		return mBleANCS_state;
	}

	@Override
	public void onStateChanged(int state) {
		mBleANCS_state = state;
	}
	
}
