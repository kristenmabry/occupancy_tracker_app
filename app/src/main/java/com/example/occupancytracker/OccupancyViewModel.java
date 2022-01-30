package com.example.occupancytracker;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import no.nordicsemi.android.ble.ConnectRequest;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;

public class OccupancyViewModel extends AndroidViewModel {
    private final OccupancyManager occupancyManager;
    private BluetoothDevice device;
    @Nullable
    private ConnectRequest connectRequest;

    public OccupancyViewModel(@NonNull final Application application) {
        super(application);

        // Initialize the manager.
        occupancyManager = new OccupancyManager(getApplication());
    }

    public LiveData<ConnectionState> getConnectionState() {
        return occupancyManager.state;
    }

    public LiveData<Integer> getOccupancyState() {
        return occupancyManager.getOccupancyState();
    }

    /**
     * Connect to the given peripheral.
     *
     * @param target the target device.
     */
    public void connect(@NonNull final DiscoveredBluetoothDevice target) {
        // Prevent from calling again when called again (screen orientation changed).
        if (device == null) {
            device = target.getDevice();
            reconnect();
        }
    }

    /**
     * Reconnects to previously connected device.
     * If this device was not supported, its services were cleared on disconnection, so
     * reconnection may help.
     */
    public void reconnect() {
        if (device != null) {
            connectRequest = occupancyManager.connect(device)
                    .retry(3, 100)
                    .useAutoConnect(false)
                    .then(d -> connectRequest = null);
            connectRequest.enqueue();
        }
    }

    /**
     * Disconnect from peripheral.
     */
    private void disconnect() {
        device = null;
        if (connectRequest != null) {
            connectRequest.cancelPendingConnection();
        } else if (occupancyManager.isConnected()) {
            occupancyManager.disconnect().enqueue();
        }
    }

//    /**
//     * Sends a command to turn ON or OFF the LED on the nRF5 DK.
//     *
//     * @param on true to turn the LED on, false to turn it OFF.
//     */
//    public void setLedState(final boolean on) {
//        occupancyManager.turnLed(on);
//    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disconnect();
    }
}
