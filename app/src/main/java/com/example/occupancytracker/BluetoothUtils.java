package com.example.occupancytracker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class BluetoothUtils {
    public static final String NO_ADDRESS = "no_address";

    public static String getSelectedAddress(Context c) {
        SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(c.getString(R.string.save_selected_device_key), BluetoothUtils.NO_ADDRESS);
    }

    public static boolean isAddressValid(Context c) {
        String address = getSelectedAddress(c);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return !(address.equals(NO_ADDRESS) || !BluetoothAdapter.checkBluetoothAddress(address) || !bluetoothAdapter.isEnabled());
    }

    public static BluetoothDevice[] getAllDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        return pairedDevices.toArray(new BluetoothDevice[pairedDevices.size()]);
    }
}
