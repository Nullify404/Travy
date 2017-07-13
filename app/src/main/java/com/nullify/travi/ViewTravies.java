package com.nullify.travi;

import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Created by abhishekpalodath on 26/06/2017.
 */

public class ViewTravies extends Fragment implements NetworkStateReceiver.NetworkStateReceiverListener {
    private String femail;
    private GridView travylist;
    private NetworkStateReceiver networkStateReceiver;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView title, t4;
    private ImageView i5;
    private SwipeRefreshLayout swipeLayout;
    private ArrayList<String> lemail = new ArrayList<String>();
    private ArrayList<String> lname = new ArrayList<String>();
    ExploreSearchListAdapter adapter;
    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.viewtravies, container, false);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot().child("Places");

        travylist = (GridView) rootView.findViewById(R.id.view_travy_list);
        title = (TextView) rootView.findViewById(R.id.view_list_title);
        t4 = (TextView) rootView.findViewById(R.id.textView4);
        i5 = (ImageView) rootView.findViewById(R.id.imageView5);
        adapter = new ExploreSearchListAdapter(getActivity(), lemail, lname);
        travylist.setAdapter(adapter);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.view_list_swipe);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetTravies().execute();
            }
        });
        travylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment profile = new ProfileActivityOthers();
                Bundle bundle = new Bundle();
                bundle.putString("email", lemail.get(position));
                profile.setArguments(bundle);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.viewlistroot, profile).addToBackStack("tag").commit();
            }
        });
        lemail.clear();
        lname.clear();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        lemail.clear();
        lname.clear();
        new GetTravies().execute();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    FragmentManager fm = getChildFragmentManager();
                    fm.popBackStackImmediate();
                    if (fm.getBackStackEntryCount() == 0 && HomeActivity.viewPager.getCurrentItem() == 3) {
                        HomeActivity.viewPager.setCurrentItem(0, false);
                    } else if (fm.getBackStackEntryCount() != 0) {
                        fm.popBackStackImmediate();
                    }
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void networkAvailable() {
        rootView.findViewById(R.id.view_network_warning).setVisibility(View.GONE);
        new GetTravies().execute();
    }

    @Override
    public void networkUnavailable() {
        rootView.findViewById(R.id.view_network_warning).setVisibility(View.VISIBLE);

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
                    rootView.findViewById(R.id.travy_warning).setVisibility(View.VISIBLE);
                }else {
                    rootView.findViewById(R.id.travy_warning).setVisibility(View.GONE);
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
