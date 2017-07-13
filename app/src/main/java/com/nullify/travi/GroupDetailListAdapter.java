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
import android.widget.RadioButton;
import android.widget.RelativeLayout;
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
 * Created by abhishekpalodath on 7/8/2017.
 */

public class GroupDetailListAdapter extends ArrayAdapter<String> {
    private ArrayList<String> email;
    private ArrayList<String> displayname;
    private Activity context;
    private StorageReference mstorage = FirebaseStorage.getInstance().getReference();

    public GroupDetailListAdapter(Activity context, ArrayList<String> email, ArrayList<String> displayname) {
        super(context, R.layout.travy_list_style, email);
        this.context = context;
        this.email = email;
        this.displayname = displayname;
    }

    @Override
    public View getView(final int position, final View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.groupcreateliststyle, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.create_group_name);
        TextView textemail = (TextView) rowView.findViewById(R.id.create_group_email);
        RelativeLayout item = (RelativeLayout) rowView.findViewById(R.id.group_list_item);
        final RadioButton button = (RadioButton) rowView.findViewById(R.id.radioButton_creategroup);
        button.setVisibility(View.GONE);
        final TextView imageView = (TextView) rowView.findViewById(R.id.create_group_image);
        txtTitle.setText(displayname.get(position));
        textemail.setText(email.get(position));
        imageView.setText(String.valueOf(displayname.get(position).charAt(0)).toUpperCase());
        try {
        mstorage.child("pro_pics/" + email.get(position) + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (context.isDestroyed()) {}else {
                    Glide.with(context.getApplicationContext()).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            try {
                                imageView.setBackground(circularBitmapDrawable);
                                imageView.setText("");}
                            catch (Exception e){

                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });}catch (Exception e){

        }
        return rowView;
    }
}
