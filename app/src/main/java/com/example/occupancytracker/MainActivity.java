package com.example.occupancytracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void viewOccupancy(View view) {
        Intent intent = new Intent(this, OccupancyActivity.class);
        startActivity(intent);
    }

    public void viewData(View view) {
//        Intent intent = new Intent(this, ViewLayoutsActivity.class);
//        startActivity(intent);
    }

    public void viewDevices(View view) {
//        Intent intent = new Intent(this, ViewBluetoothDevices.class);
//        startActivity(intent);
    }
}