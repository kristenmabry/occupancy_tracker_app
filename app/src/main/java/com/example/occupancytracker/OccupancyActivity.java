package com.example.occupancytracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.occupancytracker.databinding.ActivityOccupancyBinding;

import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.ble.observer.ConnectionObserver;

public class OccupancyActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "no.nordicsemi.android.blinky.EXTRA_DEVICE";

    private OccupancyViewModel viewModel;
    private ActivityOccupancyBinding binding;
    private Float ceilingHeight = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOccupancyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.initToolbar();

        final Intent intent = getIntent();
        final BluetoothDevice device = BluetoothUtils.getSelectedDevice(this);

//        if (BluetoothUtils.isAddressValid(this)) {
//            final String deviceAddress = BluetoothUtils.getSelectedAddress(this);
//            final String deviceName = device.getName();
//        }

        // Configure the view model.
        viewModel = new ViewModelProvider(this).get(OccupancyViewModel.class);
//        runOnUiThread(new Runnable() {
//              public void run() {
        viewModel.connect(device);
        viewModel.getOccupancyState().observe(OccupancyActivity.this, total -> binding.occupancyNumber.setText(total.toString()));
        viewModel.getCeilingHeightState().observe(this, height -> ceilingHeight = (float) (height / 1000.0));
//              }
//          });

        // Set up views.
//        binding.ledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.setLedState(isChecked));
//        binding.infoNotSupported.actionRetry.setOnClickListener(v -> viewModel.reconnect());
//        binding.infoTimeout.actionRetry.setOnClickListener(v -> viewModel.reconnect());

//        viewModel.getConnectionState().observe(this, state -> {
//            switch (state.getState()) {
//                case CONNECTING:
//                    binding.progressContainer.setVisibility(View.VISIBLE);
//                    binding.infoNotSupported.container.setVisibility(View.GONE);
//                    binding.infoTimeout.container.setVisibility(View.GONE);
//                    binding.connectionState.setText(R.string.state_connecting);
//                    break;
//                case INITIALIZING:
//                    binding.connectionState.setText(R.string.state_initializing);
//                    break;
//                case READY:
//                    binding.progressContainer.setVisibility(View.GONE);
//                    binding.deviceContainer.setVisibility(View.VISIBLE);
//                    onConnectionStateChanged(true);
//                    break;
//                case DISCONNECTED:
//                    if (state instanceof ConnectionState.Disconnected) {
//                        binding.deviceContainer.setVisibility(View.GONE);
//                        binding.progressContainer.setVisibility(View.GONE);
//                        final ConnectionState.Disconnected stateWithReason = (ConnectionState.Disconnected) state;
//                        if (stateWithReason.getReason() == ConnectionObserver.REASON_NOT_SUPPORTED) {
//                            binding.infoNotSupported.container.setVisibility(View.VISIBLE);
//                        } else {
//                            binding.infoTimeout.container.setVisibility(View.VISIBLE);
//                        }
//                    }
//                    // fallthrough
//                case DISCONNECTING:
//                    onConnectionStateChanged(false);
//                    break;
//            }
//        });

    }

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setTitle(getResources().getString(R.string.bluetooth_devices));
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OccupancyActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onConnectionStateChanged(final boolean connected) {
        binding.refresh.setEnabled(connected);
        if (!connected) {
            binding.batteryPercent.setText("0%");
            binding.occupancyNumber.setText("0%");
        }
    }

    public void refreshData(View view) {

    }

    public void openOptionsPopup(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options");

        LinearLayout lila1= new LinearLayout(this);
        lila1.setOrientation(LinearLayout.VERTICAL);
        final EditText ceilingHeightInput = new EditText(this);
        ceilingHeightInput.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ceilingHeightInput.setHint(getResources().getString(R.string.ceiling_height));
        if (ceilingHeight != null) {
            ceilingHeightInput.setText(ceilingHeight.toString());
        }
        final EditText resetOccupancyInput = new EditText(this);
        resetOccupancyInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        resetOccupancyInput.setHint(getResources().getString(R.string.reset_occupancy));
        lila1.addView(ceilingHeightInput);
        lila1.addView(resetOccupancyInput);
        builder.setView(lila1);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String ceilingHeight = ceilingHeightInput.getText().toString();
                if (!ceilingHeight.equals("")) {
                    viewModel.setCeilingHeight((int)(Float.parseFloat(ceilingHeight) * 1000));
                }
                final String occupancySubmit = resetOccupancyInput.getText().toString();
                if (!occupancySubmit.equals("")) {
                    viewModel.setOccupancy(Integer.parseInt(occupancySubmit));
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void exportData(View view) {

    }
}