package com.example.occupancytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT <= 30) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN }, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(MainActivity.this, "Bluetooth permission granted", Toast.LENGTH_SHORT) .show();
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
        else {
            Toast.makeText(MainActivity.this, "Bluetooth permission denied", Toast.LENGTH_SHORT) .show();
            findViewById(R.id.devices_button).setEnabled(false);
            findViewById(R.id.occupancy_button).setEnabled(false);
        }
    }

    public void viewOccupancy(View view) {
        Intent intent = new Intent(this, OccupancyActivity.class);
        startActivity(intent);
    }

    public void viewData(View view) {
        Intent intent = new Intent(this, SavedDataActivity.class);
        startActivity(intent);
    }

    public void viewDevices(View view) {
        Intent intent = new Intent(this, DevicesActivity.class);
        startActivity(intent);
    }
}