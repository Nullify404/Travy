package com.nullify.travi;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by abhishekpalodath on 25/06/2017.
 */

public class ProfileActivityOthers extends Fragment {
    private ImageView cover;
    TextView pro;
    private TextView name, email, followers, following, rating,about;
    private Button follow, unfollow, rate, message;
    private String otheremail,othername;
    private FirebaseAuth mAuth;
    private FrameLayout fullcover;
    private StorageReference mstorage;
    private FirebaseUser user;
    private String userrating;
    private int prerating = 0;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_profile_others, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();
        cover = (ImageView) rootView.findViewById(R.id.profile_other_cover);
        pro = (TextView) rootView.findViewById(R.id.profile_other_propic);
        name = (TextView) rootView.findViewById(R.id.profile_other_name);
        email = (TextView) rootView.findViewById(R.id.profile_other_email);
        followers = (TextView) rootView.findViewById(R.id.profile_other_no_followers);
        following = (TextView) rootView.findViewById(R.id.profile_other_no_following);
        follow = (Button) rootView.findViewById(R.id.profile_other_button_follow);
        unfollow = (Button) rootView.findViewById(R.id.profile_other_unfollow);
        rate = (Button) rootView.findViewById(R.id.profile_other_rate);
        message = (Button) rootView.findViewById(R.id.profile_other_message);
        rating = (TextView) rootView.findViewById(R.id.profile_other_rating);
        about = (TextView) rootView.findViewById(R.id.about_user);
        fullcover = (FrameLayout) rootView.findViewById(R.id.explore_fullcover);
        mstorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        otheremail = getArguments().getString("email");
        new loaddata().execute();
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new updatefollow().execute();
            }
        });
        unfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new unfollow().execute();
            }
        });
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(),MessageActivity.class);
                intent.putExtra("email", otheremail);
                intent.putExtra("name", othername);
                startActivity(intent);
            }
        });
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.rating_dialogue);
                final RatingBar ratingBar = (RatingBar) dialog.findViewById(R.id.user_rating_bar);
                final TextView textView = (TextView) dialog.findViewById(R.id.ratetitle);
                if (!(prerating == 0)){
                    textView.setText("You already rated this user");
                }
                ratingBar.setRating(prerating);
                final Button submit = (Button) dialog.findViewById(R.id.user_rating_submit);
                submit.setEnabled(false);
                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        if (!((ratingBar.getRating())==0)){
                            submit.setEnabled(true);
                        }
                    }
                });
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userrating = String.valueOf(ratingBar.getRating());
                        new rateuser().execute();
                        dialog.dismiss();
                    }
                });
                dialog.show();
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
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Fragment fragment = ProfileActivityOthers.this;
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = manager
                            .beginTransaction();
                    transaction.remove(fragment);
                    transaction.commit();
                }
                return false;
            }
        });
    }
    public class loaddata extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            fullcover.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/travydetails.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("email", otheremail);
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
            if (!result.isEmpty()) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray contacts = jsonObj.getJSONArray("result");
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String n = c.getString("name");
                        othername=n;
                        String f1 = c.getString("followers");
                        String f2 = c.getString("following");
                        String ab = c.getString("about");
                        about.setText(ab);
                        float a = Float.parseFloat(c.getString("rating"));
                        rating.setText(String.format(Locale.US,"%.2f", a)+"/5");
                        followers.setText(f1);
                        following.setText(f2);
                        name.setText(n);
                        pro.setText(String.valueOf(n.charAt(0)).toUpperCase());
                        email.setText(otheremail);
                    }
                } catch (final JSONException e) {

                }
                mstorage.child("pro_pics/" + otheremail + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getContext()).load(uri).asBitmap().into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                pro.setBackground(circularBitmapDrawable);
                                pro.setText("");
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                    }
                });
                mstorage.child("cover_pics/" + otheremail + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getContext()).load(uri).asBitmap().centerCrop().listener(new RequestListener<Uri, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {

                                return false;
                            }
                        }).into(cover);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mstorage.child("default.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(getContext()).load(uri).asBitmap().centerCrop().listener(new RequestListener<Uri, Bitmap>() {
                                    @Override
                                    public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {

                                        return false;
                                    }
                                }).into(cover);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                });
            } else {


            }
            new checkfollow().execute();
            new ratefetch().execute();
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

    public class loaddataafterfollow extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/travydetails.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("email", otheremail);
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
            fullcover.setVisibility(View.GONE);
            if (!result.isEmpty()) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray contacts = jsonObj.getJSONArray("result");
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String n = c.getString("name");
                        String f1 = c.getString("followers");
                        String f2 = c.getString("following");
                        float a = Float.parseFloat(c.getString("rating"));
                        rating.setText(String.format(Locale.US,"%.2f", a)+"/5");
                        followers.setText(f1);
                        following.setText(f2);
                        name.setText(n);
                        email.setText(otheremail);
                    }
                } catch (final JSONException e) {

                }
            } else {


            }
            new ratefetch().execute();
        }
    }

    public class updatefollow extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/updatefollow.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("uemail", user.getEmail());
                postDataParams.put("oemail", otheremail);
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

                follow.setVisibility(View.GONE);
                unfollow.setVisibility(View.VISIBLE);
                rate.setVisibility(View.VISIBLE);
                message.setVisibility(View.VISIBLE);
                Map<String,Object> map = new HashMap<String, Object>();
                map.put("uemail",user.getEmail());
                map.put("oemail",otheremail);
                map.put("status","unread");
                DatabaseReference n = mDatabase.child("Follow").push();
                n.updateChildren(map);
                new loaddataafterfollow().execute();
            }
        }
    }

    public class checkfollow extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/fetchfollowlist.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("uemail", user.getEmail());
                postDataParams.put("oemail", otheremail);
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
            if (result.equals("al")) {
                follow.setVisibility(View.GONE);
                unfollow.setVisibility(View.VISIBLE);
                rate.setVisibility(View.VISIBLE);
                message.setVisibility(View.VISIBLE);
                fullcover.setVisibility(View.GONE);
            } else if (result.equals("no")) {
                fullcover.setVisibility(View.GONE);
            }
        }
    }

    public class unfollow extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/deletefollow.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("uemail", user.getEmail());
                postDataParams.put("oemail", otheremail);
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
                follow.setVisibility(View.VISIBLE);
                unfollow.setVisibility(View.GONE);
                rate.setVisibility(View.GONE);
                message.setVisibility(View.GONE);
                new loaddataafterfollow().execute();
            }
        }
    }
    public class rateuser extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/rating2.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("uemail", user.getEmail());
                postDataParams.put("oemail", otheremail);
                postDataParams.put("rating", userrating);
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
                new loaddataafterfollow().execute();
            }
        }
    }
    public class ratefetch extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {

        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/ratingfetch.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("uemail", user.getEmail());
                postDataParams.put("oemail", otheremail);
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
            if (!result.isEmpty()) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray contacts = jsonObj.getJSONArray("result");
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        if (!((c.getString("rating")).isEmpty())) {
                            prerating = Integer.parseInt(c.getString("rating"));
                        }
                    }
                } catch (final JSONException e) {

                }
            } else {


            }
        }
    }
}
