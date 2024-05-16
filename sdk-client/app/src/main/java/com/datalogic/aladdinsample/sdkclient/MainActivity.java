package com.datalogic.aladdinsample.sdkclient;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.datalogic.aladdinsdk.AlManager;
import com.datalogic.aladdinsdk.interfaces.IScannerOutput;
import com.datalogic.aladdinsdk.interfaces.IServiceOutput;
import com.datalogic.aladdinsdk.model.BarcodeModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements IScannerOutput, IServiceOutput {

    private AlManager alManager;
    private final String tag = "Aladdin_SDK_Client";

    private Button connectToServiceButton;
    private Button disconnectFromServiceButton;
    private Button subscribeToServiceEventsButton;
    private Button unsubscribeFromServiceEventsButton;
    private Button subscribeToScansButton;
    private Button unsubscribeFromScansButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        alManager = new AlManager();
        connectToServiceButton = findViewById(R.id.connectToService);
        disconnectFromServiceButton = findViewById(R.id.disconnectFromService);
        subscribeToServiceEventsButton = findViewById(R.id.subscribeToServiceEvents);
        unsubscribeFromServiceEventsButton = findViewById(R.id.unsubscribeFromServiceEvents);
        subscribeToScansButton = findViewById(R.id.subscribeToScans);
        unsubscribeFromScansButton = findViewById(R.id.unsubscribeFromScans);

        initializeEnableStates();
        initializeClickListeners();
    }

    private void initializeEnableStates() {
        connectToServiceButton.setEnabled(true);
        disconnectFromServiceButton.setEnabled(false);
        subscribeToServiceEventsButton.setEnabled(true);
        unsubscribeFromServiceEventsButton.setEnabled(false);
        subscribeToScansButton.setEnabled(true);
        unsubscribeFromScansButton.setEnabled(false);
    }
    private void initializeClickListeners() {
        findViewById(R.id.get_latest_scan_value).setOnClickListener(view -> {
            Log.d(tag, "get_latest_scan_value");
            if (alManager.isConnectedToService())
                Toast.makeText(MainActivity.this, alManager.getLatestBarcodeData(), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "Not connected to aladdin app", Toast.LENGTH_SHORT).show();
        });

        connectToServiceButton.setOnClickListener(view -> {
            alManager.subscribeToServiceEvents(MainActivity.this);
            boolean connection = alManager.connectToService(MainActivity.this);
            if (connection)
                Log.d(tag, "connect to service true");
            else
                Log.d(tag, "connect to service false");

        });

        findViewById(R.id.isConnectedToScanner).setOnClickListener(view -> {
            if (alManager.isConnectedToService()) {
                Log.d(tag, "isConnectedToScanner");
                boolean connection = alManager.isConnectedToScanner();
                if (connection)
                    Toast.makeText(MainActivity.this, "Connected to Scanner", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "not connected to Scanner", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(MainActivity.this, "not connected to service", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.isConnectedToService).setOnClickListener(view -> {
            Log.d(tag, "isConnectedToService");
            boolean connection = alManager.isConnectedToService();
            if (connection)
                Toast.makeText(MainActivity.this, "Connected to service", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "not connected to service", Toast.LENGTH_SHORT).show();
        });

        subscribeToScansButton.setOnClickListener(view -> {
            Log.d(tag, "subscribeToScans init");
            if (alManager.isConnectedToService()) {
                alManager.subscribeToScans(MainActivity.this);
                Toast.makeText(MainActivity.this, "Subscribed to scans", Toast.LENGTH_SHORT).show();
                Log.d(tag, "subscribeToScans success");
                subscribeToScansButton.setEnabled(false);
                unsubscribeFromScansButton.setEnabled(true);
            } else
                Toast.makeText(MainActivity.this, "not connected to service", Toast.LENGTH_SHORT).show();

        });

        subscribeToServiceEventsButton.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Subscribed to service events", Toast.LENGTH_SHORT).show();
            alManager.subscribeToServiceEvents(MainActivity.this);
            Log.d(tag, "subscribeToServiceEvents");
            subscribeToServiceEventsButton.setEnabled(false);
            unsubscribeFromServiceEventsButton.setEnabled(true);
        });

        unsubscribeFromScansButton.setOnClickListener(view -> {
            Log.d(tag, "before unsubscribe");
            if (alManager.isConnectedToService()) {
                Log.d(tag, "before unsubscribe enter");
                alManager.unsubscribeFromScans();
                Log.d(tag, "before unsubscribe exit");
                Toast.makeText(MainActivity.this, "Unsubscribed from scans", Toast.LENGTH_SHORT).show();
                subscribeToScansButton.setEnabled(true);
                unsubscribeFromScansButton.setEnabled(false);
            } else
                Toast.makeText(MainActivity.this, "not connected to service", Toast.LENGTH_SHORT).show();
        });

        unsubscribeFromServiceEventsButton.setOnClickListener(view -> {
            if (alManager.isConnectedToService()) {
                Log.d(tag, "unsubscribeFromServiceEvents");
                alManager.unsubscribeFromServiceEvents();
                Toast.makeText(MainActivity.this, "Unsubscribed from service events", Toast.LENGTH_SHORT).show();
                Log.d(tag, "unsubscribeFromServiceEvents");
                subscribeToServiceEventsButton.setEnabled(true);
                unsubscribeFromServiceEventsButton.setEnabled(false);
            } else
                Toast.makeText(MainActivity.this, "not connected to service", Toast.LENGTH_SHORT).show();
        });

        disconnectFromServiceButton.setOnClickListener(view -> {
            Log.d(tag, "disconnectFromService");
            try {
                Toast.makeText(MainActivity.this, "Disconnected from service", Toast.LENGTH_SHORT).show();
                alManager.disconnectFromService();
                connectToServiceButton.setEnabled(true);
                disconnectFromServiceButton.setEnabled(false);
                subscribeToServiceEventsButton.setEnabled(true);
                unsubscribeFromServiceEventsButton.setEnabled(false);
                subscribeToScansButton.setEnabled(true);
                unsubscribeFromScansButton.setEnabled(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void onScannerConnected() {
        Toast.makeText(MainActivity.this, "Connected to scanner", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScannerDisconnected() {
        Toast.makeText(MainActivity.this, "Disconnected from scanner", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceConnected() {
        Toast.makeText(MainActivity.this, "Connected to service", Toast.LENGTH_SHORT).show();
        connectToServiceButton.setEnabled(false);
        disconnectFromServiceButton.setEnabled(true);
    }

    @Override
    public void onServiceDisconnected() {
        connectToServiceButton.setEnabled(true);
        disconnectFromServiceButton.setEnabled(false);
        Toast.makeText(MainActivity.this, "Disconnected from service", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBarcodeScanned(@Nullable BarcodeModel barcodeModel) {
        if (barcodeModel != null) {

            Toast.makeText(MainActivity.this, "Data : " + barcodeModel.getBarcode() + ", Scan Type : " + barcodeModel.getCode() + ", Time : " +
                    barcodeModel.getScanTime(), Toast.LENGTH_SHORT).show();
            Log.d(tag, "barcode time: " + barcodeModel.getScanTime());
            Log.d(tag, "instant time: " + Calendar.getInstance().getTimeInMillis());
            Log.d(tag, "time: " + new SimpleDateFormat().format(barcodeModel.getScanTime()));

        }
    }
}