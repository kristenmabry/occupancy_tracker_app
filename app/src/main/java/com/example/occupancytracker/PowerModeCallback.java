package com.example.occupancytracker;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface PowerModeCallback {

    /**
     * Called when a button was pressed or released on device.
     *
     * @param device the target device.
     * @param newPowerMode the new power mode of the device
     */
    void onPowerModeStateChanged(@NonNull final BluetoothDevice device, final Boolean newPowerMode);
}
