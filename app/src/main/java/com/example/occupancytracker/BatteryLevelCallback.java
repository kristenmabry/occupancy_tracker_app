package com.example.occupancytracker;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface BatteryLevelCallback {
    void onBatteryLevelStateChanged(@NonNull final BluetoothDevice device, final Integer battery);
}
