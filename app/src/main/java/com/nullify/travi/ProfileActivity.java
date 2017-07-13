package com.nullify.travi;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ProfileActivity extends Fragment implements NetworkStateReceiver.NetworkStateReceiverListener{
    private ImageView cover;
    private RelativeLayout rootview;
    private TextView user_name, user_country,following,followers,rating,about_user;
    private Button profile_button_edit;
    private Animation left2right, left2right2;
    private NetworkStateReceiver networkStateReceiver;
    private ImageButton edit;
    private FirebaseAuth mAuth;
    private StorageReference mstorage;
    private FirebaseUser user;
    FrameLayout loading;
    private ProgressBar pbar;
    TextView profile_image_avatar;
    WishlistAdapter wishlistAdapter;
    NonScrollListView wishlist;
    String dloc;
    ArrayList<String> wloc = new ArrayList<>();
    ArrayList<String> wdis = new ArrayList<>();
    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_user_profile, container, false);
        mstorage = FirebaseStorage.getInstance().getReference();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        rootview = (RelativeLayout) rootView.findViewById(R.id.profile_rootView);
        pbar = (ProgressBar) rootView.findViewById(R.id.profile_progress_user);
        user_name = (TextView) rootView.findViewById(R.id.profile_user_name);
        followers = (TextView) rootView.findViewById(R.id.profile_user_no_followers);
        following = (TextView) rootView.findViewById(R.id.profile_user_no_following);
        rating = (TextView) rootView.findViewById(R.id.profile_rating);
        user_country = (TextView) rootView.findViewById(R.id.profile_user_country);
        profile_button_edit = (Button) rootView.findViewById(R.id.profile_button_edit);
        edit = (ImageButton) rootView.findViewById(R.id.edit_profile_cover);
        loading = (FrameLayout) rootview.findViewById(R.id.loading_cover);
        loading.setVisibility(View.VISIBLE);
        about_user = (TextView) rootView.findViewById(R.id.about_user);
        wishlist = (NonScrollListView) rootView.findViewById(R.id.wish_list);
        wishlistAdapter = new WishlistAdapter(getActivity(),wloc,wdis);
        wishlist.setAdapter(wishlistAdapter);
        mstorage.child("pro_pics/" + mAuth.getCurrentUser().getEmail() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        profile_image_avatar.setBackground(circularBitmapDrawable);
                        profile_image_avatar.setText("");
                    }
                });
                pbar.setVisibility(View.GONE);
                rootview.findViewById(R.id.loading_cover).setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                pbar.setVisibility(View.GONE);
                rootview.findViewById(R.id.loading_cover).setVisibility(View.GONE);

            }
        });
        mstorage.child("cover_pics/" + mAuth.getCurrentUser().getEmail() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
        setListViewHeightBasedOnChildren(wishlist);
        SwipeDismissList.UndoMode mode = SwipeDismissList.UndoMode.SINGLE_UNDO;
        SwipeDismissList.OnDismissCallback callback = new SwipeDismissList.OnDismissCallback() {
            @Override
            public SwipeDismissList.Undoable onDismiss(AbsListView listView, int position) {
                final String locToDelete = wloc.get(position);
                dloc = locToDelete;
                wloc.remove(position);
                wdis.remove(position);
                wishlistAdapter.notifyDataSetChanged();
                new dltwl().execute();
                return null;
            }
        };
        wishlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment travy = new TravysList();
                Bundle bundle = new Bundle();
                bundle.putString("dis", wdis.get(position));
                bundle.putString("loc", wloc.get(position));
                travy.setArguments(bundle);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.profile_root, travy).addToBackStack("tag").commit();
            }
        });
        SwipeDismissList swipeList = new SwipeDismissList(wishlist, callback, mode);
        new loaddata().execute();


        profile_image_avatar = (TextView) rootView.findViewById(R.id.profile_image_avatar);
        profile_image_avatar.setText(String.valueOf(user.getDisplayName().charAt(0)).toUpperCase());
        cover = (ImageView) rootView.findViewById(R.id.profile_toolbar_cover);
        rootview.findViewById(R.id.loading_cover).setVisibility(View.VISIBLE);
        rootview.findViewById(R.id.profile_layout_followers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),FollowersActivity.class);
                intent.putExtra("email",user.getEmail());
                startActivity(intent);
            }
        });
        rootview.findViewById(R.id.profile_layout_following).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),FollowingActivity.class);
                intent.putExtra("email",user.getEmail());
                startActivity(intent);
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] items = {
                        "Profile picture", "Cover picture"
                };
                final Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.profile_dialogue_proco);
                Typeface geomatos = Typeface.createFromAsset(getContext().getAssets(), "Galano Grotesque DEMO Bold_1481013698090.otf");
                ListView proco = (ListView) dialog.findViewById(R.id.list_pro_co);
                ArrayAdapter adapter = new ArrayAdapter<String>(getContext(), R.layout.custom_list, items);
                proco.setAdapter(adapter);
                proco.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                if (ContextCompat.checkSelfPermission(getContext(),
                                        android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                                        .checkSelfPermission(getContext(),
                                                android.Manifest.permission.CAMERA)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(new String[]{
                                                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    android.Manifest.permission.CAMERA},
                                            0);
                                }
                                dialog.dismiss();
                                uploadPropic();
                                break;
                            case 1:
                                dialog.dismiss();
                                uploadCoverpic();
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });
        profile_button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),EditProfileActivity.class));
            }
        });
        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        new loaddata().execute();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    FragmentManager fm = getChildFragmentManager();
                    HomeActivity.viewPager.setCurrentItem(0,false);

                    return true;
                }
                return false;
            }
        });


    }

    private void uploadCoverpic() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, 2);
    }

    private void uploadPropic() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, 0);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            pbar.setVisibility(View.VISIBLE);
            final Uri uri = data.getData();
            cropImage(uri);
        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            pbar.setVisibility(View.VISIBLE);
            final Uri uri = data.getData();
            StorageReference filepath = mstorage.child("pro_pics").child(mAuth.getCurrentUser().getEmail() + ".jpg");
            filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Glide.with(getContext()).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profile_image_avatar.setBackground(circularBitmapDrawable);
                            profile_image_avatar.setText(" ");
                        }

                    });
                    pbar.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Some thing went wrong", Toast.LENGTH_SHORT).show();
                    pbar.setVisibility(View.GONE);
                }
            });

        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            final Uri uri = data.getData();
            StorageReference filepath = mstorage.child("cover_pics").child(mAuth.getCurrentUser().getEmail() + ".jpg");
            filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Glide.with(ProfileActivity.this)
                            .load(uri)
                            .centerCrop()
                            .into(cover);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Some thing went wrong", Toast.LENGTH_SHORT).show();
                    pbar.setVisibility(View.GONE);
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

        startActivityForResult(CropIntent, 1);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void networkAvailable() {
        new loaddata().execute();
        rootView.findViewById(R.id.uprofile_network_warning).setVisibility(View.GONE);
    }

    @Override
    public void networkUnavailable() {
        rootView.findViewById(R.id.uprofile_network_warning).setVisibility(View.VISIBLE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser){
            new loaddata().execute();
        }
    }

    public class loaddata extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/travydetails.php");

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
            if (!result.isEmpty()) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray contacts = jsonObj.getJSONArray("result");
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String n = c.getString("name");
                        String f1 = c.getString("followers");
                        String f2 = c.getString("following");
                        String about = c.getString("about");
                        float a = Float.parseFloat(c.getString("rating"));
                        rating.setText(String.format(Locale.US,"%.2f", a)+"/5");
                        about_user.setText(about);
                        followers.setText(f1);
                        following.setText(f2);
                        user_name.setText(n);
                        user_country.setText(user.getEmail());
                    }
                } catch (final JSONException e) {

                }
                new loadwl().execute();
            } else {


            }
        }
    }
    public class loadwl extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/loadwl.php");

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
            if (!result.isEmpty()) {
                try {
                    wloc.clear();
                    wdis.clear();
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray contacts = jsonObj.getJSONArray("result");
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String dis = c.getString("dis");
                        String loc = c.getString("loc");
                        if (loc == null || loc.isEmpty()){
                            wloc.add(dis);
                            wdis.add(dis);
                            wishlistAdapter.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(wishlist);
                        }else if (dis == null || dis.isEmpty()){
                            wloc.add(loc);
                            wdis.add(loc);
                            wishlistAdapter.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(wishlist);
                        }
                        else {
                            wloc.add(loc);
                            wdis.add(dis);
                            wishlistAdapter.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(wishlist);
                        }

                    }
                } catch (final JSONException e) {

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
    public class dltwl extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/deletewl.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("email", user.getEmail());
                postDataParams.put("loc", dloc);
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
                    wloc.clear();
                    wdis.clear();
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray contacts = jsonObj.getJSONArray("result");
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String dis = c.getString("dis");
                        String loc = c.getString("loc");
                        if (loc == null || loc.isEmpty()){
                            wloc.add(dis);
                            wdis.add("");
                            wishlistAdapter.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(wishlist);
                        }else {
                            wloc.add(loc);
                            wdis.add(dis);
                            wishlistAdapter.notifyDataSetChanged();
                            setListViewHeightBasedOnChildren(wishlist);
                        }

                    }
                } catch (final JSONException e) {

                }
            } else {


            }
        }
    }
    public static void reload(){

    }

}
