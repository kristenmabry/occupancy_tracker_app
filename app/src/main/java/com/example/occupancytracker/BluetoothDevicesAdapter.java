package com.example.occupancytracker;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import static androidx.core.content.ContextCompat.getColor;

public class BluetoothDevicesAdapter extends RecyclerView.Adapter<BluetoothDevicesAdapter.ViewHolder> implements View.OnClickListener {
    private ArrayList<BluetoothDevice> dataset;
    private ViewGroup viewGroup;
    private Activity activity;
    private ArrayList<ViewHolder> viewHolders;
    private SharedPreferences sharedPref;
    private String selectedAddress;

    public BluetoothDevicesAdapter(Activity a, BluetoothDevice[] newData) {
        dataset = new ArrayList<>();
        this.viewHolders = new ArrayList<>();
        Collections.addAll(dataset, newData);
        this.activity = a;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        viewGroup = parent;

        this.sharedPref = this.activity.getSharedPreferences(parent.getContext().getString(R.string.shared_preferences_file_key), Context.MODE_PRIVATE);
        this.selectedAddress = this.sharedPref.getString(parent.getContext().getString(R.string.save_selected_device_key), BluetoothUtils.NO_ADDRESS);

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.two_line_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        this.viewHolders.add(vh);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = dataset.get(position);
        holder.view.setId(position);
        holder.view.setBackground(ContextCompat.getDrawable(viewGroup.getContext(), R.drawable.list_item_ripple_effect));
        holder.view.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            holder.titleView.setText(dataset.get(position).getAlias());
        } else {
            holder.titleView.setText(dataset.get(position).getName());
        }
        holder.subtitleView.setText(dataset.get(position).getAddress());
        holder.subtitleView.setTextColor(getColor(viewGroup.getContext(), android.R.color.darker_gray));
        if (device.getAddress().equals(this.selectedAddress)) {
            holder.selectedBox.setBackgroundColor(getColor(viewGroup.getContext(), R.color.green));
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    @Override
    public void onClick(View view) {
        this.viewHolders.forEach(vh ->
            vh.selectedBox.setBackgroundColor(getColor(viewGroup.getContext(), android.R.color.darker_gray))
        );
        String deviceAddress = dataset.get(view.getId()).getAddress();
        SharedPreferences.Editor editor = this.sharedPref.edit();
        if (deviceAddress.equals(this.selectedAddress)) {
            editor.remove(view.getContext().getString(R.string.save_selected_device_key));
            this.selectedAddress = BluetoothUtils.NO_ADDRESS;
        }
        else {
            editor.putString(view.getContext().getString(R.string.save_selected_device_key), deviceAddress);
            this.viewHolders.get(view.getId()).selectedBox.setBackgroundColor(getColor(viewGroup.getContext(), R.color.green));
            this.selectedAddress = deviceAddress;
        }
        editor.apply();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView titleView;
        public TextView subtitleView;
        public View selectedBox;
        public ViewHolder(View v) {
            super(v);
            view = v;
            titleView = v.findViewById(R.id.text1);
            subtitleView = v.findViewById(R.id.text2);
            selectedBox = v.findViewById(R.id.selected);
        }

    }
}
