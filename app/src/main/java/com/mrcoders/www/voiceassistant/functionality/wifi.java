package com.mrcoders.www.voiceassistant.functionality;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Created by rohit on 5/4/17.
 */

public class wifi {

    private WifiManager wifiManager;
    Context context=null;

    public wifi(Context context) {
        this.context = context;
    }

    private void change_wifi(boolean wifi)
    {
        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        if(wifi==true)
        {
            if(!wifiManager.isWifiEnabled()){
                wifiManager.setWifiEnabled(true);
            }else{
                //  wifiManager.setWifiEnabled(true);
            }
        }
        else if(wifi == false){
            if(wifiManager.isWifiEnabled()){
                wifiManager.setWifiEnabled(false);
            }else{
                //  wifiManager.setWifiEnabled(true);
            }
        }

    }
}
