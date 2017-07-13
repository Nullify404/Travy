package com.nullify.travi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class ExploreActivity extends Fragment implements OnMapReadyCallback,NetworkStateReceiver.NetworkStateReceiverListener {

    private GoogleMap mMap;
    private MapView mMapView;
    private TextView eloc, edis, estate, ecountry, epin;
    private EditText locinput;
    private Button search, proceed;
    private ImageButton cross;
    public String loc, locname=new String(), district=new String(), state, country, pin, loc_dis;
    private Double lon = new Double(0);
    private Double lat = new Double(0);
    private Marker mark;
    private ProgressBar pb;
    private RelativeLayout locInfo;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private NetworkStateReceiver networkStateReceiver;
    View rootView;


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_explore, container, false);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();
        search = (Button) rootView.findViewById(R.id.explore_search_button);
        proceed = (Button) rootView.findViewById(R.id.explore_proceed);
        cross = (ImageButton) rootView.findViewById(R.id.explore_cross);
        pb = (ProgressBar) rootView.findViewById(R.id.explore_progress);
        locinput = (EditText) rootView.findViewById(R.id.explore_search_loc);
        locInfo = (RelativeLayout) rootView.findViewById(R.id.explore_loc_info);
        eloc = (TextView) rootView.findViewById(R.id.explore_locname);
        edis = (TextView) rootView.findViewById(R.id.explore_district);
        estate = (TextView) rootView.findViewById(R.id.explore_state);
        ecountry = (TextView) rootView.findViewById(R.id.explore_country);
        epin = (TextView) rootView.findViewById(R.id.explore_pin);


        MapsInitializer.initialize(this.getActivity());
        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);


        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locInfo.setVisibility(View.GONE);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                }
                loc = locinput.getText().toString();
                new GetLocation().execute();
            }
        });
        locinput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    search.performClick();
                    handled = true;
                }
                return handled;
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!district.isEmpty()) {
                    loc_dis = district;
                    new SendPostRequest().execute();
                } else if (!locname.isEmpty()) {
                    loc_dis = locname;
                    new SendPostRequest().execute();
                } else {
                    LayoutInflater inflater1 = getActivity().getLayoutInflater();
                    View layout = inflater1.inflate(R.layout.toast_layout,(ViewGroup) rootView.findViewById(R.id.toast_root));
                    TextView msg = (TextView) layout.findViewById(R.id.toast_msg);
                    msg.setText("Location needs to be more precise");
                    Toast toast = new Toast(getContext());
                    toast.setGravity(Gravity.BOTTOM,0,20);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    FragmentManager fm = getChildFragmentManager();
                    fm.popBackStackImmediate();
                    if (fm.getBackStackEntryCount() == 0 && HomeActivity.viewPager.getCurrentItem() == 2) {
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
        } else {

        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void networkAvailable() {
        rootView.findViewById(R.id.explore_network_warning).setVisibility(View.GONE);
    }

    @Override
    public void networkUnavailable() {
        rootView.findViewById(R.id.explore_network_warning).setVisibility(View.VISIBLE);

    }

    private class GetLocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            loc = loc.replaceAll(" ", "%20");
            String url = "http://maps.google.com/maps/api/geocode/json?address=" + loc + "&sensor=false";
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    locname = "";
                    state = "";
                    district = "";
                    country = "";
                    pin = "";
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    lon = ((JSONArray) jsonObj.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lng");
                    lat = ((JSONArray) jsonObj.get("results")).getJSONObject(0)
                            .getJSONObject("geometry").getJSONObject("location")
                            .getDouble("lat");
                    JSONArray adrress_result = jsonObj.getJSONArray("results");
                    JSONObject zero = adrress_result.getJSONObject(0);
                    JSONArray address_components = zero.getJSONArray("address_components");
                    for (int i = 0; i < address_components.length(); i++) {
                        JSONObject zero2 = address_components.getJSONObject(i);
                        String long_name = zero2.getString("long_name");
                        JSONArray mtypes = zero2.getJSONArray("types");
                        String Type = mtypes.getString(0);
                        if (Type.equals("locality")) {
                            locname = long_name;
                        } else if (Type.equals("administrative_area_level_2")) {
                            district = long_name;
                        } else if (Type.equals("administrative_area_level_1")) {
                            state = long_name;
                        } else if (Type.equals("country")) {
                            country = long_name;
                        } else if (Type.equals("postal_code")) {
                            pin = long_name;
                        }
                    }


                } catch (final JSONException e) {

                }
            } else {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pb.setVisibility(View.GONE);
            if (mark != null) {
                mark.remove();
            }
            LatLng loc = new LatLng(lat, lon);
            mark = mMap.addMarker(new MarkerOptions().position(loc).title("Marker"));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(loc)
                    .zoom(10)
                    .bearing(0)
                    .tilt(0)
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            eloc.setText(locname);
            edis.setText(district);
            estate.setText(state);
            ecountry.setText(country);
            epin.setText(pin);
            locInfo.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (mark != null) {
            mark.remove();
        }
        mMap = googleMap;
        LatLng loc = new LatLng(0, 0);
        mark = mMap.addMarker(new MarkerOptions().position(loc).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
        }
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            proceed.setEnabled(false);
            pb.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/addwl.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("email", user.getEmail());
                postDataParams.put("dis", district);
                postDataParams.put("loc", locname);
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
            if (result.equals("1")) {
                Map<String,Object> map = new HashMap<String, Object>();
                map.put("loc",loc_dis);
                map.put("email",user.getEmail());
                DatabaseReference n = mDatabase.child("Places").push();
                n.updateChildren(map);
                Fragment travy = new TravysList();
                Bundle bundle = new Bundle();
                bundle.putString("dis", district);
                bundle.putString("loc", locname);
                travy.setArguments(bundle);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.explore_root, travy).addToBackStack("tag").commit();
                pb.setVisibility(View.GONE);
                proceed.setEnabled(true);
            } else if (result.equals("2")) {
                Fragment travy = new TravysList();
                Bundle bundle = new Bundle();
                bundle.putString("dis", district);
                bundle.putString("loc", locname);
                travy.setArguments(bundle);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.explore_root, travy).addToBackStack("tag").commit();
                pb.setVisibility(View.GONE);
                proceed.setEnabled(true);
            } else {
                Toast.makeText(getContext(), "Something went wrong try again", Toast.LENGTH_SHORT).show();
                pb.setVisibility(View.GONE);
                proceed.setEnabled(true);
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

