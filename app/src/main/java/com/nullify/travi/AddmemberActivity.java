package com.nullify.travi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

public class AddmemberActivity extends AppCompatActivity {
    ImageView back;
    Button done;
    SwipeRefreshLayout swipeRefreshLayout;
    ListView memList;
    String grpid;
    ArrayList<String> memberList = new ArrayList<>();
    ArrayList<String> lemail = new ArrayList<>();
    ArrayList<String> lname = new ArrayList<>();
    ArrayList<String> emem = new ArrayList<>();
    ArrayList<String> nmem = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private StorageReference mstorage;
    CreateGroupAdapter adapter;
    int membercount = 0;
    int members = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmember);
        grpid = getIntent().getExtras().getString("id");
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mstorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();
        back = (ImageView) findViewById(R.id.groupmem_back);
        done = (Button) findViewById(R.id.groumem_done);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.create_group_swipe);
        memList = (ListView) findViewById(R.id.create_group_travylist);
        adapter = new CreateGroupAdapter(this, lemail, lname);
        memList.setAdapter(adapter);
        TextView t1 = (TextView) findViewById(R.id.add_head);
        new GetTravies().execute();
        mDatabase.child("Chats").child("grps").child(grpid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                membercount = Integer.parseInt((String) dataSnapshot.child("nummem").getValue());
                members = Integer.parseInt((String) dataSnapshot.child("members").getValue());
                for (int i = 0; i < membercount; i++) {
                    memberList.add((String) dataSnapshot.child(String.valueOf(i) + "e").getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        memList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RelativeLayout item_view = (RelativeLayout) view;
                final RadioButton itemcheck = (RadioButton) item_view
                        .findViewById(R.id.radioButton_creategroup);
                if (!itemcheck.isChecked()) {
                    if (!emem.contains(lemail.get(position))) {
                        emem.add(lemail.get(position));
                        nmem.add(lname.get(position));
                    }
                    itemcheck.setChecked(true);
                } else {
                    if (emem.contains(lemail.get(position))) {
                        emem.remove(lemail.get(position));
                        nmem.remove(lname.get(position));
                    }
                    itemcheck.setChecked(false);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddmemberActivity.this.finish();
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emem.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Select members", Toast.LENGTH_SHORT).show();
                } else {
                    for (int i = 0; i < emem.size(); i++) {
                        mDatabase.child("Chats").child("grps").child(grpid).child(String.valueOf(membercount) + "e").setValue(emem.get(i));
                        mDatabase.child("Chats").child("grps").child(grpid).child(String.valueOf(membercount) + "n").setValue(nmem.get(i));
                        membercount++;
                        members++;
                        mDatabase.child("Chats").child("grps").child(grpid).child("nummem").setValue(String.valueOf(membercount));
                        mDatabase.child("Chats").child("grps").child(grpid).child("members").setValue(String.valueOf(membercount));
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("msg", " ");
                        map.put("email", emem.get(i));
                        map.put("name", nmem.get(i));
                        map.put("time", "0:00 PM");
                        map.put("add", "true");
                        DatabaseReference n = mDatabase.child("Chats").child("grps").child(grpid).child("msg").push();
                        n.updateChildren(map);
                    }
                    AddmemberActivity.this.finish();
                }
            }
        });
    }

    public class GetTravies extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
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
            swipeRefreshLayout.setRefreshing(false);
            if (!result.isEmpty()) {
                try {
                    JSONObject jsonObj = new JSONObject(result);

                    JSONArray contacts = jsonObj.getJSONArray("result");

                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        if (!memberList.contains(c.getString("email"))) {
                            lemail.add(c.getString("email"));
                            lname.add(c.getString("name"));
                        }
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
}
