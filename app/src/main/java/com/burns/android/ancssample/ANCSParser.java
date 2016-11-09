package com.burns.android.ancssample;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ANCSParser {
	// ANCS constants
	public final static int NotificationAttributeIDAppIdentifier = 0;
	public final static int NotificationAttributeIDTitle = 1; //, (Needs to be followed by a 2-bytes max length parameter)
	public final static int NotificationAttributeIDSubtitle = 2; //, (Needs to be followed by a 2-bytes max length parameter)
	public final static int NotificationAttributeIDMessage = 3; //, (Needs to be followed by a 2-bytes max length parameter)
	public final static int NotificationAttributeIDMessageSize = 4; //,
	public final static int NotificationAttributeIDDate = 5; //,
	public final static int AppAttributeIDDisplayName = 0;

	public final static int CommandIDGetNotificationAttributes = 0;
	public final static int CommandIDGetAppAttributes = 1;

	public final static int EventFlagSilent = (1 << 0);
	public final static int EventFlagImportant = (1 << 1);
	public final static int EventIDNotificationAdded = 0;
	public final static int EventIDNotificationModified = 1;
	public final static int EventIDNotificationRemoved = 2;

	// !ANCS constants

	private final static int MSG_ADD_NOTIFICATION = 100;
	private final static int MSG_DO_NOTIFICATION = 101;
	private final static int MSG_RESET = 102;
	private final static int MSG_ERR = 103;
	private final static int MSG_CHECK_TIME = 104;
	private final static int MSG_FINISH = 105;
	private final static int FINISH_DELAY = 700;
	private final static int TIMEOUT = 15 * 1000;
	protected static final String TAG = "ANCSParser";

	private List<ANCSData> mPendingNotifcations = new LinkedList<ANCSData>();
	private Handler mHandler;

	private ANCSData mCurData;
	BluetoothGatt mGatt;

	BluetoothGattService mService;
	Context mContext;
	private static ANCSParser sInst;

	private ArrayList<OnIOSNotificationListener> mListeners=new ArrayList<OnIOSNotificationListener>();

	private ANCSParser(Context c) {
		mContext = c;
		mHandler = new Handler(c.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				if (MSG_CHECK_TIME == what) {
					if (mCurData == null) {
						return;
					}
					if (System.currentTimeMillis() >= mCurData.mTimeExpired) {
		
						Log.i(TAG,"msg timeout!");
					}
				} else if (MSG_ADD_NOTIFICATION == what) {
					mPendingNotifcations.add(new ANCSData((byte[]) msg.obj));
					mHandler.sendEmptyMessage(MSG_DO_NOTIFICATION);
				} else if (MSG_DO_NOTIFICATION == what) {
					processNotificationList();
				} else if (MSG_RESET == what) {
					mHandler.removeMessages(MSG_ADD_NOTIFICATION);
					mHandler.removeMessages(MSG_DO_NOTIFICATION);
					mHandler.removeMessages(MSG_RESET);
					mHandler.removeMessages(MSG_ERR);
					mPendingNotifcations.clear();
					mCurData = null;
		
					Log.i(TAG,"ANCSHandler reseted");
				} else if (MSG_ERR == what) {
	
					Log.i(TAG,"error,skip_cur_data");

					mCurData.clear();
					removeCurData();
					mHandler.sendEmptyMessage(MSG_DO_NOTIFICATION);
				} else if (MSG_FINISH == what) {
					Log.i(TAG,"msg data.finish()");
					if(null!=mCurData)
					mCurData.finish();
				}
			}
		};
	}
	
	public void addOnIOSNotificationListener(OnIOSNotificationListener lis){
		if(!mListeners.contains(lis)){
			mListeners.add(lis);
		}
	}

	public void removeOnIOSNotificationListener(OnIOSNotificationListener lis){
		mListeners.remove(lis);
	}

	public void removeAllListener(){
		mListeners.clear();
	}

	public void setService(BluetoothGattService bgs, BluetoothGatt bg) {
		mGatt = bg;
		mService = bgs;
	}

	public static ANCSParser getDefault(Context c) {
		if (sInst == null) {
			sInst = new ANCSParser(c);
		}
		return sInst;
	}

	public static ANCSParser get() {
		return sInst;
	}

	private void sendNotification(final IOSNotification noti) {
		Log.i(TAG,"[Add Notification] : "+noti.uid);
		for(OnIOSNotificationListener lis: mListeners){
			lis.onIOSNotificationAdd(noti);
		}
	}
	private void cancelNotification(int uid){
		Log.i(TAG,"[cancel Notification] : "+uid);
		for(OnIOSNotificationListener lis: mListeners){
			lis.onIOSNotificationRemove(uid);
		}
	}
	
	private class ANCSData {
		private long mTimeExpired;
		private int mCurStep = 0;
		private final byte[] mNotifyData; // 8 bytes
		private ByteArrayOutputStream mBout;
		private IOSNotification mNoti;

		ANCSData(byte[] data) {
			mNotifyData = data;
			mCurStep = 0;
			mTimeExpired = System.currentTimeMillis();
			mNoti =new  IOSNotification();
		}

		void clear() {
			if (mBout != null) {
				mBout.reset();
			}
			mBout = null;
			mCurStep = 0;
		}

		int getUID() {
			return (0xff & mNotifyData[7] << 24) | (0xff & mNotifyData[6] << 16)
					| (0xff & mNotifyData[5] << 8) | (0xff & mNotifyData[4]);
		}

		void finish() {
			if (null == mBout) {
				return;
			}
	
			final byte[] data = mBout.toByteArray();

			if (data.length < 5) {
				return; // 
			}
			// check if finished ?
			int cmdId = data[0]; // should be 0								//0 commandID
			if (cmdId != 0) {
				Log.i(TAG,"bad cmdId: " + cmdId);
				return;
			}
			int uid = ((0xff&data[4]) << 24) | ((0xff &data[3]) << 16)			
					| ((0xff & data[2]) << 8) | ((0xff &data[1]));
			if (uid != mCurData.getUID()) {
	
				Log.i(TAG,"bad uid: " + uid + "->" + mCurData.getUID());
				return;
			}

			// read attributes
			mNoti.uid = uid;
			int curIdx = 5; //hard code
			while (true) {
				if (mNoti.isAllInit()) {
					break; 
				}
				if (data.length < curIdx + 3) {
					return;
				}
				// attributes head
				int attrId = data[curIdx];
				int attrLen = ((data[curIdx + 1])&0xFF) | (0xFF&(data[curIdx + 2] << 8));
				curIdx += 3;
				if (data.length < curIdx + attrLen) {
					return;
				}
				String val = new String(data, curIdx, attrLen);//utf-8 encode
				if (attrId == NotificationAttributeIDTitle) { 
					mNoti.title = val;
				} else if (attrId == NotificationAttributeIDMessage) {
					mNoti.message = val;
				} else if (attrId == NotificationAttributeIDDate) { 
					mNoti.date = val;
				} else if (attrId == NotificationAttributeIDSubtitle) {
					mNoti.subtitle = val;
				} else if (attrId == NotificationAttributeIDMessageSize) {
					mNoti.messageSize = val;
				}
				curIdx += attrLen;
			}

			removeCurData();
//			mHandler.sendEmptyMessage(MSG_DO_NOTIFICATION); // continue next!
			sendNotification(mNoti);
		}
	}


	private void processNotificationList() {
		mHandler.removeMessages(MSG_DO_NOTIFICATION);

		// handle curData!
		if (mCurData == null) {
			if (mPendingNotifcations.size() == 0) {
				return;
			}

			mCurData = mPendingNotifcations.remove(0);
			Log.i(TAG,"ANCS New CurData");
		} else if (mCurData.mCurStep == 0) { // parse notify data
			do {
				if (mCurData.mNotifyData == null
						|| mCurData.mNotifyData.length != 8) {
					removeCurData(); // ignore
			
					Log.i(TAG,"ANCS Bad Head!");
					break;
				}
				if(EventIDNotificationRemoved ==mCurData.mNotifyData[0]){
					int uid=(mCurData.mNotifyData[4]&0xff) |
							(mCurData.mNotifyData[5]&0xff<<8)|
							(mCurData.mNotifyData[6]&0xff<<16)|
							(mCurData.mNotifyData[7]&0xff<<24);
					cancelNotification(uid);
					removeCurData();
					break;
				}
				if (EventIDNotificationAdded != mCurData.mNotifyData[0]) {
					removeCurData(); // ignore
					Log.i(TAG,"ANCS NOT Add!");
					break;
				}
				// get attribute if needed!
				BluetoothGattCharacteristic cha = mService	
						.getCharacteristic(GattConstant.Apple.sUUIDControl);
				if (null != cha ) {
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
	
					bout.write((byte) 0); 
			
					bout.write(mCurData.mNotifyData[4]);
					bout.write(mCurData.mNotifyData[5]);
					bout.write(mCurData.mNotifyData[6]);
					bout.write(mCurData.mNotifyData[7]);

			
					bout.write(NotificationAttributeIDTitle);
					bout.write(50);	
					bout.write(0);	
					// subtitle
					bout.write(NotificationAttributeIDSubtitle);
					bout.write(100);
					bout.write(0);

					// message 
					bout.write(NotificationAttributeIDMessage);
					bout.write(500);
					bout.write(0);

					// message size
					bout.write(NotificationAttributeIDMessageSize);
					bout.write(10);
					bout.write(0);
					// date 
					bout.write(NotificationAttributeIDDate);
					bout.write(10);
					bout.write(0);

					byte[] data = bout.toByteArray();

					cha.setValue(data);
					mGatt.writeCharacteristic(cha);
					Log.i(TAG,"request ANCS(CP) the data of Notification. = ");
					mCurData.mCurStep = 1;
					mCurData.mBout = new ByteArrayOutputStream();
					mCurData.mTimeExpired = System.currentTimeMillis() + TIMEOUT;
//					mHandler.removeMessages(MSG_CHECK_TIME);
//					mHandler.sendEmptyMessageDelayed(MSG_CHECK_TIME, TIMEOUT);
					return;
				} else {
					Log.i(TAG,"ANCS has No Control Point !");
					// has no control!// just vibrate ...
					mCurData.mBout = null;
					mCurData.mCurStep = 1;
				}

			} while (false);
		} else if (mCurData.mCurStep == 1) {
			Log.i(TAG, "mCurData.finish()");
			// check if finished!	
			mCurData.finish();
			return;
		} else {
			return;
		}
		mHandler.sendEmptyMessage(MSG_DO_NOTIFICATION); // do next step
	}


	public void onDSNotification(byte[] data) {
		if (mCurData == null) {
	
			Log.i(TAG,"got ds notify without cur data");
			return;
		}
		try {
			mHandler.removeMessages(MSG_FINISH);
			mCurData.mBout.write(data);
			mHandler.sendEmptyMessageDelayed(MSG_FINISH, FINISH_DELAY);
		} catch (IOException e) {
			Log.i(TAG,e.toString());
		}
	}

	private void removeCurData(){
		mPendingNotifcations.clear();
		mCurData = null;
	}

	void onWrite(BluetoothGattCharacteristic characteristic, int status) {
		if (status != BluetoothGatt.GATT_SUCCESS) {
			Log.i(TAG,"write err: " + status);
			mHandler.sendEmptyMessage(MSG_ERR);
		} else {
			Log.i(TAG,"write OK");
			mHandler.sendEmptyMessage(MSG_DO_NOTIFICATION);
		}
	}

	public void onNotification(byte[] data) {
		if (data == null || data.length != 8) {
			Log.i(TAG,"bad ANCS notification data");
			return;
		}

		Message msg = mHandler.obtainMessage(MSG_ADD_NOTIFICATION);
		msg.obj = data;
		msg.sendToTarget();
	}

	public void reset() {
		mHandler.sendEmptyMessage(MSG_RESET);
	}
}
