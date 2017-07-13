package com.nullify.travi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ConfirmUsername extends AppCompatActivity {
    private TextView profile_pic;
    private ImageButton profile_pic_edit;
    private EditText username;
    private FirebaseAuth mAuth;
    private Button proceed;
    private TextInputLayout user_hint;
    private StorageReference mstorage;
    private ProgressBar pbar;
    private static final int GALLARY_INT = 2;
    private FirebaseUser user;
    public static final String MyPREFERENCES = "MyPrefs";
    private SharedPreferences sharedpreferences;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_username);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.YELLOW);
        }
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (sharedpreferences.getString("done", "0").equals("1")) {
            startActivity(new Intent(ConfirmUsername.this, HomeActivity.class));
            ConfirmUsername.this.finish();
        }
        mstorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


        Typeface galano = Typeface.createFromAsset(getAssets(), "Galano Grotesque DEMO Bold_1481013698090.otf");
        profile_pic = (TextView) findViewById(R.id.confirm_prof_pic);
        profile_pic_edit = (ImageButton) findViewById(R.id.confirm_pro_edit_btn);
        username = (EditText) findViewById(R.id.confirm_username);
        proceed = (Button) findViewById(R.id.confirm_btn_proceed);
        user_hint = (TextInputLayout) findViewById(R.id.text_input_confirm_username);
        pbar = (ProgressBar) findViewById(R.id.confirm_profile_loading);

        username.setTypeface(galano);
        proceed.setTypeface(galano);
        user_hint.setTypeface(galano);

        profile_pic.setText(String.valueOf(user.getDisplayName().charAt(0)));

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor;
                editor = sharedpreferences.edit();
                editor.putString("done", "1");
                editor.commit();
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username.getText().toString()).build();
                user.updateProfile(profileUpdates);
                startActivity(new Intent(ConfirmUsername.this, HomeActivity.class));
                ConfirmUsername.this.finish();
            }
        });

        username.setText(user.getDisplayName());
        mstorage.child("pro_pics/" + mAuth.getCurrentUser().getEmail() + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        profile_pic.setBackground(circularBitmapDrawable);
                        profile_pic.setText("");
                    }
            });
                pbar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                pbar.setVisibility(View.GONE);
            }
        });

        profile_pic_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ConfirmUsername.this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                        .checkSelfPermission(ConfirmUsername.this,
                                android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.CAMERA},
                            0);
                }
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, GALLARY_INT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_INT && resultCode == RESULT_OK) {
            pbar.setVisibility(View.VISIBLE);
            final Uri uri = data.getData();
            cropImage(uri);
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            StorageReference filepath = mstorage.child("pro_pics").child(mAuth.getCurrentUser().getEmail() + ".jpg");
            filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Glide.with(getApplicationContext()).load(uri).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getApplicationContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profile_pic.setBackground(circularBitmapDrawable);
                        }
                });
                    pbar.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Some thing went wrong", Toast.LENGTH_SHORT).show();
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

        startActivityForResult(CropIntent, 3);
    }
}
