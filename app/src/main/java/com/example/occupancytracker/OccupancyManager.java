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
    /** LED characteristic UUID. */
    private final static UUID LBS_UUID_LED_CHAR = UUID.fromString("00001525-1212-efde-1523-785feabcd123");

    private final MutableLiveData<Integer> occupancyState = new MutableLiveData<>();

    private BluetoothGattCharacteristic occupancyCharacteristic;
    private boolean supported;
    private boolean ledOn;

    public OccupancyManager(@NonNull final Context context) {
        super(context);
    }

    public final LiveData<Integer> getOccupancyState() {
        return occupancyState;
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
            Log.i("OccupancyDataCallback", "Occupancy: " + total.toString());
            occupancyState.setValue(total);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
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
            }

//            boolean writeRequest = false;
//            if (ledCharacteristic != null) {
//                final int ledProperties = ledCharacteristic.getProperties();
//                writeRequest = (ledProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
//            }

//            supported = occupancyCharacteristic != null && writeRequest;
            supported = occupancyCharacteristic != null;
            return supported;
        }

        @Override
        protected void onServicesInvalidated() {
            occupancyCharacteristic = null;
        }
    }

//    /**
//     * Sends a request to the device to turn the LED on or off.
//     *
//     * @param on true to turn the LED on, false to turn it off.
//     */
//    public void turnLed(final boolean on) {
//        // Are we connected?
//        if (ledCharacteristic == null)
//            return;
//
//        // No need to change?
//        if (ledOn == on)
//            return;
//
//        log(Log.VERBOSE, "Turning LED " + (on ? "ON" : "OFF") + "...");
//        writeCharacteristic(
//                ledCharacteristic,
//                BlinkyLED.turn(on),
//                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
//        ).with(ledCallback).enqueue();
//    }

}
