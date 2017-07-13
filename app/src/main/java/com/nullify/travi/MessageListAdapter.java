package com.nullify.travi;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by abhishekpalodath on 30/06/2017.
 */

public class MessageListAdapter extends ArrayAdapter<String> {
    private ArrayList<String> msg;
    private ArrayList<String> type;
    private ArrayList<String> time;
    private Activity context;
    private StorageReference mstorage = FirebaseStorage.getInstance().getReference();

    public MessageListAdapter(Activity context, ArrayList<String> msg, ArrayList<String> type, ArrayList<String> time) {
        super(context, R.layout.individual_message_style, msg);
        this.context = context;
        this.msg = msg;
        this.type = type;
        this.time = time;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.individual_message_style, null, true);
        TextView txtin = (TextView) rowView.findViewById(R.id.incomming_msg_text);
        TextView intime = (TextView) rowView.findViewById(R.id.incomimg_time);
        TextView outtime = (TextView) rowView.findViewById(R.id.ougoing_time);
        LinearLayout ic = (LinearLayout) rowView.findViewById(R.id.incomimg_carrier);
        LinearLayout oc = (LinearLayout) rowView.findViewById(R.id.outgoing_carrier);
        TextView txtout = (TextView) rowView.findViewById(R.id.outgoint_msg_text);
        if (type.get(position).equals("send")) {
            ic.setVisibility(View.GONE);
            oc.setVisibility(View.VISIBLE);
            if (msg.get(position).equals("❤")) {
                oc.getLayoutParams().width=100;
                oc.getLayoutParams().height=100;
                oc.setBackground(context.getResources().getDrawable(R.drawable.love));
            } else {
                txtout.setText(msg.get(position));
                outtime.setText(time.get(position));
            }
        } else {
            oc.setVisibility(View.GONE);
            ic.setVisibility(View.VISIBLE);
            if (msg.get(position).equals("❤")) {
                ic.getLayoutParams().width=100;
                ic.getLayoutParams().height=100;
                ic.setBackground(context.getResources().getDrawable(R.drawable.love));
            } else {
                txtin.setText(msg.get(position));
                intime.setText(time.get(position));
            }
        }
        return rowView;
    }
}