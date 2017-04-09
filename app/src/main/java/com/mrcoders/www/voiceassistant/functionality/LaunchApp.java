package com.mrcoders.www.voiceassistant.functionality;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

/**
 * Created by rohit on 5/4/17.
 */

public class LaunchApp{

    private static final String TAG = "LaunchApp";

    private static Context context;
    private ApplicationInfo applicationinfo;

    public LaunchApp(Context context) {
        this.context = context;
    }

    public static boolean launchApp(String name)
    {
        String pack = getPackage(name);
        if(pack == null)return false;

        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(pack);
        if (launchIntent != null) {
            context.startActivity(launchIntent);
        }

        return true;
    }

    public static String getPackage(String appName){

        String pack=null;

        final PackageManager pm = context.getPackageManager();

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {

            Log.d(TAG, "Installed package :" + packageInfo.packageName);
            if(packageInfo.packageName.toLowerCase().contains(appName.toLowerCase())){
                Log.d(TAG,"Launch Application here.................");
                pack = packageInfo.packageName;
                break;
            }
        }

        return pack;
    }
}
