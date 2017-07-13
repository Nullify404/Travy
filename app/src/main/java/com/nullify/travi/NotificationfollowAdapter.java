package com.nullify.travi;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by abhishekpalodath on 28/06/2017.
 */

public class NotificationfollowAdapter extends ArrayAdapter<String> {
    private ArrayList<String> email;
    private ArrayList<String> name;
    private ArrayList<String> loc;
    private ArrayList<String> type;
    private Activity context;
    private StorageReference mstorage = FirebaseStorage.getInstance().getReference();

    public NotificationfollowAdapter(Activity context,ArrayList<String> email,ArrayList<String> name,ArrayList<String> loc,ArrayList<String> type) {
        super(context, R.layout.notification_list_follow_style,email);
        this.context = context;
        this.email = email;
        this.name = name;
        this.loc = loc;
        this.type = type;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.notification_list_follow_style, null, true);
        TextView temail = (TextView) rowView.findViewById(R.id.notification_name);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.notification_image);
        if (type.get(position).equals("Follow")){
            String text = "<b>" + name.get(position) + "</b> is started to follow you";
            temail.setText(Html.fromHtml(text));
        }
        else if (type.get(position).equals("new")){
            temail.setText("New travy is available to "+loc.get(position));
        }
        Drawable myDrawable = context.getResources().getDrawable(R.drawable.avatar);
        Bitmap avatar = ((BitmapDrawable) myDrawable).getBitmap();
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), avatar);
        roundedBitmapDrawable.setCircular(true);
        imageView.setBackground(roundedBitmapDrawable);
        mstorage.child("pro_pics/" + email.get(position) + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).asBitmap().centerCrop().into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
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
