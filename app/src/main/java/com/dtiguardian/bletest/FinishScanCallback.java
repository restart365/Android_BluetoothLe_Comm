package com.dtiguardian.bletest;

import android.bluetooth.le.ScanResult;

import java.util.List;

public abstract class FinishScanCallback{
    public void onFinishScan(List<BleDevice> devices) {
        throw new RuntimeException("Stub!");
    }
}
