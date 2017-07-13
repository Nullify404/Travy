package com.nullify.travi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class CreateGroupActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mstorage;
    ArrayList<String> lemail = new ArrayList<String>();
    ArrayList<String> lname = new ArrayList<String>();
    ArrayList<String> emembers = new ArrayList<String>();
    ArrayList<String> nmembers = new ArrayList<String>();
    SwipeRefreshLayout swipeLayout;
    int GALLARY_INT = 2;
    ListView list;
    CreateGroupAdapter adapter;
    Toolbar toolbar;
    ProgressBar pb;
    Button next, done;
    ImageView back, memback;
    TextView grpImage;
    EditText groupname;
    TabHost host;
    Uri imguri;
    String grpid;
    boolean set = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();
        host = (TabHost) findViewById(R.id.create_group_tab);
        host.setup();
        TabHost.TabSpec spec = host.newTabSpec("Name");
        spec.setContent(R.id.name_tab);
        spec.setIndicator("Name");
        host.addTab(spec);
        spec = host.newTabSpec("List");
        spec.setContent(R.id.list_tab);
        spec.setIndicator("List");
        host.addTab(spec);

        mstorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        list = (ListView) findViewById(R.id.create_group_travylist);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.create_group_swipe);

        next = (Button) findViewById(R.id.groupname_next);
        done = (Button) findViewById(R.id.groumem_done);
        pb = (ProgressBar) findViewById(R.id.groupname_image_progress);
        memback = (ImageView) findViewById(R.id.groupmem_back);
        next.setEnabled(false);
        back = (ImageView) findViewById(R.id.groupname_back);
        grpImage = (TextView) findViewById(R.id.groupname_image);
        groupname = (EditText) findViewById(R.id.groupname_name);
        adapter = new CreateGroupAdapter(this, lemail, lname);
        list.setAdapter(adapter);
        new GetTravies().execute();
        groupname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (groupname.getText().toString().isEmpty()) {
                    grpImage.setText("#");
                } else {
                    if (!set) {
                        grpImage.setText(String.valueOf(groupname.getText().toString().charAt(0)).toUpperCase());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emembers.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Select members", Toast.LENGTH_SHORT).show();
                } else {
                    emembers.add(user.getEmail());
                    nmembers.add(user.getDisplayName());
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("grpname", groupname.getText().toString());
                    map.put("nummem", String.valueOf(emembers.size()));
                    map.put("members", String.valueOf(emembers.size()));
                    for (int i = 0; i < emembers.size(); i++) {
                        map.put(String.valueOf(i) + "e", emembers.get(i));
                        map.put(String.valueOf(i) + "n", nmembers.get(i));
                    }
                    DatabaseReference n = mDatabase.child("Chats").child("grps");
                    grpid = n.push().getKey();
                    n.child(grpid).updateChildren(map);
                    if (imguri != null && !imguri.equals(Uri.EMPTY)) {
                        StorageReference filepath = mstorage.child("grps").child(grpid + ".jpg");
                        filepath.putFile(imguri);
                    }
                    CreateGroupActivity.this.finish();
                }
            }
        });
        memback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                host.setCurrentTab(0);
            }
        });
        groupname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (groupname.getText().toString().isEmpty()) {
                    next.setEnabled(false);
                } else {
                    next.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                }
                host.setCurrentTab(1);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateGroupActivity.this.finish();
            }
        });
        grpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(CreateGroupActivity.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                        .checkSelfPermission(CreateGroupActivity.this,
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
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RelativeLayout item_view = (RelativeLayout) view;
                final RadioButton itemcheck = (RadioButton) item_view
                        .findViewById(R.id.radioButton_creategroup);
                if (itemcheck.isChecked()) {
                    emembers.remove(lemail.get(position));
                    nmembers.remove(lname.get(position));
                    itemcheck.setChecked(false);
                } else {
                    if (!emembers.contains(lemail.get(position))) {
                        emembers.add(lemail.get(position));
                        nmembers.add(lname.get(position));
                    }
                    itemcheck.setChecked(true);
                }
            }
        });
    }

    public class GetTravies extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            swipeLayout.setRefreshing(true);
            lemail.clear();
            lname.clear();

        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/viewfollow.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("email", user.getEmail());
                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            swipeLayout.setRefreshing(false);
            if (!result.isEmpty()) {
                try {
                    JSONObject jsonObj = new JSONObject(result);

                    JSONArray contacts = jsonObj.getJSONArray("result");

                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        lemail.add(c.getString("email"));
                        lname.add(c.getString("name"));
                    }
                } catch (final JSONException e) {

                }
                if (lemail.isEmpty()) {

                } else {
                    adapter.notifyDataSetChanged();
                }

            } else {

            }

        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    @Override
    public void onBackPressed() {
        if (host.getCurrentTab() == 1) {
            host.setCurrentTab(0);
        } else {
            CreateGroupActivity.this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_INT && resultCode == RESULT_OK) {
            pb.setVisibility(View.VISIBLE);
            final Uri uri = data.getData();
            cropImage(uri);
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            imguri = data.getData();
            Glide.with(getApplicationContext()).load(imguri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>(120, 120) {

                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    grpImage.setBackground(circularBitmapDrawable);
                    grpImage.setText("");
                    set = true;
                }
            });
            pb.setVisibility(View.GONE);
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
