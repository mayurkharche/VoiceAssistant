package com.mrcoders.www.voiceassistant.functionality;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.mrcoders.www.voiceassistant.activity.TalkActivity;

/**
 * Created by rohit on 5/4/17.
 */

public class BatteryStatus{

    private String batLevel = null;
    private Context context = null;

    public BatteryStatus(Context context) {
        this.context = context;
    }

    public int batteryLevel()
    {
        /*
        final BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                int level = -1;
                if (rawlevel >= 0 && scale > 0) {
                    level = (rawlevel * 100) / scale;
                }
                batLevel = String.valueOf(level);
                Toast.makeText(context,batLevel,Toast.LENGTH_LONG).show();
            }
        };

        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(batteryLevelReceiver, batteryLevelFilter);
        */

        Intent battery=context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int rawlevel = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int level = -1;
        if (rawlevel >= 0 && scale > 0) {
            level = (rawlevel * 100) / scale;
        }
        batLevel = String.valueOf(level);

        //Toast.makeText(context, ""+batLevel, Toast.LENGTH_SHORT).show();

        return level;
    }
}
