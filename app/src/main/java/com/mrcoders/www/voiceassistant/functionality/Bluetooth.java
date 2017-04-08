package com.mrcoders.www.voiceassistant.functionality;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by mayur on 8/4/17.
 */

public class Bluetooth {

    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

}
