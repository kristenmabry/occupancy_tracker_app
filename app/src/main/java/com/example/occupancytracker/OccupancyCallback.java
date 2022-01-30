package com.example.occupancytracker;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface OccupancyCallback {

    /**
     * Called when a button was pressed or released on device.
     *
     * @param device the target device.
     * @param total the new occupancy total
     */
    void onOccupancyStateChanged(@NonNull final BluetoothDevice device, final Integer total);
}
