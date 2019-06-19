package com.dtiguardian.bletest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<BleDevice> {
    private Context ctx;
    private int res;

    public DeviceListAdapter(Context context, int resource, List<BleDevice> list){
        super(context, resource, list);
        ctx = context;
        res = resource;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).getName();
        String uuid = getItem(position).getUuid().toString();

        LayoutInflater inflater = LayoutInflater.from(ctx);
        convertView = inflater.inflate(res, parent, false);

        TextView tvName, tvUuid;
        tvName = convertView.findViewById(R.id.tvName);
        tvUuid = convertView.findViewById(R.id.tvUuid);

        tvName.setText(name);
        tvUuid.setText(uuid);
        return convertView;
    }


}
