package com.burns.android.ancssample.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ray.lee on 2016/11/8.
 */
public class Utility {

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static void showText(Context context, CharSequence text, int duration){
        Toast.makeText(context, text, duration).show();
    }
}
