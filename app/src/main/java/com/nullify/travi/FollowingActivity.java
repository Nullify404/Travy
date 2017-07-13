package com.nullify.travi;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class FollowingActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {
    private String femail;
    private GridView travylist;
    private NetworkStateReceiver networkStateReceiver;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView title, t4;
    private ImageView i5,back;
    private SwipeRefreshLayout swipeLayout;
    private ArrayList<String> lemail = new ArrayList<String>();
    private ArrayList<String> lname = new ArrayList<String>();
    ExploreSearchListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(FollowingActivity.this);
        getApplicationContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot().child("Places");

        travylist = (GridView) findViewById(R.id.view_travy_list);
        title = (TextView) findViewById(R.id.view_list_title);
        t4 = (TextView) findViewById(R.id.textView4);
        i5 = (ImageView) findViewById(R.id.imageView5);
        back = (ImageView) findViewById(R.id.groupmem_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowingActivity.this.finish();
            }
        });
        adapter = new ExploreSearchListAdapter(FollowingActivity.this, lemail, lname);
        travylist.setAdapter(adapter);
        femail = getIntent().getExtras().getString("email");
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.view_list_swipe);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetTravies().execute();
            }
        });
        travylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FollowingActivity.this,FollowProfileActivity.class);
                intent.putExtra("email",lemail.get(position));
                intent.putExtra("name",lname.get(position));
                startActivity(intent);
                FollowingActivity.this.finish();
            }
        });
        lemail.clear();
        lname.clear();
    }

    @Override
    public void networkAvailable() {
        findViewById(R.id.view_network_warning).setVisibility(View.GONE);
        new GetTravies().execute();
    }

    @Override
    public void networkUnavailable() {
        findViewById(R.id.view_network_warning).setVisibility(View.VISIBLE);

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
                postDataParams.put("email", femail);
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
            lemail.clear();
            lname.clear();
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
                if (lemail.isEmpty()){
                    findViewById(R.id.travy_warning).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.travy_warning).setVisibility(View.GONE);
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
