package com.example.videoconferenceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.videoconferenceapp.R;

public class OptionsAdapter  extends BaseAdapter {

    private Context context;
    private int[] images;
    private String[] names;

    public OptionsAdapter(Context context, int[] images, String[] names){
        this.context = context;
        this.images = images;
        this.names = names;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.option_design, parent, false);

        ImageView imageView = v.findViewById(R.id.img_option);
        TextView textView = v.findViewById(R.id.txt_option);

        imageView.setImageResource(images[position]);
        textView.setText(names[position]);

        return v;
    }

}
