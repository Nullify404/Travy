package com.nullify.travi;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by abhishekpalodath on 7/10/2017.
 */

public class WishlistAdapter extends ArrayAdapter<String>{
    private ArrayList<String> loc;
    private ArrayList<String> dis;
    private Activity context;

    public WishlistAdapter(Activity context, ArrayList<String> loc, ArrayList<String> dis)
    {
        super(context, R.layout.wishlist_style, loc);
        this.context = context;
        this.loc = loc;
        this.dis = dis;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.wishlist_style, null, true);
        TextView t1 = (TextView) rowView.findViewById(R.id.local_name);
        TextView t2 = (TextView) rowView.findViewById(R.id.disname);
        t1.setText(loc.get(position));
        t2.setText(dis.get(position));
        return rowView;
    }
}
