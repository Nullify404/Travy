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
 * Created by abhishekpalodath on 29/06/2017.
 */

public class ChatroomListAdapter extends ArrayAdapter<String>{
    private ArrayList<String> email;
    private ArrayList<String> name;
    private ArrayList<String> msg;
    private ArrayList<String> count;
    private Activity context;
    private StorageReference mstorage = FirebaseStorage.getInstance().getReference();

    public ChatroomListAdapter(Activity context,ArrayList<String> email,ArrayList<String> name,ArrayList<String> msg,ArrayList<String> count) {
        super(context, R.layout.chatroomstyle,email);
        this.context = context;
        this.email = email;
        this.name = name;
        this.msg = msg;
        this.count = count;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.chatroomstyle, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.chat_result_name);
        TextView lmsg = (TextView) rowView.findViewById(R.id.chat_result_msg);
        TextView tcount  =(TextView) rowView.findViewById(R.id.msg_count);
        if (count.get((count.size()-1)-position).equals("0")){
            tcount.setVisibility(View.GONE);
        }
        else {
            tcount.setVisibility(View.VISIBLE);
            tcount.setText(count.get((count.size()-1)-position));
        }
        final TextView imageView = (TextView) rowView.findViewById(R.id.chat_result_image);
        String a = name.get((name.size()-1)-position);
        txtTitle.setText(a);
        lmsg.setText(msg.get((name.size()-1)-position));
        imageView.setText(String.valueOf(a.charAt(0)).toUpperCase());
        mstorage.child("pro_pics/" + email.get(((name.size()-1)-position)) + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setBackground(circularBitmapDrawable);
                        imageView.setText("");
                    }
            });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
        return rowView;
    }
}
