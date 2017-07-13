package com.nullify.travi;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by abhishekpalodath on 21/06/2017.
 */

public class NewTravies extends Service implements NetworkStateReceiver.NetworkStateReceiverListener {
    private Handler handler;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private NetworkStateReceiver networkStateReceiver;
    private DatabaseReference mDatabase;
    public static ArrayList<String> nemail = new ArrayList<String>();
    public static ArrayList<String> ntype = new ArrayList<String>();
    public static ArrayList<String> ndisp = new ArrayList<String>();
    public static ArrayList<String> nloc = new ArrayList<String>();
    public static ArrayList<String> mmail = new ArrayList<String>();
    private Boolean read = true;
    private StorageReference mstorage;
    private String uemail, floc, oemail, dloc, ddis, femail,me,mname,memail,msg;
    private int i = 0,nummsg=0;
    public static int noofnot = 0,countmsg=0,countemail=0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        mAuth = FirebaseAuth.getInstance();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        getApplicationContext().registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        mstorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().getRoot();
        handler = new Handler();handler.post(new Runnable() {
            @Override
            public void run() {
                user = mAuth.getCurrentUser();
                if (user != null)
                {
                    me = user.getEmail();
                    me = me.replace(".", "-");
                    mDatabase.child("Follow").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Iterator i = dataSnapshot.getChildren().iterator();
                            while (i.hasNext()) {
                                oemail = (String) ((DataSnapshot) i.next()).getValue();
                                String status = (String) ((DataSnapshot) i.next()).getValue();
                                uemail = (String) ((DataSnapshot) i.next()).getValue();
                                if ((oemail.equals(user.getEmail())) && status.equals("unread")) {
                                    noofnot++;
                                    if (isForeground("com.nullify.travi")) {
                                        tab1Home.count.setVisibility(View.VISIBLE);
                                        tab1Home.count.setText(String.valueOf(noofnot));
                                    }
                                    new loaddata().execute();
                                    mDatabase.child("Follow").child(dataSnapshot.getKey()).child("status").setValue("read");

                                }
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                mDatabase.child("Places").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        femail = (String) dataSnapshot.child("email").getValue();
                        floc = (String) dataSnapshot.child("loc").getValue();
                        if ((!(femail.equals(user.getEmail()))) && !(dataSnapshot.hasChild(user.getUid()))) {
                            new SendPostRequest().execute();
                            mDatabase.child("Places").child(dataSnapshot.getKey()).child(user.getUid()).setValue("read");
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mDatabase.child("Chats").child(me).child("ind").limitToLast(1).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterator i = dataSnapshot.getChildren().iterator();
                        while (i.hasNext()) {
                            String key = ((DataSnapshot) i.next()).getKey();
                            String nstatus = (String) dataSnapshot.child(key).child("nstatus").getValue();
                            if (nstatus.equals("unread")) {
                                memail = (String) dataSnapshot.child(key).child("email").getValue();
                                if (!mmail.contains(memail)) {
                                    countemail++;
                                    mmail.add(memail);
                                }
                                countmsg++;
                                mDatabase.child("Chats").child(me).child("ind").child(key).child("nstatus").setValue("read");
                                mname = (String) dataSnapshot.child(key).child("name").getValue();
                                msg = (String) dataSnapshot.child(key).child("msg").getValue();
                                if (MessageActivity.active) {
                                    if (MessageActivity.email.equals(memail)) {

                                    } else {
                                        notifymsg(mname, msg);
                                    }
                                } else {
                                    notifymsg(mname, msg);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("YouWillNeverKillMe"));

    }

    @Override
    public void networkAvailable() {

    }

    @Override
    public void networkUnavailable() {

    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

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
                    JSONObject jsonObj = new JSONObject(result);

                    JSONArray contacts = jsonObj.getJSONArray("result");

                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        ddis = c.getString("dis");
                        dloc = c.getString("loc");
                        if (ddis.equals(floc)) {
                            notifyThis(ddis);
                            break;
                        } else if (dloc.equals(floc)) {
                            notifyThis(dloc);
                            break;
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

    public class loaddata extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/travydetails.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("email", uemail);
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
                        ndisp.add(n);
                        notifyfollow(n);
                    }
                } catch (final JSONException e) {

                }
            } else {


            }
        }
    }
    public class loaddataloc extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/travydetails.php");

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
            if (!result.isEmpty()) {
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONArray contacts = jsonObj.getJSONArray("result");
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String n = c.getString("name");
                        ndisp.add(n);
                    }
                } catch (final JSONException e) {

                }
            } else {


            }
        }
    }

    public void notifyThis(String loc) {
        nemail.add(femail);
        ntype.add("new");
        nloc.add(floc);
        new loaddataloc().execute();
        noofnot++;
        if (isForeground("com.nullify.travi")) {
            tab1Home.count.setVisibility(View.VISIBLE);
            tab1Home.count.setText(String.valueOf(noofnot));
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());
        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.noticon)
                .setTicker("New travi is available to" + " " + loc)
                .setContentTitle("Travi")
                .setContentText("New travi is available to" + " " + loc)
                .setContentInfo("");
        b.setPriority(Notification.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= 21) b.setVibrate(new long[0]);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("dis", ddis);
        intent.putExtra("loc", dloc);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        b.setContentIntent(contentIntent);
        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(++i, b.build());
    }

    public void notifyfollow(String name) {
        nemail.add(uemail);
        ntype.add("Follow");
        nloc.add(" ");
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());
        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.noticon)
                .setTicker(name + " started to follow you")
                .setContentTitle("Travi")
                .setContentText(name + " started to follow you")
                .setContentInfo("");
        b.setPriority(Notification.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= 21) b.setVibrate(new long[0]);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("email", uemail);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        b.setContentIntent(contentIntent);
        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(++i, b.build());
    }
    public void notifymsg(String name,String msg) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());
        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.noticon)
                .setContentTitle("Travi")
                .setContentText(countmsg+" messages "+countemail+" chats");
        b.setPriority(Notification.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= 21) b.setVibrate(new long[0]);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("not", "msg");
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        b.setContentIntent(contentIntent);
        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify("msg",1, b.build());
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
}

