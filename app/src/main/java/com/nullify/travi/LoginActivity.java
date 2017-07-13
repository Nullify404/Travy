package com.nullify.travi;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText emailInput, passwordInput, snameInput, semailInput, spasswordInput, sdob;
    private TextInputLayout t1, t2, t3, t4, t5, t6, t7;
    private static final int RC_SIGN_IN = 1;
    public static final String MyPREFERENCES = "MyPrefs" ;
    private GoogleApiClient mGoogleApiClient;
    private Button fb, ggl, signin, signup, ssignup;
    private LoginButton fbloginButton;
    private CallbackManager callBackManager;
    private FirebaseAuth mAuth;
    private CoordinatorLayout loginco;
    private int i = 0;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "LoginActivity";
    private TextView or, ady, shead;
    private View previousView, currentView;
    private int currentTab, mYear, mMonth, mDay, age;
    private TabHost host;
    private static final int ANIMATION_TIME = 240;
    private String name, email, pass, lemail, lpass;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.login_signup);

        Typeface geomatos = Typeface.createFromAsset(getAssets(), "Galano Grotesque DEMO Bold_1481013698090.otf");
        Typeface cinematography = Typeface.createFromAsset(getAssets(), "Cinematografica-Bold-trial.ttf");

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        loginco = (CoordinatorLayout) findViewById(R.id.login_coordinator);
        snameInput = (EditText) findViewById(R.id.signup_edittext_name);
        semailInput = (EditText) findViewById(R.id.signup_edittext_email);
        sdob = (EditText) findViewById(R.id.signup_edittext_dob);
        spasswordInput = (EditText) findViewById(R.id.signup_edittext_password);
        ssignup = (Button) findViewById(R.id.signup_button_signup);
        ady = (TextView) findViewById(R.id.signup_already);
        shead = (TextView) findViewById(R.id.signup_heading);
        host = (TabHost) findViewById(R.id.tabHost);
        emailInput = (EditText) findViewById(R.id.login_edittext_email);
        fbloginButton = (LoginButton) findViewById(R.id.fbloginButton);
        passwordInput = (EditText) findViewById(R.id.login_edittext_password);
        t1 = (TextInputLayout) findViewById(R.id.text_input_layout_email);
        t2 = (TextInputLayout) findViewById(R.id.text_input_layout_password);
        t3 = (TextInputLayout) findViewById(R.id.text_input_signup_email);
        t4 = (TextInputLayout) findViewById(R.id.text_input_signup_name);
        t6 = (TextInputLayout) findViewById(R.id.text_input_signup_password);
        t7 = (TextInputLayout) findViewById(R.id.text_input_signup_dob);
        t5 = (TextInputLayout) findViewById(R.id.text_input_layout_password);
        signin = (Button) findViewById(R.id.login_button_signin);
        signup = (Button) findViewById(R.id.login_button_signup);
        fb = (Button) findViewById(R.id.login_fb);
        ggl = (Button) findViewById(R.id.login_ggl);
        or = (TextView) findViewById(R.id.login_or_text);

        snameInput.setTypeface(geomatos);
        semailInput.setTypeface(geomatos);
        spasswordInput.setTypeface(geomatos);
        ssignup.setTypeface(geomatos);
        shead.setTypeface(geomatos);
        ady.setTypeface(geomatos);
        emailInput.setTypeface(geomatos);
        passwordInput.setTypeface(geomatos);
        t1.setTypeface(geomatos);
        t2.setTypeface(geomatos);
        t3.setTypeface(geomatos);
        t4.setTypeface(geomatos);
        sdob.setTypeface(geomatos);
        t5.setTypeface(geomatos);
        t6.setTypeface(geomatos);
        t7.setTypeface(geomatos);
        signin.setTypeface(geomatos);
        signup.setTypeface(geomatos);
        fb.setTypeface(cinematography);
        ggl.setTypeface(cinematography);
        or.setTypeface(geomatos);
        fb.setTextScaleX(1);


        host.setup();
        TabHost.TabSpec spec = host.newTabSpec("login");
        spec.setContent(R.id.login_tab);
        spec.setIndicator("Tab One");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("signup");
        spec.setContent(R.id.signup_tab);
        spec.setIndicator("Tab Two");
        host.addTab(spec);

        previousView = host.getCurrentView();
        currentTab = host.getCurrentTab();

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                currentView = host.getCurrentView();
                if (host.getCurrentTab() > currentTab) {
                    previousView.setAnimation(outToLeftAnimation());
                    currentView.setAnimation(inFromRightAnimation());
                } else {
                    previousView.setAnimation(outToRightAnimation());
                    currentView.setAnimation(inFromLeftAnimation());
                }
                previousView = currentView;
                currentTab = host.getCurrentTab();
            }
        });


        callBackManager = CallbackManager.Factory.create();

        ady.setOnClickListener(this);
        ssignup.setOnClickListener(this);
        fb.setOnClickListener(this);
        ggl.setOnClickListener(this);
        signup.setOnClickListener(this);
        signin.setOnClickListener(this);
        sdob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager =
                        (InputMethodManager) LoginActivity.this.getSystemService(
                                Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(
                        LoginActivity.this.getCurrentFocus().getWindowToken(), 0);
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(LoginActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                age = mYear - year;
                                sdob.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        fbloginButton.setReadPermissions("email", "public_profile");
        fbloginButton.registerCallback(callBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                LayoutInflater inflater1 = getLayoutInflater();
                View layout = inflater1.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.toast_root));
                TextView msg = (TextView) layout.findViewById(R.id.toast_msg);
                msg.setText("Some thing went wrong");
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.BOTTOM,0,20);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }

            @Override
            public void onError(FacebookException error) {
                LayoutInflater inflater1 = getLayoutInflater();
                View layout = inflater1.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.toast_root));
                TextView msg = (TextView) layout.findViewById(R.id.toast_msg);
                msg.setText("Some thing went wrong");
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.BOTTOM,0,20);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                toast.show();
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    if (sharedpreferences.getString("mode","").equals("ep")) {
                        Intent i = new Intent(LoginActivity.this, ConfirmActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("email", email);
                        bundle.putString("pass", pass);
                        i.putExtras(bundle);
                        startActivity(i);
                        LoginActivity.this.finish();
                    }
                    else {
                        startActivity(new Intent(LoginActivity.this,ConfirmUsername.class));
                        LoginActivity.this.finish();
                    }

                }
            }
        };
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        LayoutInflater inflater1 = getLayoutInflater();
                        View layout = inflater1.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.toast_root));
                        TextView msg = (TextView) layout.findViewById(R.id.toast_msg);
                        msg.setText("Some thing went wrong");
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM,0,20);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout);
                        toast.show();
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        /*logingglsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });*/
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    LayoutInflater inflater1 = getLayoutInflater();
                    View layout = inflater1.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.toast_root));
                    TextView msg = (TextView) layout.findViewById(R.id.toast_msg);
                    msg.setText("Some thing went wrong");
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM,0,20);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();
                } else {
                    FirebaseUser user = mAuth.getCurrentUser();
                    email = user.getEmail();
                    name = user.getDisplayName();
                    new SendsignupPostRequest().execute();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        } else {
            callBackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            email = user.getEmail();
                            name = user.getDisplayName();
                            new SendsignupPostRequest().execute();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            LayoutInflater inflater1 = getLayoutInflater();
                            View layout = inflater1.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.toast_root));
                            TextView msg = (TextView) layout.findViewById(R.id.toast_msg);
                            msg.setText("Authentication failed");
                            Toast toast = new Toast(getApplicationContext());
                            toast.setGravity(Gravity.BOTTOM,0,20);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setView(layout);
                            toast.show();
                        }

                        // ...
                    }
                });

    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor;
        switch (v.getId()) {
            case R.id.login_ggl:
                editor = sharedpreferences.edit();
                editor.putString("mode", "ggl");
                editor.commit();
                signIn();
                break;
            case R.id.login_fb:
                editor = sharedpreferences.edit();
                editor.putString("mode", "fb");
                editor.commit();
                fbloginButton.performClick();
                break;
            case R.id.login_button_signin:
                editor = sharedpreferences.edit();
                editor.putString("mode", "signin");
                editor.commit();
                signin.setText("Wait..");
                signin.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner, 0);
                signin.setCompoundDrawablePadding(10);
                Drawable progressAnimationLeft = signin.getCompoundDrawables()[2];
                Animatable animatable = ((Animatable) progressAnimationLeft);
                animatable.start();
                lemail = emailInput.getText().toString();
                lpass = passwordInput.getText().toString();
                if (lemail.isEmpty() || lpass.isEmpty()) {
                    Snackbar snackbar = Snackbar
                            .make(loginco, "Invalid credentials!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    LoginUser();
                }
                break;
            case R.id.login_button_signup:
                host.setCurrentTab(1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.YELLOW);
                }
                break;
            case R.id.signup_button_signup:
                editor = sharedpreferences.edit();
                editor.putString("mode", "ep");
                editor.commit();
                name = snameInput.getText().toString();
                email = semailInput.getText().toString();
                pass = spasswordInput.getText().toString();
                if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || (sdob.getText().toString()).isEmpty()) {
                    Snackbar snackbar = Snackbar
                            .make(loginco, "Please input all fields", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (!email.contains("@") || !email.contains(".com")) {
                    Snackbar snackbar = Snackbar
                            .make(loginco, "Please enter a valid email", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (pass.length() < 6) {
                    Snackbar snackbar = Snackbar
                            .make(loginco, "Password must be atleast 6 letter long", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (age < 13) {
                    Snackbar snackbar = Snackbar
                            .make(loginco, "You can't use our service because of your age", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    ViewDialog alert = new ViewDialog();
                    alert.showDialog(LoginActivity.this);

                }
                break;
            case R.id.signup_already:
                host.setCurrentTab(0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.TRANSPARENT);
                }
                break;
        }
    }

    private void LoginUser() {
        mAuth.signInWithEmailAndPassword(lemail, lpass)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            signin.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            signin.setText("SIGN IN");
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Snackbar snackbar = Snackbar
                                    .make(loginco, "Authentication failed", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }

                        // ...
                    }
                });
    }

    private Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        return setProperties(inFromRight);
    }

    private Animation outToRightAnimation() {
        Animation outToRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        return setProperties(outToRight);
    }

    private Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        return setProperties(inFromLeft);
    }

    private Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        return setProperties(outtoLeft);
    }

    private Animation setProperties(Animation animation) {
        animation.setDuration(ANIMATION_TIME);
        animation.setInterpolator(new AccelerateInterpolator());
        return animation;
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            ssignup.setText("Wait..");
            ssignup.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner, 0);
            ssignup.setCompoundDrawablePadding(10);
            Drawable progressAnimationLeft = ssignup.getCompoundDrawables()[2];
            Animatable animatable = ((Animatable) progressAnimationLeft);
            animatable.start();
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/TraviSignUp.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("name", name);
                postDataParams.put("email", email);
                postDataParams.put("pass", pass);
                postDataParams.put("age", String.valueOf(age));
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
                ssignup.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                ssignup.setText("SIGN UP");
                Snackbar snackbar = Snackbar
                        .make(loginco, "Email is already registered try to ", Snackbar.LENGTH_LONG)
                        .setAction("Login", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                host.setCurrentTab(0);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Window window = getWindow();
                                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                                    window.setStatusBarColor(Color.TRANSPARENT);
                                }
                            }
                        });

                snackbar.show();
            } else if (result.equals("1")) {
                AddfirbaseUser();

            }
        }
    }

    public class SendsignupPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            signin.setText("Wait..");
            signin.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner, 0);
            signin.setCompoundDrawablePadding(10);
            Drawable progressAnimationLeft = signin.getCompoundDrawables()[2];
            Animatable animatable = ((Animatable) progressAnimationLeft);
            animatable.start();
        }

        protected String doInBackground(String... arg0) {

            try {

                URL url = new URL("http://www.nullify.in/travi/TraviSignUp.php");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("name", name);
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

    public void AddfirbaseUser() {
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            host.setCurrentTab(0);
                            snameInput.setText("");
                            semailInput.setText("");
                            spasswordInput.setText("");
                            sdob.setText("");
                            ssignup.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            ssignup.setText("SIGN UP");
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                            user.updateProfile(profileUpdates);
                            Snackbar snackbar = Snackbar
                                    .make(loginco, "A verification mail is send to registered mail, Please Verify!", Snackbar.LENGTH_LONG);
                            snackbar.show();
                            if (!user.isEmailVerified()) {
                                user.sendEmailVerification();
                            }

                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void CheckforVerify(FirebaseUser user) {
        if (user.isEmailVerified()) {
            startActivity(new Intent(LoginActivity.this, tab1Home.class));
            LoginActivity.this.finish();
        } else {
            signin.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            signin.setText("SIGN IN");
            Snackbar snackbar = Snackbar
                    .make(loginco, "Email is not verified", Snackbar.LENGTH_LONG);
            snackbar.show();
            FirebaseAuth.getInstance().signOut();

        }
    }

    public class ViewDialog {

        public void showDialog(Activity activity) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.confirm_mail);
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            Double width = metrics.widthPixels * .9;
            Double height = metrics.heightPixels * .6;
            Window win = dialog.getWindow();
            win.setLayout(width.intValue(), height.intValue());
            Typeface geomatos = Typeface.createFromAsset(getAssets(), "Galano Grotesque DEMO Bold_1481013698090.otf");
            TextView t1 = (TextView) dialog.findViewById(R.id.text_dialog1);
            t1.setTypeface(geomatos);
            TextView t2 = (TextView) dialog.findViewById(R.id.text_dialog2);
            t2.setTypeface(geomatos);
            final EditText cemail = (EditText) dialog.findViewById(R.id.confirm_mail);
            cemail.setText(email);
            cemail.setTypeface(geomatos);
            cemail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    email = cemail.getText().toString();
                }
            });
            Button dialogButton = (Button) dialog.findViewById(R.id.confirm_button_email);
            dialogButton.setTypeface(geomatos);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SendPostRequest().execute();
                    dialog.dismiss();
                    semailInput.setText(email);

                }
            });

            dialog.show();

        }
    }

    @Override
    public void onBackPressed() {
        if (host.getCurrentTab() == 1) {
            host.setCurrentTab(0);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (i == 1) {
            LoginActivity.this.finish();
        } else {
            Toast.makeText(getApplicationContext(), "One more to exit", Toast.LENGTH_SHORT).show();
            i++;
        }
    }
}
