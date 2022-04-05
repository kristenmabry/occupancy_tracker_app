package com.example.occupancytracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

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
//        binding.exportButton.setEnabled(false);
        this.initToolbar();

        final Intent intent = getIntent();
        final BluetoothDevice device = BluetoothUtils.getSelectedDevice(this);

//        if (BluetoothUtils.isAddressValid(this)) {
//            final String deviceAddress = BluetoothUtils.getSelectedAddress(this);
//            final String deviceName = device.getName();
//        }

        // Configure the view model.
        viewModel = new ViewModelProvider(this).get(OccupancyViewModel.class);
        runOnUiThread(new Runnable() {
              public void run() {
                viewModel.connect(device);
                viewModel.getOccupancyState().observe(OccupancyActivity.this, total -> binding.occupancyNumber.setText(total.toString()));
                viewModel.getCeilingHeightState().observe(OccupancyActivity.this, height -> ceilingHeight = (float) (height / 1000.0));
                viewModel.getBatteryLevelState().observe(OccupancyActivity.this, battery -> {
                    binding.batteryPercent.setText(battery + "%");
                    setBatteryIcon(battery);
                });
                viewModel.getConnectionState().observe(OccupancyActivity.this, state -> {
                      switch (state.getState()) {
                          case CONNECTING:
                              break;
                          case INITIALIZING:
                              break;
                          case READY:
                              onConnectionStateChanged(true);
                              break;
                          case DISCONNECTED:
                              if (state instanceof ConnectionState.Disconnected) {
                                  final ConnectionState.Disconnected stateWithReason = (ConnectionState.Disconnected) state;
                                  Log.v("DISCONNECT", String.valueOf(stateWithReason.getReason()));
                                  if (stateWithReason.getReason() == ConnectionObserver.REASON_TIMEOUT) {
                                      Toast.makeText(OccupancyActivity.this, "Error: Timed out trying to connect to device", Toast.LENGTH_SHORT).show();
                                  } else if (stateWithReason.getReason() != ConnectionObserver.REASON_UNKNOWN) {
                                      viewModel.reconnect();
                                  }
                              }
                          case DISCONNECTING:
                              onConnectionStateChanged(false);
                              break;
                      }
              });
              }
          });
    }

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.disconnect();
                Intent intent = new Intent(OccupancyActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onConnectionStateChanged(final boolean connected) {
        binding.refresh.setEnabled(connected);
        if (!connected) {
//            binding.batteryPercent.setText("0%");
            binding.occupancyNumber.setText("--");
            binding.optionsButton.setEnabled(false);
//            binding.exportButton.setEnabled(false);
        }
        else {
            Toast.makeText(this, "Successfully connected to device", Toast.LENGTH_SHORT).show();
            binding.optionsButton.setEnabled(true);
//            binding.exportButton.setEnabled(true);
        }
    }

    public void refreshData(View view) {

    }

    public void openOptionsPopup(View view) {
        runOnUiThread(new Runnable() {
              public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(OccupancyActivity.this);
            builder.setTitle("Options");

            LinearLayout lila1= new LinearLayout(OccupancyActivity.this);
            lila1.setOrientation(LinearLayout.VERTICAL);
            final EditText ceilingHeightInput = new EditText(OccupancyActivity.this);
            ceilingHeightInput.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            ceilingHeightInput.setHint(getResources().getString(R.string.ceiling_height));
            if (ceilingHeight != null) {
                ceilingHeightInput.setText(ceilingHeight.toString());
            }
            final EditText resetOccupancyInput = new EditText(OccupancyActivity.this);
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

            AlertDialog dialog = builder.show();
            ceilingHeightInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    final String ceilingHeight = ceilingHeightInput.getText().toString();
                    Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    try {
                        if (Float.parseFloat(ceilingHeight) > 4.0) {
                            ceilingHeightInput.setError("Ceiling height must be 4.0 m or less");
                            okButton.setEnabled(false);
                        } else {
                            ceilingHeightInput.setError(null);
                            okButton.setEnabled(true);
                        }
                    } catch (NumberFormatException e) {
                        ceilingHeightInput.setError(null);
                        okButton.setEnabled(true);
                    }
                }
            });
              }
        });
    }

    public void exportData(View view) {

    }

    private void setBatteryIcon(int level) {
        if (level >= 80) {
            binding.batteryImage.setImageResource(R.drawable.battery_5);
        } else if (level >= 60) {
            binding.batteryImage.setImageResource(R.drawable.battery_4);
        } else if (level >= 40) {
            binding.batteryImage.setImageResource(R.drawable.battery_3);
        } else if (level >= 20) {
            binding.batteryImage.setImageResource(R.drawable.battery_2);
        } else {
            binding.batteryImage.setImageResource(R.drawable.battery_1);
        }
    }
}