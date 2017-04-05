package com.mrcoders.www.voiceassistant.functionality;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by rohit on 5/4/17.
 */

public class LaunchApp extends AppCompatActivity {

    private void launch_app(String name)
    {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(name);
        if (launchIntent != null) {
            startActivity(launchIntent);
        }
    }
}
