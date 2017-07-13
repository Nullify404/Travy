package com.nullify.travi;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by abhishekpalodath on 7/7/2017.
 */

public class MessageGroupAdapter extends ArrayAdapter<String> {
    private ArrayList<String> email;
    private ArrayList<String> msg;
    private ArrayList<String> name;
    private ArrayList<String> time;
    private ArrayList<String> add;
    String uemail;
    private Activity context;
    private StorageReference mstorage = FirebaseStorage.getInstance().getReference();

    public MessageGroupAdapter(Activity context, ArrayList<String> email, ArrayList<String> name, ArrayList<String> msg, ArrayList<String> time,ArrayList<String> add, String uemail) {
        super(context, R.layout.individual_message_style, email);
        this.context = context;
        this.email = email;
        this.name = name;
        this.msg = msg;
        this.time = time;
        this.add = add;
        this.uemail = uemail;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.group_message_style, null, true);
        TextView txtin = (TextView) rowView.findViewById(R.id.incomming_msg_text);
        TextView intime = (TextView) rowView.findViewById(R.id.incomimg_time);
        TextView sendername = (TextView) rowView.findViewById(R.id.sender_name);
        final TextView senderimg = (TextView) rowView.findViewById(R.id.senderimage);
        senderimg.setText(String.valueOf(name.get(position).charAt(0)).toUpperCase());
        TextView outtime = (TextView) rowView.findViewById(R.id.ougoing_time);
        LinearLayout ic = (LinearLayout) rowView.findViewById(R.id.incomimg_carrier);
        LinearLayout oc = (LinearLayout) rowView.findViewById(R.id.outgoing_carrier);
        TextView txtout = (TextView) rowView.findViewById(R.id.outgoint_msg_text);
        TextView adedtext = (TextView) rowView.findViewById(R.id.adedmsg);
        if (add.get(position).equals("true")) {
            ic.setVisibility(View.GONE);
            senderimg.setVisibility(View.GONE);
            adedtext.setVisibility(View.VISIBLE);
            oc.setVisibility(View.GONE);
            adedtext.setText(name.get(position) + " is added");
        }else if (add.get(position).equals("left")){
            ic.setVisibility(View.GONE);
            senderimg.setVisibility(View.GONE);
            adedtext.setVisibility(View.VISIBLE);
            oc.setVisibility(View.GONE);
            adedtext.setText(name.get(position) + " left");
        }
        else {
            mstorage.child("pro_pics/" + email.get(position) + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    if (context.isDestroyed()) {}else {
                        Glide.with(context).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                senderimg.setBackground(circularBitmapDrawable);
                                senderimg.setText("");
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
            if (email.get(position).equals(uemail)) {
                ic.setVisibility(View.GONE);
                senderimg.setVisibility(View.GONE);
                adedtext.setVisibility(View.GONE);
                oc.setVisibility(View.VISIBLE);
                if (msg.get(position).equals("❤")) {
                    oc.getLayoutParams().width = 100;
                    oc.getLayoutParams().height = 100;
                    oc.setBackground(context.getResources().getDrawable(R.drawable.love));
                } else {
                    txtout.setText(msg.get(position));
                    outtime.setText(time.get(position));
                }
            } else {
                oc.setVisibility(View.GONE);
                adedtext.setVisibility(View.GONE);
                ic.setVisibility(View.VISIBLE);
                if (msg.get(position).equals("❤")) {
                    ic.getLayoutParams().width = 100;
                    ic.getLayoutParams().height = 100;
                    ic.setBackground(context.getResources().getDrawable(R.drawable.love));
                } else {
                    txtin.setText(msg.get(position));
                    if (position != 0) {
                        if (name.get(position).equals(name.get(position - 1))) {
                            sendername.setVisibility(View.GONE);
                        } else {
                            sendername.setText(name.get(position));
                        }
                    } else {
                        sendername.setText(name.get(position));
                    }
                    intime.setText(time.get(position));
                }
            }
        }
        return rowView;
    }
}
