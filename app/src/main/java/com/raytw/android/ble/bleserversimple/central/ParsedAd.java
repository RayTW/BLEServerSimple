package com.raytw.android.ble.bleserversimple.central;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by leeray on 16/3/16.
 */
public class ParsedAd {
    public byte flags;
    public String localName;
    public short manufacturer;
    public short mTXPowerLevel;
    public ArrayList<UUID> uuids = new ArrayList<UUID>();

    public String toString(){
        StringBuffer str = new StringBuffer();
        str.append("\n---------begin----------");
        str.append("\nflags=" + flags);
        str.append("\nlocalName=" + localName);
        str.append("\nmanufacturer=" + manufacturer);
        str.append("\nmTXPowerLevel=" + mTXPowerLevel);
        str.append("\nuuids=" + uuids);
        str.append("\n---------end----------");

        return str.toString();
    }
}
