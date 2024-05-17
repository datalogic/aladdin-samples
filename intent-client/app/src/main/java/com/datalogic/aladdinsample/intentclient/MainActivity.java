package com.datalogic.aladdinsample.intentclient;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {
    private BarCodeReceiver barCodeReceiver;
    private ScannerStateReceiver scannerStateReceiver;
    private Button btnRegisterAladdin, btnGetLatestScan, btnGetScannerState, btnUnregisterAladdin;
    String path = "";
    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor editor;

    public static final String TAG = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        path = String.valueOf(getApplicationContext().getFilesDir());
        Context context = getApplicationContext();
        sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        initializeControls();
    }

    private void initializeControls() {
        btnRegisterAladdin = findViewById(R.id.btn_register_aladdin);
        btnGetLatestScan = findViewById(R.id.btn_get_latest_bar_code);
        btnGetScannerState = findViewById(R.id.btn_get_scanner_status);
        btnUnregisterAladdin = findViewById(R.id.btn_unregister_aladdin);

        btnRegisterAladdin.setEnabled(true);
        btnUnregisterAladdin.setEnabled(false);
        btnGetLatestScan.setEnabled(false);
        btnGetScannerState.setEnabled(false);

        btnRegisterAladdin.setOnClickListener(view -> {
            btnRegisterAladdin.setEnabled(false);
            btnUnregisterAladdin.setEnabled(true);
            showIntentDialog(view);

        });

        btnGetLatestScan.setOnClickListener(view -> {
            Intent intent = new Intent("com.datalogic.aladdinapp.intent.ACTION_GET_LATEST_SCAN_DATA");
            sendBroadcast(intent);
        });

        btnGetScannerState.setOnClickListener(view -> {
            Intent intent = new Intent("com.datalogic.aladdinapp.intent.ACTION_GET_SCANNER_STATE");
            sendBroadcast(intent);
        });

        btnUnregisterAladdin.setOnClickListener(view -> {
            if (scannerStateReceiver != null && barCodeReceiver != null) {
                btnRegisterAladdin.setEnabled(true);
                btnGetLatestScan.setEnabled(false);
                btnGetScannerState.setEnabled(false);
                btnUnregisterAladdin.setEnabled(false);
                unregisterReceiver(scannerStateReceiver);
                unregisterReceiver(barCodeReceiver);
                Toast.makeText(MainActivity.this, "Unregistered from Aladdin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void showIntentDialog(View v) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_intent_settings, null);
        Dialog barcodeDialog = new Dialog(this, R.style.CustomAlertDialog);

        barcodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        barcodeDialog.setCancelable(false);
        barcodeDialog.setContentView(popupView);

        final CustomEditText action = popupView.findViewById(R.id.et_intent_action);
        final CustomEditText data = popupView.findViewById(R.id.et_intent_extra_data);
        final CustomEditText type = popupView.findViewById(R.id.et_intent_extra_type);
        final CustomEditText scanTime = popupView.findViewById(R.id.et_intent_extra_scantime);
        final Button save = popupView.findViewById(R.id.save);

        save.setOnClickListener(view -> {
            String actionEdtTxt = action.getText().toString().trim();
            String dataEdtTxt = data.getText().toString().trim();
            String typeEdtTxt = type.getText().toString().trim();
            String scanTimeEdtTxt = scanTime.getText().toString().trim();

            if (actionEdtTxt.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter barcode action", Toast.LENGTH_SHORT).show();
            } else if (dataEdtTxt.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter barcode extra data", Toast.LENGTH_SHORT).show();
            } else if (typeEdtTxt.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter barcode extra type", Toast.LENGTH_SHORT).show();
            } else if (scanTimeEdtTxt.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter barcode extra scantime", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(MainActivity.TAG, actionEdtTxt + "," + dataEdtTxt + "," + typeEdtTxt + "," + scanTimeEdtTxt + "," + System.currentTimeMillis());
                //inserting data into SP
                editor.putString("action", actionEdtTxt);
                editor.putString("data", dataEdtTxt);
                editor.putString("type", typeEdtTxt);
                editor.putString("scantime", scanTimeEdtTxt);
                editor.apply();

                barCodeReceiver = new BarCodeReceiver();
                scannerStateReceiver = new ScannerStateReceiver();

                IntentFilter barCodeIntent = new IntentFilter(actionEdtTxt);
                registerReceiver(barCodeReceiver, barCodeIntent);

                IntentFilter scannerIntent = new IntentFilter("com.datalogic.aladdinapp.intent.ACTION_SEND_SCANNER_STATE");
                registerReceiver(scannerStateReceiver, scannerIntent);

                Toast.makeText(MainActivity.this, "Registered for Aladdin", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent("com.datalogic.aladdinapp.intent.ACTION_GET_SCANNER_STATE");
                sendBroadcast(intent);

                barcodeDialog.dismiss();

            }
        });

        action.setDrawableClickListener(new DrawableClickListener() {
            public void onClick(DrawablePosition target) {
                switch (target) {
                    case RIGHT: {
                        action.setText("");
                    }
                    break;
                    default:
                        break;
                }
            }
        });

        data.setDrawableClickListener(new DrawableClickListener() {
            public void onClick(DrawablePosition target) {
                switch (target) {
                    case RIGHT: {
                        data.setText("");
                    }
                    break;
                    default:
                        break;
                }
            }
        });

        type.setDrawableClickListener(new DrawableClickListener() {
            public void onClick(DrawablePosition target) {
                switch (target) {
                    case RIGHT: {
                        type.setText("");
                    }
                    break;
                    default:
                        break;
                }
            }
        });

        scanTime.setDrawableClickListener(new DrawableClickListener() {
            public void onClick(DrawablePosition target) {
                switch (target) {
                    case RIGHT: {
                        scanTime.setText("");
                    }
                    break;
                    default:
                        break;
                }
            }
        });
        barcodeDialog.show();
    }

    class ScannerStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.datalogic.aladdinapp.intent.ACTION_SEND_SCANNER_STATE".equals(intent.getAction())) {
                if (intent.getBooleanExtra("com.datalogic.aladdinapp.extra.SCANNER_STATE", false)) {
                    Toast.makeText(context, "Connected to scanner", Toast.LENGTH_SHORT).show();
                    btnGetLatestScan.setEnabled(true);
                    btnGetScannerState.setEnabled(true);
                    btnUnregisterAladdin.setEnabled(true);
                } else {
                    Toast.makeText(context, "Disconnected from scanner", Toast.LENGTH_SHORT).show();
                    btnRegisterAladdin.setEnabled(true);
                    btnGetLatestScan.setEnabled(false);
                    btnGetScannerState.setEnabled(false);
                    btnUnregisterAladdin.setEnabled(false);
                    unregisterReceiver(scannerStateReceiver);
                    unregisterReceiver(barCodeReceiver);
                }
            }
        }
    }
}