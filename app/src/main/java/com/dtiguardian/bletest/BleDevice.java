package com.dtiguardian.bletest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.os.ParcelUuid;

import java.util.List;

public class BleDevice{
    private String name;
    private List<ParcelUuid> uuids;
    private BluetoothDevice device;

    public BleDevice(BluetoothDevice device, ScanRecord record){
        name = record.getDeviceName();
        uuids = record.getServiceUuids();
        this.device = device;
    }

    public String getName() {
        if(name != null)
            return name;
        return "";
    }

    public List<ParcelUuid> getUuids() {
        return uuids;
    }

    public ParcelUuid getUuid(){
        return getUuid(0);
    }

    public ParcelUuid getUuid(int i){
        if(uuids == null || i<0 || uuids.size() < i+1){
            return null;
        }
        return uuids.get(i);
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
