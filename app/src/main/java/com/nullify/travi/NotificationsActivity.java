package com.nullify.travi;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by abhishekpalodath on 27/06/2017.
 */

public class NotificationsActivity extends AppCompatActivity {
    GridView notificationlist;
    ArrayList<String> femail = new ArrayList<>();
    ArrayList<String> fname = new ArrayList<>();
    ArrayList<String> ftype = new ArrayList<>();
    ArrayList<String> floc = new ArrayList<>();
    private DatabaseReference mDatabase;
    String email;
    public static NotificationfollowAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();
        notificationlist = (GridView) findViewById(R.id.list_notification);
        if (!NewTravies.ntype.isEmpty()) {
            findViewById(R.id.warning_notification).setVisibility(View.GONE);
            for (int i = NewTravies.ntype.size() - 1; i >= 0; i--) {
                femail.add(NewTravies.nemail.get(i));
                fname.add(NewTravies.ndisp.get(i));
                floc.add(NewTravies.nloc.get(i));
                ftype.add(NewTravies.ntype.get(i));
            }
            adapter = new NotificationfollowAdapter(this, femail, fname, floc, ftype);
            notificationlist.setAdapter(adapter);
        } else {
            findViewById(R.id.warning_notification).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.notification_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationsActivity.this.finish();
            }
        });
        notificationlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (NewTravies.ntype.get(position).equals("Follow")) {
                    Fragment profile = new ProfileActivityOthers();
                    Bundle bundle = new Bundle();
                    bundle.putString("email", NewTravies.nemail.get(position));
                    profile.setArguments(bundle);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.add(R.id.notification_root, profile).addToBackStack("tag").commit();
                } else if (NewTravies.ntype.get(position).equals("new")) {
                    Fragment travy = new TravysList();
                    Bundle bundle = new Bundle();
                    bundle.putString("dis", NewTravies.nloc.get(position));
                    bundle.putString("loc", NewTravies.nloc.get(position));
                    travy.setArguments(bundle);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.add(R.id.notification_root, travy).addToBackStack("tag").commit();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tab1Home.count.setVisibility(View.GONE);
        NewTravies.noofnot = 0;
    }
}
