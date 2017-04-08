package com.mrcoders.www.voiceassistant.functionality;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Created by mayur on 8/4/17.
 */

public class Wifi {

    private WifiManager wifiManager;
    Context context=null;

    public Wifi(Context context) {
        this.context = context;
    }

    public void change_wifi(boolean wifi)
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
