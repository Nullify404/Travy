package com.nullify.travi;

import android.app.NotificationManager;
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

/**
 * Created by abhishekpalodath on 30/06/2017.
 */

public class MessageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private StorageReference mstorage;
    public static boolean active;
    String me, name, ume;
    public static String email = new String();
    GridView messages;
    EditText input;
    TextView messagename, userstate;
    boolean flag = true;
    Button send, sendl;
    TextView propic;
    ArrayList<String> msg = new ArrayList<>();
    ArrayList<String> type = new ArrayList<>();
    ArrayList<String> time = new ArrayList<>();
    MessageListAdapter adapter;
    Toolbar chattoolbar;
    ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        NewTravies.countemail = 0;
        NewTravies.countmsg = 0;
        NewTravies.mmail.clear();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel("msg", 1);
        chattoolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chattoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        chattoolbar.setContentInsetStartWithNavigation(0);
        chattoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.removeEventListener(listener);
                MessageActivity.this.finish();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mstorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();
        messages = (GridView) findViewById(R.id.messages);
        messagename = (TextView) findViewById(R.id.message_name);
        userstate = (TextView) findViewById(R.id.messages_user_state);
        messages.smoothScrollToPosition(messages.getCount() - 1);
        adapter = new MessageListAdapter(MessageActivity.this, msg, type, time);
        messages.setAdapter(adapter);
        input = (EditText) findViewById(R.id.messages_input);
        propic = (TextView) findViewById(R.id.message_other_propic);
        send = (Button) findViewById(R.id.send);
        sendl = (Button) findViewById(R.id.send_love);
        email = getIntent().getExtras().getString("email");
        ume = email;
        ume = ume.replace(".", "-");
        name = getIntent().getExtras().getString("name");
        findViewById(R.id.sender_details).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this,MessageProfileActivity.class);
                intent.putExtra("email",email);
                startActivity(intent);
            }
        });
        messagename.setText(name);
        me = user.getEmail();
        me = me.replace(".", "-");
        propic.setText(String.valueOf(name.charAt(0)).toUpperCase());
        mstorage.child("pro_pics/" + email + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        propic.setBackground(circularBitmapDrawable);
                        propic.setText("");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
        listener = mDatabase.child("Chats").child(me).child("ind").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                NewTravies.countemail = 0;
                NewTravies.countmsg = 0;
                msg.clear();
                type.clear();
                time.clear();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    String key = ((DataSnapshot) i.next()).getKey();
                    String femail = (String) dataSnapshot.child(key).child("email").getValue();
                    String fmsg = (String) dataSnapshot.child(key).child("msg").getValue();
                    String ftime = (String) dataSnapshot.child(key).child("time").getValue();
                    String ftype = (String) dataSnapshot.child(key).child("type").getValue();
                    if (femail.equals(email)) {
                        mDatabase.child("Chats").child(me).child("ind").child(key).child("status").setValue("read");
                        msg.add(fmsg);
                        type.add(ftype);
                        time.add(ftime);
                        adapter.notifyDataSetChanged();
                        messages.setSelection(messages.getCount() - 1);
                        messages.smoothScrollToPosition(messages.getCount() - 1);
                    }
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
        sendl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg.add(input.getText().toString());
                type.add("send");
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                time.add(dateFormat.format(new Date()).toString());
                messages.smoothScrollToPosition(messages.getCount() - 1);
                adapter.notifyDataSetChanged();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("msg", "❤");
                map.put("email", email);
                map.put("type", "send");
                map.put("status", "read");
                map.put("nstatus", "read");
                map.put("name", name);
                map.put("time", dateFormat.format(new Date()).toString());
                DatabaseReference n = mDatabase.child("Chats").child(me).child("ind").push();
                n.updateChildren(map);
                Map<String, Object> map2 = new HashMap<String, Object>();
                map2.put("msg", "❤");
                map2.put("email", user.getEmail());
                map2.put("type", "retrieve");
                map2.put("nstatus", "unread");
                map2.put("status", "unread");
                map2.put("time", dateFormat.format(new Date()).toString());
                map2.put("name", user.getDisplayName());
                DatabaseReference n2 = mDatabase.child("Chats").child(ume).child("ind").push();
                n2.updateChildren(map2);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(input.getText().toString().isEmpty())) {
                    msg.add(input.getText().toString());
                    type.add("send");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    time.add(dateFormat.format(new Date()).toString());
                    messages.smoothScrollToPosition(messages.getCount() - 1);
                    adapter.notifyDataSetChanged();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("msg", input.getText().toString());
                    map.put("email", email);
                    map.put("type", "send");
                    map.put("status", "read");
                    map.put("nstatus", "read");
                    map.put("name", name);
                    map.put("time", dateFormat.format(new Date()).toString());
                    DatabaseReference n = mDatabase.child("Chats").child(me).child("ind").push();
                    n.updateChildren(map);
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("msg", input.getText().toString());
                    map2.put("email", user.getEmail());
                    map2.put("type", "retrieve");
                    map2.put("nstatus", "unread");
                    map2.put("status", "unread");
                    map2.put("time", dateFormat.format(new Date()).toString());
                    map2.put("name", user.getDisplayName());
                    DatabaseReference n2 = mDatabase.child("Chats").child(ume).child("ind").push();
                    n2.updateChildren(map2);
                    input.setText("");
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (listener != null && mDatabase != null) {
            mDatabase.child("Chats").child(me).child("ind").removeEventListener(listener);
        }
        active = false;
    }

    @Override
    public void onBackPressed() {
        mDatabase.child("Chats").child(me).child("ind").removeEventListener(listener);
        MessageActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null && mDatabase != null) {
            mDatabase.child("Chats").child(me).child("ind").removeEventListener(listener);
        }
    }
}
