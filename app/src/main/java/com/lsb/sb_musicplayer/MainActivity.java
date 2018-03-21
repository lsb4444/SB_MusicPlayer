package com.lsb.sb_musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;

import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.lsb.sb_musicplayer.fragment.MusicListFragment;
import com.lsb.sb_musicplayer.fragment.NowPlayingMusicFragment;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1000;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        임시 권한 체크
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }

        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        final Fragment mControllerFrag = getSupportFragmentManager().findFragmentById(R.id.controller_fragment);



        mViewPager.setAdapter(new MyViewPager(getSupportFragmentManager()));
        mViewPager.setCurrentItem(1);

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.hide(mControllerFrag);
                    ft.commit();
                } else {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.show(mControllerFrag);
                    ft.commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private static class MyViewPager extends FragmentPagerAdapter {

        private static final int PAGE_NUM = 2;

        public MyViewPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:

                    return new NowPlayingMusicFragment();
                case 1:
                    return new MusicListFragment();

            }
            return null;
        }

        @Override
        public int getCount() {
            return PAGE_NUM;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "플레이";
                case 1:
                    return "노래";
            }
            return super.getPageTitle(position);
        }
    }

    public ViewPager getViewPager() {
        if (null == mViewPager) {
            mViewPager = (ViewPager) findViewById(R.id.view_pager);
        }
        return mViewPager;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //임시 권한 체크
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "권한 거절 됨", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
