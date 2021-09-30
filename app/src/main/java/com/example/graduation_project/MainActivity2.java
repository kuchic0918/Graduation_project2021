package com.example.graduation_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity2 extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    CommunityFragment fragment1;
    WalkFragment fragment2;
    MapFragment fragment3;
    InfoFragment fragment4;
    CalendarFragment fragment5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        fragment1 = new CommunityFragment();
        fragment2 = new WalkFragment();
        fragment3 = new MapFragment();
        fragment4 = new InfoFragment();
        fragment5 = new CalendarFragment();


        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout,fragment1).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.navigation_community : {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout,fragment1).commitAllowingStateLoss();
                        return true;


                    }

                    case R.id.navigation_walk : {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout,fragment2).commitAllowingStateLoss();
                        return true;
                    }

                    case R.id.navigation_map : {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout,fragment3).commitAllowingStateLoss();
                        return true;
                    }

                    case R.id.navigation_info : {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout,fragment4).commitAllowingStateLoss();
                        return true;
                    }

                    case R.id.navigation_calendar : {
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout,fragment5).commitAllowingStateLoss();
                        return true;
                    }
                    default: return false;

                }
            }
        });
    }

}