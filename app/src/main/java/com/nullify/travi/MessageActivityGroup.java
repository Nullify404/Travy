package com.nullify.travi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageActivityGroup extends AppCompatActivity {

    String grpname, grpid;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private StorageReference mstorage;
    ArrayList<String> name = new ArrayList<>();
    ArrayList<String> msg = new ArrayList<>();
    ArrayList<String> email = new ArrayList<>();
    ArrayList<String> time = new ArrayList<>();
    ArrayList<String> added = new ArrayList<>();
    TextView groupname;
    Toolbar chattoolbar;
    GridView messages;
    EditText input;
    TextView grppic;
    Button send, sendl;
    MessageGroupAdapter adapter;
    ValueEventListener listener;
    public static boolean active = true;
    public static Activity fa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_group);
        fa=this;

        chattoolbar = (Toolbar) findViewById(R.id.chat_group_toolbar);
        setSupportActionBar(chattoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        chattoolbar.setContentInsetStartWithNavigation(0);
        chattoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.removeEventListener(listener);
                MessageActivityGroup.this.finish();
            }
        });
        messages = (GridView) findViewById(R.id.messages);
        groupname = (TextView) findViewById(R.id.group_name);
        messages.smoothScrollToPosition(messages.getCount() - 1);
        input = (EditText) findViewById(R.id.messages_input);
        grppic = (TextView) findViewById(R.id.message_other_propic);
        send = (Button) findViewById(R.id.send);
        sendl = (Button) findViewById(R.id.send_love);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mstorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();

        adapter = new MessageGroupAdapter(MessageActivityGroup.this, email, name, msg, time, added, user.getEmail());
        messages.setAdapter(adapter);
        grpname = getIntent().getExtras().getString("name");
        grpid = getIntent().getExtras().getString("id");

        groupname.setText(grpname);
        grppic.setText(String.valueOf(grpname.charAt(0)).toUpperCase());
        mstorage.child("grps/" + grpid + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        grppic.setBackground(circularBitmapDrawable);
                        grppic.setText("");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
        findViewById(R.id.group_details).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivityGroup.this,GroupDetailActivity.class);
                intent.putExtra("id",grpid);
                intent.putExtra("name",grpname);
                startActivity(intent);
            }
        });
        sendl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg.add(input.getText().toString());
                email.add(user.getEmail());
                name.add(user.getDisplayName());
                added.add("false");
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                time.add(dateFormat.format(new Date()).toString());
                messages.smoothScrollToPosition(messages.getCount() - 1);
                adapter.notifyDataSetChanged();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("msg", "❤");
                map.put("add", "false");
                map.put("email", user.getEmail());
                map.put("name", user.getDisplayName());
                map.put("time", dateFormat.format(new Date()).toString());
                DatabaseReference n = mDatabase.child("Chats").child("grps").child(grpid).child("msg").push();
                n.updateChildren(map);
                input.setText("");
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(input.getText().toString().isEmpty())) {
                    msg.add(input.getText().toString());
                    email.add(user.getEmail());
                    name.add(user.getDisplayName());
                    added.add("false");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    time.add(dateFormat.format(new Date()).toString());
                    messages.smoothScrollToPosition(messages.getCount() - 1);
                    adapter.notifyDataSetChanged();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("msg", input.getText().toString());
                    map.put("email", user.getEmail());
                    map.put("name", user.getDisplayName());
                    map.put("add", "false");
                    map.put("time", dateFormat.format(new Date()).toString());
                    DatabaseReference n = mDatabase.child("Chats").child("grps").child(grpid).child("msg").push();
                    n.updateChildren(map);
                    input.setText("");
                }
            }
        });
        listener = mDatabase.child("Chats").child("grps").child(grpid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                grpname = (String) dataSnapshot.child("grpname").getValue();
                groupname.setText(grpname);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listener = mDatabase.child("Chats").child("grps").child(grpid).child("msg").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                msg.clear();
                email.clear();
                name.clear();
                time.clear();
                added.clear();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    String key = ((DataSnapshot) i.next()).getKey();
                    String femail = (String) dataSnapshot.child(key).child("email").getValue();
                    String fname = (String) dataSnapshot.child(key).child("name").getValue();
                    String fmsg = (String) dataSnapshot.child(key).child("msg").getValue();
                    String ftime = (String) dataSnapshot.child(key).child("time").getValue();
                    String addd = (String) dataSnapshot.child(key).child("add").getValue();
                    msg.add(fmsg);
                    email.add(femail);
                    name.add(fname);
                    time.add(ftime);
                    added.add(addd);
                    adapter.notifyDataSetChanged();
                    messages.setSelection(messages.getCount() - 1);
                    messages.smoothScrollToPosition(messages.getCount() - 1);
                    mDatabase.child("Chats").child("grps").child(grpid).child("msg").child(key).child(user.getUid()).setValue("read");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (input.getText().toString().isEmpty()) {
                    sendl.setVisibility(View.VISIBLE);
                    send.setVisibility(View.GONE);
                } else {
                    sendl.setVisibility(View.GONE);
                    send.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mstorage.child("grps/" + grpid + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        grppic.setBackground(circularBitmapDrawable);
                        grppic.setText("");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
        messages.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        messages.setAdapter(adapter);
        mDatabase.removeEventListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        messages.setAdapter(null);
        mDatabase.removeEventListener(listener);
    }

    @Override
    public void onBackPressed() {
        MessageActivityGroup.this.finish();
        mDatabase.removeEventListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messages.setAdapter(null);
    }
}
