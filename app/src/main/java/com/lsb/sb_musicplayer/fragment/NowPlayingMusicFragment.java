package com.lsb.sb_musicplayer.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lsb.sb_musicplayer.R;
import com.lsb.sb_musicplayer.service.MyMusicService;

/**
 * A simple {@link Fragment} subclass.
 */
public class NowPlayingMusicFragment extends Fragment implements View.OnClickListener, MyMusicService.MusicServiceCallback2 {


    private ImageView mNowImage;
    private TextView mTitleText;
    private TextView mArtistText;
    private SeekBar mSeekbar;
    private ImageButton mRepeat;
    private ImageButton mOneRepeat;
    private ImageButton mPervious;
    private ImageButton mPlay;
    private ImageButton mNext;
    private ServiceConnection serviceConnection;
    private MyMusicService mMyService;
    private boolean mBound;
    private MediaPlayer mMediaPlayer;

    public NowPlayingMusicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_now_playing_music, container, false);

        mNowImage = v.findViewById(R.id.now_image);
        mTitleText = v.findViewById(R.id.now_title);
        mArtistText = v.findViewById(R.id.now_artist);

        mSeekbar = v.findViewById(R.id.now_seekBar);
        mOneRepeat = v.findViewById(R.id.now_one_repeat_button);
        mRepeat = v.findViewById(R.id.now_repeat_button);


        mOneRepeat.setOnClickListener(this);
        mRepeat.setOnClickListener(this);


        mPervious = v.findViewById(R.id.now_pervious_button);
        mPlay = v.findViewById(R.id.now_play_button);
        mNext = v.findViewById(R.id.now_next_button);


        final NowPlayingMusicFragment fragment = (NowPlayingMusicFragment) getFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + 0);
        Intent service = new Intent(getContext(), MyMusicService.class);
        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mMyService = ((MyMusicService.MyBinder) iBinder).getService();
                mMyService.setNowCallback(fragment);
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBound = false;
            }
        };
        getContext().bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);

        return v;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.now_one_repeat_button:
                mOneRepeat.setSelected(!mOneRepeat.isSelected());
                break;
            case R.id.now_repeat_button:
                mRepeat.setSelected(!mRepeat.isSelected());
                break;

        }

    }

    public void chageImage(Drawable drawable) {
        mPlay.setImageDrawable(drawable);
    }

    @Override
    public void onNowCallback(MediaPlayer mediaPlayer, boolean play) {
        if (play) {
            Drawable drawable = ActivityCompat.getDrawable(getActivity(), R.drawable.ic_pause_circle_filled_black_24dp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                VectorDrawable vectorDrawable = (VectorDrawable) drawable;
                chageImage(vectorDrawable);
            } else {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                chageImage(bitmapDrawable);
            }
        } else {
            Drawable drawable = ActivityCompat.getDrawable(getActivity(), R.drawable.ic_play_circle_filled_black_24dp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                VectorDrawable vectorDrawable = (VectorDrawable) drawable;
                chageImage(vectorDrawable);
            } else {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                chageImage(bitmapDrawable);
            }
        }
    }
}
