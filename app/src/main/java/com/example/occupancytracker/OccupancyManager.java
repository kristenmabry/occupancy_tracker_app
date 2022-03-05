package com.example.occupancytracker;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.livedata.ObservableBleManager;

public class OccupancyManager extends ObservableBleManager {
    /** Occupancy Tracker Service UUID. */
    public final static UUID LBS_UUID_SERVICE = UUID.fromString("9fed1400-fc85-41c0-be7b-0c6ec45d960e");
    /** Occupancy Number characteristic UUID. */
    private final static UUID LBS_UUID_OCCUPANCY_INT = UUID.fromString("9fed1401-fc85-41c0-be7b-0c6ec45d960e");
    /** Ceiling Height characteristic UUID. */
    private final static UUID LBS_UUID_HEIGHT_INT = UUID.fromString("9fed1402-fc85-41c0-be7b-0c6ec45d960e");

    private final MutableLiveData<Integer> occupancyState = new MutableLiveData<>();
    private final MutableLiveData<Integer> ceilingHeightState = new MutableLiveData<>();

    private BluetoothGattCharacteristic occupancyCharacteristic;
    private BluetoothGattCharacteristic ceilingHeightCharacteristic;
    private boolean supported;
    private Integer ceilingHeight;

    public OccupancyManager(@NonNull final Context context) {
        super(context);
    }

    public final LiveData<Integer> getOccupancyState() {
        return occupancyState;
    }
    public final LiveData<Integer> getCeilingHeightState() {
        return ceilingHeightState;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new OccupancyBleManagerGattCallback();
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        if (BuildConfig.DEBUG) {
            Log.println(priority, "OccupancyManager", message);
        }
    }

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return !supported;
    }

    /**
     * The Button callback will be notified when a notification from Button characteristic
     * has been received, or its data was read.
     * <p>
     * If the data received are valid (single byte equal to 0x00 or 0x01), the
     * {@link OccupancyDataCallback#onOccupancyStateChanged} will be called.
     * Otherwise, the {@link OccupancyDataCallback#onInvalidDataReceived(BluetoothDevice, Data)}
     * will be called with the data received.
     */
    private	final OccupancyDataCallback occupancyCallback = new OccupancyDataCallback() {
        @Override
        public void onOccupancyStateChanged(@NonNull final BluetoothDevice device,
                                         final Integer total) {
            log(Log.VERBOSE, "Occupancy: " + total.toString());
            occupancyState.setValue(total);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            log(Log.WARN, "Invalid data received: " + data);
        }
    };

    private final OccupancyHeightDataCallback ceilingHeightCallback = new OccupancyHeightDataCallback() {
        @Override
        public void onCeilingHeightStateChanged(@NonNull final BluetoothDevice device,
                                      final Integer height) {
            ceilingHeight = height;
            log(Log.VERBOSE, "Ceiling height: " + height.toString() + " m");
            ceilingHeightState.setValue(height);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            // Data can only invalid if we read them. We assume the app always sends correct data.
            log(Log.WARN, "Invalid data received: " + data);
        }
    };

    /**
     * BluetoothGatt callbacks object.
     */
    private class OccupancyBleManagerGattCallback extends BleManagerGattCallback {

        @Override
        protected void initialize() {
            setNotificationCallback(occupancyCharacteristic).with(occupancyCallback);
            readCharacteristic(occupancyCharacteristic).with(occupancyCallback).enqueue();
            enableNotifications(occupancyCharacteristic).enqueue();
        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(LBS_UUID_SERVICE);
            if (service != null) {
                occupancyCharacteristic = service.getCharacteristic(LBS_UUID_OCCUPANCY_INT);
                ceilingHeightCharacteristic = service.getCharacteristic(LBS_UUID_HEIGHT_INT);
            }

            boolean writeRequest = false;
            if (ceilingHeightCharacteristic != null) {
                final int heightProperties = ceilingHeightCharacteristic.getProperties();
                writeRequest = (heightProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
            }

            supported = occupancyCharacteristic != null && writeRequest;
            supported = occupancyCharacteristic != null;
            return supported;
        }

        @Override
        protected void onServicesInvalidated() {
            occupancyCharacteristic = null;
        }
    }

    public void setCeilingHeight(final Integer height) {
        // Are we connected?
        if (ceilingHeightCharacteristic == null)
            return;

        // No need to change?
        if (ceilingHeight == height)
            return;

        log(Log.VERBOSE, "Setting height to " + height.toString() + "...");
        byte[] array = new byte[] {
                (byte)((height >> 8) & 0xff),
                (byte)((height >> 0) & 0xff),
        };
        writeCharacteristic(
                ceilingHeightCharacteristic,
                array,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).with(ceilingHeightCallback).enqueue();
    }

    public void setOccupancy(final Integer newOccupancy) {
        // Are we connected?
        if (occupancyCharacteristic == null)
            return;

        log(Log.VERBOSE, "Setting occupancy to " + newOccupancy.toString() + "...");
        writeCharacteristic(
                occupancyCharacteristic,
                Data.from(newOccupancy.toString()),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        ).with(occupancyCallback).enqueue();
    }

}
