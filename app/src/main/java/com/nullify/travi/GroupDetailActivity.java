package com.nullify.travi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import java.util.HashMap;
import java.util.Map;

public class GroupDetailActivity extends AppCompatActivity {

    String grpid, grpnames;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    ImageButton addmem, grpedit;
    TextView grpicon, grpname, nummem;
    EditText grpnameedit;
    ImageView back;
    Button done;
    NonScrollListView memlist;
    LinearLayout exit;
    private DatabaseReference mDatabase;
    private StorageReference mstorage;
    ArrayList<String> lemail = new ArrayList<>();
    ArrayList<String> lname = new ArrayList<>();
    ArrayList<String> eee = new ArrayList<>();
    ArrayList<String> nnn = new ArrayList<>();
    GroupDetailListAdapter adapter;
    ValueEventListener mvl,vl;
    int membercount = 0;
    int members = 0;
    Uri imguri;
    int position = 0;
    int GALLARY_INT = 2;
    public static Boolean active;
    Boolean find = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mstorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();

        grpid = getIntent().getExtras().getString("id");
        grpnames = getIntent().getExtras().getString("name");

        addmem = (ImageButton) findViewById(R.id.addgroup);
        grpedit = (ImageButton) findViewById(R.id.grp_edit_button);
        done = (Button) findViewById(R.id.done);
        back = (ImageView) findViewById(R.id.groupmem_back);
        grpicon = (TextView) findViewById(R.id.grp_icon);
        grpname = (TextView) findViewById(R.id.grp_name);
        grpnameedit = (EditText) findViewById(R.id.grp_name_edit);
        nummem = (TextView) findViewById(R.id.num_mem);
        memlist = (NonScrollListView) findViewById(R.id.member_list);
        exit = (LinearLayout) findViewById(R.id.exit_group);

        adapter = new GroupDetailListAdapter(GroupDetailActivity.this, lemail, lname);
        memlist.setAdapter(adapter);
        memlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (lemail.get(position).equals(user.getEmail())){
                }else {
                    Intent intent = new Intent(GroupDetailActivity.this,MessageProfileActivity.class);
                    intent.putExtra("email",lemail.get(position));
                    startActivity(intent);
                }
            }
        });
        grpicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(GroupDetailActivity.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                        .checkSelfPermission(GroupDetailActivity.this,
                                android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                    android.Manifest.permission.CAMERA},
                            0);
                }
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, GALLARY_INT);
            }
        });
        grpname.setText(grpnames);
        grpicon.setText(String.valueOf(grpnames.charAt(0)).toUpperCase());
        mstorage.child("grps/" + grpid + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        grpicon.setBackground(circularBitmapDrawable);
                        grpicon.setText("");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
        addmem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupDetailActivity.this, AddmemberActivity.class);
                intent.putExtra("id", grpid);
                startActivity(intent);
                GroupDetailActivity.this.finish();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupDetailActivity.this.finish();
            }
        });
        grpedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grpname.setVisibility(View.GONE);
                grpnameedit.setVisibility(View.VISIBLE);
                grpnameedit.setText(grpnames);
                addmem.setVisibility(View.GONE);
                done.setVisibility(View.VISIBLE);
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                }
                grpnames = grpnameedit.getText().toString();
                grpnameedit.setVisibility(View.GONE);
                grpname.setVisibility(View.VISIBLE);
                grpname.setText(grpnames);
                done.setVisibility(View.GONE);
                addmem.setVisibility(View.VISIBLE);
                mDatabase.child("Chats").child("grps").child(grpid).child("grpname").setValue(grpnames);
            }
        });
        mvl = mDatabase.child("Chats").child("grps").child(grpid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lemail.clear();
                lname.clear();
                membercount = Integer.parseInt((String) dataSnapshot.child("nummem").getValue());
                members = Integer.parseInt((String) dataSnapshot.child("members").getValue());
                if (membercount != 0) {
                    nummem.setText(String.valueOf(members));
                }
                for (int i = 0; i < membercount; i++) {
                    if (((String) dataSnapshot.child(String.valueOf(i) + "e").getValue()) != null) {
                        if (((String) dataSnapshot.child(String.valueOf(i) + "e").getValue()).equals(user.getEmail())){
                            lemail.add((String) dataSnapshot.child(String.valueOf(i) + "e").getValue());
                            lname.add("You");
                        }
                        else{
                        lemail.add((String) dataSnapshot.child(String.valueOf(i) + "e").getValue());
                        lname.add((String) dataSnapshot.child(String.valueOf(i) + "n").getValue());
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                setListViewHeightBasedOnChildren(memlist);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.removeEventListener(mvl);
                vl = mDatabase.child("Chats").child("grps").child(grpid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        membercount = Integer.parseInt((String) dataSnapshot.child("nummem").getValue());
                        members = Integer.parseInt((String) dataSnapshot.child("members").getValue());
                        for (int i = 0; i < membercount; i++) {
                            String e = (String) dataSnapshot.child(String.valueOf(i) + "e").getValue();
                            if (e != null) {
                                if (e.equals(user.getEmail())) {
                                    mDatabase.child("Chats").child("grps").child(grpid).child(String.valueOf(i) + "e").removeValue();
                                    mDatabase.child("Chats").child("grps").child(grpid).child(String.valueOf(i) + "n").removeValue();
                                    members--;
                                    mDatabase.child("Chats").child("grps").child(grpid).child("members").setValue(String.valueOf(members));
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("msg", " ");
                                    map.put("email", user.getEmail());
                                    map.put("name", user.getDisplayName());
                                    map.put("time", "0:00 PM");
                                    map.put("add", "left");
                                    DatabaseReference n = mDatabase.child("Chats").child("grps").child(grpid).child("msg").push();
                                    n.updateChildren(map);
                                    GroupDetailActivity.this.finish();
                                    MessageActivityGroup.fa.finish();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        });

    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        memlist.setAdapter(null);
        active = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        memlist.setAdapter(null);
    }

    @Override
    public void onBackPressed() {
        GroupDetailActivity.this.finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_INT && resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            cropImage(uri);
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            imguri = data.getData();
            StorageReference filepath = mstorage.child("grps").child(grpid + ".jpg");
            filepath.putFile(imguri);
            Glide.with(getApplicationContext()).load(imguri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>(120, 120) {

                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    grpicon.setBackground(circularBitmapDrawable);
                    grpicon.setText("");
                }
            });
        }
    }

    private void cropImage(Uri uri) {
        Intent CropIntent = new Intent("com.android.camera.action.CROP");
        CropIntent.setDataAndType(uri, "image/*");
        CropIntent.putExtra("crop", "true");
        CropIntent.putExtra("outputX", 500);
        CropIntent.putExtra("outputY", 500);
        CropIntent.putExtra("aspectX", 3);
        CropIntent.putExtra("aspectY", 3);
        CropIntent.putExtra("return-data", false);
        CropIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        startActivityForResult(CropIntent, 3);
    }
}
