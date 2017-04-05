package com.mrcoders.www.voiceassistant.functionality;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by rohit on 5/4/17.
 */

public class BatteryStatus extends AppCompatActivity {

    private void batteryLevel()
    {
        BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                int level = -1;
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                Toast.makeText(BatteryStatus.this,String.valueOf(level),Toast.LENGTH_LONG).show();
            }
        };
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }
}
