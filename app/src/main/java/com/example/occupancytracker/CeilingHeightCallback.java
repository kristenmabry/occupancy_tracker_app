package com.example.occupancytracker;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface CeilingHeightCallback {
    void onCeilingHeightStateChanged(@NonNull final BluetoothDevice device, final Integer height);
}
