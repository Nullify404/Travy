package com.nullify.travi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class tab1Home extends Fragment implements View.OnClickListener {
    RelativeLayout home_layout_explore, home_layout_profile, home_layout_view;
    Button home_button_explore, home_button_travies, home_button_profile;
    private RelativeLayout notification,settings;
    public static TextView count;
    public static Activity z;
    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_home,container,false);
        setRetainInstance(true);
        z=getActivity();
        count = (TextView) rootView.findViewById(R.id.notification_count);
        count.setVisibility(View.GONE);
        home_button_explore = (Button) rootView.findViewById(R.id.home_button_explore);
        home_button_travies = (Button) rootView.findViewById(R.id.home_button_travies);
        home_button_profile = (Button) rootView.findViewById(R.id.home_button_profile);
        notification = (RelativeLayout) rootView.findViewById(R.id.home_notification);
        settings = (RelativeLayout) rootView.findViewById(R.id.home_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),SettingsActivity.class));
            }
        });
        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),NotificationsActivity.class));
            }
        });

        Typeface geomatos = Typeface.createFromAsset(getContext().getAssets(), "Geometos_1481025580932.ttf");
        home_button_explore.setTypeface(geomatos);
        home_button_profile.setTypeface(geomatos);
        home_button_travies.setTypeface(geomatos);


        home_button_profile.setOnClickListener(this);
        home_button_travies.setOnClickListener(this);
        home_button_explore.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_button_profile:
                HomeActivity.viewPager.setCurrentItem(1,false);
                break;
            case R.id.home_button_explore:
                HomeActivity.viewPager.setCurrentItem(2,false);
                break;
            case R.id.home_button_travies:
                HomeActivity.viewPager.setCurrentItem(3,false);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity().getIntent().hasExtra("dis")) {
            HomeActivity.viewPager.setCurrentItem(0);
            Fragment travy = new TravysList();
            Bundle bundle = new Bundle();
            bundle.putString("dis", getActivity().getIntent().getExtras().getString("dis"));
            bundle.putString("loc", getActivity().getIntent().getExtras().getString("loc"));
            travy.setArguments(bundle);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.rootView, travy).addToBackStack("tag").commit();
        }
        else if (getActivity().getIntent().hasExtra("loc")){
            HomeActivity.viewPager.setCurrentItem(0);
            Fragment travy = new TravysList();
            Bundle bundle = new Bundle();
            bundle.putString("dis", " ");
            bundle.putString("loc", getActivity().getIntent().getExtras().getString("loc"));
            travy.setArguments(bundle);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.rootView, travy).addToBackStack("tag").commit();
        }else if (getActivity().getIntent().hasExtra("email")){
            Fragment profile = new ProfileActivityOthers();
            Bundle bundle = new Bundle();
            bundle.putString("email", getActivity().getIntent().getExtras().getString("email"));
            profile.setArguments(bundle);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.rootView, profile).addToBackStack("tag").commit();
        }else if (getActivity().getIntent().hasExtra("not")){
            HomeActivity.viewPager.setCurrentItem(4);
        }
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    FragmentManager fm = getChildFragmentManager();
                    if (HomeActivity.viewPager.getCurrentItem() == 0 && fm.getBackStackEntryCount() ==0){
                        getActivity().finish();
                    }else {
                        fm.popBackStackImmediate();
                    }

                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
