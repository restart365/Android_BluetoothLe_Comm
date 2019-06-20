package com.dtiguardian.bletest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleHelper {

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final int SCAN_PERIOD = 5000;
    private static final boolean NO_AUTO_CONNECT = false;

    private UUID service_uuid;
    private UUID characteristic_uuid;

    private Handler mHandler;
    private boolean mScanning = false;
    private boolean mConnected = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mScanner;
    private BluetoothGatt mBluetoothGatt;

    private List<BleDevice> mDevices;

    private final Context ctx;

    public BleHelper(Context context, BluetoothAdapter adapter){
        mDevices = new ArrayList<>();
        mBluetoothAdapter = adapter;
        mScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mHandler = new Handler();
        ctx = context;
    }

    public void scanLeDevice(final FinishScanCallback cb){
        mDevices.clear();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!mScanning){
                    return;
                }
                mScanning = false;
                mScanner.stopScan(mScanCallBack);
                cb.onFinishScan(mDevices);
            }
        }, SCAN_PERIOD);

        mScanner.startScan(mScanCallBack);
        mScanning = true;
    }

    public void stopScan(FinishScanCallback cb){
        mScanning = false;
        mScanner.stopScan(mScanCallBack);
        cb.onFinishScan(mDevices);
    }

    public void connectBluetoothLeDevice(final BleDevice device, final ConnectionCallback cb){
        mBluetoothGatt = device.getDevice().connectGatt(ctx, NO_AUTO_CONNECT, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    gatt.discoverServices();
                } else {
                    cb.onFinishConnect(false);
                }

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if(status == BluetoothGatt.GATT_SUCCESS){
                    mConnected = true;
                    cb.onFinishConnect(true);
                    setupUuid(device.getUuid());
                    setNotification();
                } else {
                    cb.onFinishConnect(false);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                String value = new String(characteristic.getValue());
                cb.onReadWrite(gatt.getDevice().getName() + " recieved: " + value);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                gatt.readCharacteristic(characteristic);
                cb.onReadWrite(gatt.getDevice().getName() + " write successfully at " + String.valueOf(System.currentTimeMillis()));
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                String response = new String(characteristic.getValue());
                cb.onReadWrite("The response is "+ response + " at " + String.valueOf(System.currentTimeMillis()));
            }


        });
    }

    public void disconnectBluetoothLeDevice(){
        mConnected = false;
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
    }

    private void setupUuid(ParcelUuid uuid){
        service_uuid = uuid.getUuid();
        String serialNum = service_uuid.toString().substring(service_uuid.toString().length() - 4);
        String temp = "06d1e5e7-79ad-4a71-8faa-373789f7d93c";// + serialNum;
        characteristic_uuid = UUID.fromString(temp);
        //send_characteristic_uuid = UUID.fromString("06d1e5e7-79ad-4a71-8faa-373789f7d93c")
    }

    public void setNotification(){
        BluetoothGattService service = mBluetoothGatt.getService(service_uuid);
        if(service != null){
            BluetoothGattCharacteristic bluetoothGattCharacteristic = service.getCharacteristic(characteristic_uuid);
            if(bluetoothGattCharacteristic != null){
                mBluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(CCCD);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }

    public void writeToBle(String msg){
        BluetoothGattService service = mBluetoothGatt.getService(service_uuid);
        if(service != null){
            BluetoothGattCharacteristic bluetoothGattCharacteristic = service.getCharacteristic(characteristic_uuid);
            if(bluetoothGattCharacteristic != null){
                bluetoothGattCharacteristic.setValue(msg.getBytes());
                mBluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
            }
        }
    }

    private BluetoothDevice getDevice(int i){
        return mDevices.get(i).getDevice();
    }

    private boolean isInList(BleDevice device){
        for (BleDevice d : mDevices){
            if(device.getDevice().getAddress().equals(d.getDevice().getAddress())){
                return true;
            }
        }
        return false;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public boolean isScanning() {
        return mScanning;
    }

    public List<BleDevice> getDevices() {
        return mDevices;
    }

    private final ScanCallback mScanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if(result.getScanRecord() != null && result.getScanRecord().getDeviceName() != null){
                BleDevice device = new BleDevice(result.getDevice(), result.getScanRecord());

                if(!isInList(device)){
                    mDevices.add(device);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("BLE","Scan failed");
        }
    };
}
