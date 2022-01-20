package com.example.occupancytracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class DevicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        this.initToolbar();
        this.populateListView();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.bluetooth_devices));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DevicesActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void populateListView() {
        BluetoothDevice[] devices = BluetoothUtils.getAllDevices();
        if (devices.length > 0) {
            findViewById(R.id.no_devices).setVisibility(View.GONE);
            findViewById(R.id.list).setVisibility(View.VISIBLE);

            RecyclerView list = findViewById(R.id.list);
            list.setHasFixedSize(true);

            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            list.setLayoutManager(layoutManager);

            BluetoothDevicesAdapter adapter = new BluetoothDevicesAdapter(this, devices);
            list.setAdapter(adapter);
        }
    }
}