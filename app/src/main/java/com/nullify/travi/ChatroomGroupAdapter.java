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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by abhishekpalodath on 7/6/2017.
 */

public class ChatroomGroupAdapter extends ArrayAdapter<String> {
    private ArrayList<String> name;
    private ArrayList<String> id;
    private Activity context;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    String fmsg, femail, fname, key = new String();
    private StorageReference mstorage = FirebaseStorage.getInstance().getReference();
    private DatabaseReference mDatabase;
    int msgcount = 0;

    public ChatroomGroupAdapter(Activity context, ArrayList<String> name, ArrayList<String> id) {
        super(context, R.layout.chatroomstyle, name);
        this.context = context;
        this.name = name;
        this.id = id;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.chatroomstyle, null, true);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();
        TextView txtTitle = (TextView) rowView.findViewById(R.id.chat_result_name);
        final TextView lmsg = (TextView) rowView.findViewById(R.id.chat_result_msg);
        final TextView tcount = (TextView) rowView.findViewById(R.id.msg_count);
        final TextView imageView = (TextView) rowView.findViewById(R.id.chat_result_image);
        String a = name.get((name.size() - 1) - position);
        txtTitle.setText(a);
        imageView.setText(String.valueOf(a.charAt(0)).toUpperCase());
        mstorage.child("grps/" + id.get(((name.size() - 1) - position)) + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
        mDatabase.child("Chats").child("grps").child(id.get(((name.size() - 1) - position))).child("msg").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                msgcount = 0;
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    key = ((DataSnapshot) i.next()).getKey();
                    String fadd = (String) dataSnapshot.child(key).child("add").getValue();
                    if (fadd.equals("false")) {
                        fmsg = (String) dataSnapshot.child(key).child("msg").getValue();
                        femail = (String) dataSnapshot.child(key).child("email").getValue();
                        fname = (String) dataSnapshot.child(key).child("name").getValue();
                        if (!dataSnapshot.child(key).hasChild(user.getUid())) {
                            msgcount = msgcount + 1;
                        }
                        if (femail.equals(user.getEmail())) {
                            lmsg.setText("You :" + fmsg);
                        } else {
                            lmsg.setText(fname + " :" + lmsg);
                        }
                    }
                }
                if (msgcount == 0) {
                    tcount.setVisibility(View.GONE);
                } else {
                    tcount.setVisibility(View.VISIBLE);
                    if (msgcount < 99) {
                        tcount.setText(String.valueOf(msgcount));
                    } else {
                        tcount.setText(String.valueOf("99+"));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return rowView;
    }
}
