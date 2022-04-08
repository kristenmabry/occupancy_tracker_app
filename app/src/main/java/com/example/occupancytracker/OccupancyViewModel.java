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
    public LiveData<Integer> getCeilingHeightState() {
        return occupancyManager.getCeilingHeightState();
    }
    public final LiveData<Integer> getBatteryLevelState() {
        return occupancyManager.getBatteryLevelState();
    }

    /**
     * Connect to the given peripheral.
     *
     * @param target the target device.
     */
    public void connect(@NonNull final BluetoothDevice target) {
        // Prevent from calling again when called again (screen orientation changed).
        if (device == null) {
            device = target;
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
    public void disconnect() {
        device = null;
        if (connectRequest != null) {
            connectRequest.cancelPendingConnection();
        } else if (occupancyManager.isConnected()) {
            occupancyManager.disconnect().enqueue();
        }
    }

    public void setCeilingHeight(final Integer height) {
        occupancyManager.setCeilingHeight(height);
    }

    public void setOccupancy(final Integer occupancy) {
        occupancyManager.setOccupancy(occupancy);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disconnect();
    }

    public void getBatteryLevel() {
        occupancyManager.getBatteryLevel();
    }
}
