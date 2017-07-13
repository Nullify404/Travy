package com.nullify.travi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class ConfirmActivity extends AppCompatActivity {
    private Button cverify, cancell;
    private FirebaseAuth mAuth;
    private String email, pass;
    private CoordinatorLayout coordinate;
    private TextView c1,c2;
    public static final String MyPREFERENCES = "MyPrefs";
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.YELLOW);
        }
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.getString("vd","0").equals("1")){
            startActivity(new Intent(ConfirmActivity.this, ConfirmUsername.class));
            ConfirmActivity.this.finish();
        }
        Bundle bundle = getIntent().getExtras();
        email = bundle.getString("email");
        pass = bundle.getString("pass");
        Typeface galano = Typeface.createFromAsset(getAssets(), "Galano Grotesque DEMO Bold_1481013698090.otf");
        cverify = (Button) findViewById(R.id.confirm_verify_next);
        cancell = (Button) findViewById(R.id.confirm_verify_cancel);
        c1 = (TextView) findViewById(R.id.ct1);
        c2 = (TextView) findViewById(R.id.ct2);
        cverify.setTypeface(galano);
        cancell.setTypeface(galano);
        c1.setTypeface(galano);
        c2.setTypeface(galano);
        coordinate = (CoordinatorLayout) findViewById(R.id.confirm_cordnate);
        mAuth = FirebaseAuth.getInstance();
        cancell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = mAuth.getCurrentUser();
                user.delete();

                mAuth.signOut();
                new DeletePost().execute();
                startActivity(new Intent(ConfirmActivity.this, LoginActivity.class));
                ConfirmActivity.this.finish();
            }
        });
        cverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                cverify.setText("Wait..");
                cverify.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner, 0);
                cverify.setCompoundDrawablePadding(10);
                Drawable progressAnimationLeft = cverify.getCompoundDrawables()[2];
                Animatable animatable = ((Animatable) progressAnimationLeft);
                animatable.start();
                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(ConfirmActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user.isEmailVerified()) {
                                        SharedPreferences.Editor editor;
                                        editor = sharedpreferences.edit();
                                        editor.putString("vd", "1");
                                        editor.commit();
                                        startActivity(new Intent(ConfirmActivity.this, ConfirmUsername.class));
                                        ConfirmActivity.this.finish();
                                    } else {
                                        Snackbar snackbar = Snackbar
                                                .make(coordinate, "Email is not verified please try after verify ", Snackbar.LENGTH_LONG);

                                        snackbar.show();
                                        cverify.setText("NEXT");
                                        cverify.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public class DeletePost extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            cancell.setText("Wait..");
            cancell.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner, 0);
            cancell.setCompoundDrawablePadding(10);
            Drawable progressAnimationLeft = cancell.getCompoundDrawables()[2];
            Animatable animatable = ((Animatable) progressAnimationLeft);
            animatable.start();
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/delete.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("email", email);
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
