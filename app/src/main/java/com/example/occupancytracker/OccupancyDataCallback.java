package com.example.occupancytracker;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public abstract class OccupancyDataCallback implements ProfileDataCallback, DataSentCallback, OccupancyCallback {

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        parse(device, data);
    }

    @Override
    public void onDataSent(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        parse(device, data);
    }

    public void parse(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 2) {
            onInvalidDataReceived(device, data);
            return;
        }

        byte[] dataBytes = data.getValue();
        byte[] array = { 0x00, 0x00, dataBytes[0], dataBytes[1] };
        final Integer occupancy = ByteBuffer.wrap(array).getInt();
        onOccupancyStateChanged(device, occupancy);
    }
}
