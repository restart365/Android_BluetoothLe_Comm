package com.dtiguardian.bletest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_ENABLE_BT = 1;

    private ListView lvDevices;
    private Button btnScan, btnSend;
    private TextView tvStatus;
    private EditText etInput;

    private BleHelper bleHelper;

    private final Context ctx = this;

    private String status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        lvDevices = findViewById(R.id.lvDevices);
        btnScan = findViewById(R.id.btnScan);
        btnSend = findViewById(R.id.btnSend);
        tvStatus = findViewById(R.id.tvStatus);
        etInput = findViewById(R.id.etInput);

        tvStatus.setMovementMethod(new ScrollingMovementMethod());

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        bleHelper = new BleHelper(ctx, bluetoothAdapter);


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!bleHelper.isScanning()) {
                    updateStatus("Scan started");
                    bleHelper.scanLeDevice(new FinishScanCallback() {
                        @Override
                        public void onFinishScan(List<BleDevice> devices) {
                            updateStatus("Scan finished");
                            updateUI(devices);
                        }
                    });
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bleHelper.writeToBle(etInput.getText().toString().trim() + "\r");
            }
        });

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(bleHelper.isScanning())
                    bleHelper.stopScan(new FinishScanCallback() {
                        @Override
                        public void onFinishScan(List<BleDevice> devices) {
                            updateStatus("Scan finished");
                        }
                    });

                updateStatus("Connecting to " + ((BleDevice)adapterView.getItemAtPosition(i)).getName());

                bleHelper.connectBluetoothLeDevice((BleDevice) adapterView.getItemAtPosition(i), new ConnectionCallback() {
                    @Override
                    public void onFinishConnect(boolean connected) {
                        if (connected)
                            updateStatus("Connected");
                        else
                            updateStatus("Failed");
                    }

                    @Override
                    public void onReadWrite(String msg) {
                        updateStatus(msg);
                    }
                });
            }
        });
    }



    private void updateUI(final List<BleDevice> devices){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO: update list view
                DeviceListAdapter adapter = new DeviceListAdapter(ctx, R.layout.list_item, devices);
                lvDevices.setAdapter(adapter);
            }
        });
    }

    private void updateStatus(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status = status + msg + "\n";
                tvStatus.setText(status);
            }
        });
    }






}
