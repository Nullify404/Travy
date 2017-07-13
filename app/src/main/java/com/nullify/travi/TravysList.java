package com.nullify.travi;

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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

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

public class TravysList extends Fragment {
    private String dis, loc, Demail, Ddis, Dloc, Dname;
    private GridView travylist;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView title;
    private SwipeRefreshLayout swipeLayout;
    ArrayList<String> lemail = new ArrayList<String>();
    ArrayList<String> lname = new ArrayList<String>();
    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_travys_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        dis = getArguments().getString("dis");
        loc = getArguments().getString("loc");
        user = mAuth.getCurrentUser();

        travylist = (GridView) rootView.findViewById(R.id.travy_list);
        title = (TextView) rootView.findViewById(R.id.explore_list_title);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.explore_list_swipe);
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
                transaction.add(R.id.explorelistroot, profile).addToBackStack("tag").commit();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() == null) {
            return;
        }
        new GetTravies().execute();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    FragmentManager fm = getChildFragmentManager();
                    while (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStackImmediate();
                    }
                    if (fm.getBackStackEntryCount() == 0) {
                        Fragment fragment = TravysList.this;
                        FragmentManager manager = getActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = manager
                                .beginTransaction();
                        transaction.remove(fragment);
                        transaction.commit();
                    }


                }
                return false;
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

                URL url = new URL("http://www.nullify.in/travi/searchtravy.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("dis", dis);
                postDataParams.put("loc", loc);
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
                        Demail = c.getString("email");
                        if (!(Demail.equals(user.getEmail()))) {
                            lemail.add(Demail);
                            Dname = c.getString("name");
                            lname.add(Dname);
                            Ddis = c.getString("dis");
                            Dloc = c.getString("loc");
                        }
                    }
                } catch (final JSONException e) {

                }
                if (lemail.isEmpty()) {
                    rootView.findViewById(R.id.travylist_warning).setVisibility(View.VISIBLE);
                    title.setVisibility(View.GONE);
                } else {
                    rootView.findViewById(R.id.travylist_warning).setVisibility(View.GONE);
                    title.setVisibility(View.VISIBLE);
                    ExploreSearchListAdapter adapter = new ExploreSearchListAdapter(getActivity(), lemail, lname);
                    travylist.setAdapter(adapter);
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
