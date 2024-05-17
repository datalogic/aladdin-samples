package com.datalogic.aladdinsample.intentclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BarCodeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action != null && !action.isEmpty()) {
            Log.d(MainActivity.TAG, "BarCodeReceiver Data: " + MainActivity.sharedPref.getString("action", "") + ", Label type: " +
                    MainActivity.sharedPref.getString("type", "") + ", Scan time: " +
                    MainActivity.sharedPref.getString("scantime", ""));
            try {
                String data = intent.getStringExtra(MainActivity.sharedPref.getString("data", ""));
                String codeType = intent.getStringExtra(MainActivity.sharedPref.getString("type", ""));
                long scanTimeValue = intent.getLongExtra(MainActivity.sharedPref.getString("scantime", ""), 0L);

                Date date = new Date(scanTimeValue);
                String scanTimeFormatted = new SimpleDateFormat("MMM dd, hh:mm a").format(date);

                if (data != null && data.isEmpty()) {
                    Toast.makeText(context, "No barcode has scanned yet", Toast.LENGTH_SHORT).show();
                } else if (data == null || codeType == null || scanTimeValue == 0) {
                    Toast.makeText(context, "Extra names should be same as Aladdin application. please check again.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Data:" + data + " Label Type:" + codeType + " Scan Time:" + scanTimeFormatted, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(MainActivity.TAG,"BarCodeReceiver Exception " + e.getMessage());
            }

        }

    }

}





