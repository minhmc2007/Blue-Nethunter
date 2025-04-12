package com.offsec.nethunter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

class AudioBufferSizeAdapter extends BaseAdapter {

    private final Context context;
    private final List<Long> presets;

    AudioBufferSizeAdapter(Context context, List<Long> presets) {
        this.context = context;
        this.presets = presets;
    }

    @Override
    public int getCount() {
        return presets.size();
    }

    @Override
    public Long getItem(int position) {
        return presets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return presets.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.simple_text_row, parent, false);
        } else {
            view = convertView;
        }

        TextView contentText = view.findViewById(R.id.contentText);

        long item = getItem(position);
        contentText.setText(formatBufferLength(item));

        return view;
    }

    private String formatBufferLength(long usec) {
        if (usec < 0) {
            return context.getString(R.string.buffer_infinite);
        }
        if (usec == 0) {
            return context.getString(R.string.buffer_none);
        }
        DecimalFormat format = new DecimalFormat("");
        format.setMinimumIntegerDigits(1);
        format.setMaximumFractionDigits(6);
        return format.format(usec / 1000000d) + "s";
    }

    public int getItemPosition(long size) {
        for (int i = 0; i < presets.size(); i++) {
            if (presets.get(i) == size) {
                return i;
            }
        }
        return -1;
    }
}
