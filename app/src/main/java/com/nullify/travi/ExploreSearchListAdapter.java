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
 * Created by abhishekpalodath on 24/06/2017.
 */

public class ExploreSearchListAdapter extends ArrayAdapter<String>{
    private ArrayList<String> email;
    private ArrayList<String> displayname;
    private Activity context;
    private StorageReference mstorage = FirebaseStorage.getInstance().getReference();

    public ExploreSearchListAdapter(Activity context,ArrayList<String> email,ArrayList<String> displayname) {
        super(context, R.layout.travy_list_style,email);
        this.context = context;
        this.email = email;
        this.displayname = displayname;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.travy_list_style, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.explore_result_name);
        TextView textemail = (TextView) rowView.findViewById(R.id.explore_result_email);

        final TextView imageView = (TextView) rowView.findViewById(R.id.explore_result_image);
        imageView.setText(String.valueOf(displayname.get(position).charAt(0)).toUpperCase());
        txtTitle.setText(displayname.get(position));
        textemail.setText(email.get(position));
        mstorage.child("pro_pics/" + email.get(position) + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (context.isDestroyed()){}else {
                    Glide.with(getContext()).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setBackground(circularBitmapDrawable);
                            imageView.setText("");
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
        return rowView;
    }
}
