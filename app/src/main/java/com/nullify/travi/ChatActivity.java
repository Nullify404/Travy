package com.nullify.travi;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;

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
 * Created by abhishekpalodath on 29/06/2017.
 */

public class ChatActivity extends Fragment implements NetworkStateReceiver.NetworkStateReceiverListener {
    private FirebaseAuth mAuth;
    private NetworkStateReceiver networkStateReceiver;
    int postion;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private StorageReference mstorage;
    ListView indchatrooms, grpchatrooms;
    String me;
    ArrayList<String> chatroommail = new ArrayList<>();
    ArrayList<String> chatroomname = new ArrayList<>();
    ArrayList<String> lastchat = new ArrayList<>();
    ArrayList<String> newcount = new ArrayList<>();
    ArrayList<String> groupname = new ArrayList<>();
    ArrayList<String> groupid = new ArrayList<>();
    ChatroomListAdapter adapter;
    ChatroomGroupAdapter adapter2;
    FloatingActionButton add;
    View rootView;
    boolean g = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.chatrooms_list_activity, container, false);
        TabHost host = (TabHost) rootView.findViewById(R.id.chat_tabs);
        host.setup();
        TabHost.TabSpec spec = host.newTabSpec("CHATS");
        spec.setContent(R.id.chat_tab);
        spec.setIndicator("CHATS");
        host.addTab(spec);
        spec = host.newTabSpec("GROUPS");
        spec.setContent(R.id.group_tab);
        spec.setIndicator("GROUPS");
        host.addTab(spec);

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mstorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();
        indchatrooms = (ListView) rootView.findViewById(R.id.individual_chat_list);
        grpchatrooms = (ListView) rootView.findViewById(R.id.group_list);
        add = (FloatingActionButton) rootView.findViewById(R.id.create_group);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreateGroupActivity.class));
            }
        });
        adapter = new ChatroomListAdapter(getActivity(), chatroommail, chatroomname, lastchat, newcount);
        indchatrooms.setAdapter(adapter);
        adapter2 = new ChatroomGroupAdapter(getActivity(), groupname, groupid);
        grpchatrooms.setAdapter(adapter2);
        indchatrooms.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                rootView.findViewById(R.id.chat_progress).setVisibility(View.GONE);
            }
        });

        me = user.getEmail();
        me = me.replace(".", "-");
        mDatabase.child("Chats").child(me).child("ind").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatroommail.clear();
                chatroomname.clear();
                lastchat.clear();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    String key = ((DataSnapshot) i.next()).getKey();
                    String email = (String) dataSnapshot.child(key).child("email").getValue();
                    String msg = (String) dataSnapshot.child(key).child("msg").getValue();
                    String name = (String) dataSnapshot.child(key).child("name").getValue();
                    String status = (String) dataSnapshot.child(key).child("status").getValue();
                    if (!chatroommail.contains(email)) {
                        if (status.equals("unread")) {
                            newcount.add("1");
                        } else {
                            newcount.add("0");
                        }
                        chatroommail.add(email);
                        chatroomname.add(name);
                        lastchat.add(msg);
                        adapter.notifyDataSetChanged();
                    } else {
                        for (int j = chatroommail.size() - 1; j >= 0; j--) {
                            if (chatroommail.get(j).equals(email)) {
                                postion = j;
                                break;
                            }
                        }
                        chatroommail.remove(email);
                        chatroomname.remove(postion);
                        lastchat.remove(postion);
                        chatroommail.add(email);
                        chatroomname.add(name);
                        lastchat.add(msg);
                        if (status.equals("unread")) {
                            int count = Integer.parseInt(newcount.get(postion));
                            count = count + 1;
                            newcount.remove(postion);
                            newcount.add(String.valueOf(count));
                            adapter.notifyDataSetChanged();
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
                if (dataSnapshot.getChildrenCount() > 0) {
                    rootView.findViewById(R.id.chat_warning).setVisibility(View.GONE);
                } else {
                    rootView.findViewById(R.id.chat_warning).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase.child("Chats").child("grps").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupname.clear();
                groupid.clear();
                if (!dataSnapshot.hasChildren()){
                    g = false;
                }
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    String key = ((DataSnapshot) i.next()).getKey();
                    String name = (String) dataSnapshot.child(key).child("grpname").getValue();
                    int num = Integer.parseInt((String) dataSnapshot.child(key).child("nummem").getValue());
                    for (int j = 0; j < num; j++) {
                        String e = (String) dataSnapshot.child(key).child(String.valueOf(j) + "e").getValue();
                        if (e != null) {
                            if (e.equals(user.getEmail())) {
                                groupname.add(name);
                                groupid.add(key);
                                g = true;
                                adapter2.notifyDataSetChanged();
                                break;
                            }else{
                                g = false;
                            }
                        }
                    }
                }
                if (!g) {
                    rootView.findViewById(R.id.group_warning).setVisibility(View.VISIBLE);
                } else {
                    rootView.findViewById(R.id.group_warning).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        indchatrooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity().getApplicationContext(), MessageActivity.class);
                intent.putExtra("email", chatroommail.get((chatroommail.size() - 1) - position));
                intent.putExtra("name", chatroomname.get(((chatroomname.size() - 1) - position)));
                startActivity(intent);
            }
        });
        grpchatrooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity().getApplicationContext(), MessageActivityGroup.class);
                intent.putExtra("name", groupname.get((groupname.size() - 1) - position));
                intent.putExtra("id", groupid.get(((groupid.size() - 1) - position)));
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (HomeActivity.viewPager.getCurrentItem() == 4) {
                        HomeActivity.viewPager.setCurrentItem(0, false);
                    }
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void networkAvailable() {
        rootView.findViewById(R.id.chat_network_warning).setVisibility(View.GONE);
    }

    @Override
    public void networkUnavailable() {
        rootView.findViewById(R.id.chat_network_warning).setVisibility(View.VISIBLE);
    }
}
