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
import android.widget.TextView;


import com.lsb.sb_musicplayer.R;
import com.lsb.sb_musicplayer.service.MyMusicService;

/**
 * A simple {@link Fragment} subclass.
 */
public class ControllerFragment extends Fragment implements MyMusicService.MusicServiceCallback, View.OnClickListener {


    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private MyMusicService mMyService;
    boolean mBound;
    public MediaPlayer mMediaPlayer;
    private ServiceConnection serviceConnection;
    private ImageView mImageView;
    private TextView mTitleText;
    private TextView mArtistText;

    public ControllerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_controller, container, false);

        mImageView = v.findViewById(R.id.controller_image);
        mTitleText = v.findViewById(R.id.controller_title);
        mArtistText = v.findViewById(R.id.controller_artist);

        mPlayButton = (ImageButton) v.findViewById(R.id.play_button);
        mNextButton = (ImageButton) v.findViewById(R.id.next_button);
        mPrevButton = (ImageButton) v.findViewById(R.id.prev_button);


        // 서비스 바인드
        final ControllerFragment fragment = (ControllerFragment) getFragmentManager().findFragmentById(R.id.controller_fragment);
        Intent service = new Intent(getContext(), MyMusicService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mMyService = ((MyMusicService.MyBinder) iBinder).getService();
                mMyService.setControllerCallback(fragment);
                mBound = true;

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBound = false;
            }
        };
        getContext().bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);

        mPlayButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);
        return v;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBound) {
            getContext().unbindService(serviceConnection);

        }
    }


    // 버튼 이미지 체인지
    public void chageImage(Drawable drawable) {
        mPlayButton.setImageDrawable(drawable);
    }

    // 콜백
    @Override
    public void onControllerCallback(MediaPlayer mediaPlayer, boolean play) {
        mMediaPlayer = mediaPlayer;

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

        mMyService.uiChange(mImageView, mTitleText, mArtistText);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button:
                if (mMediaPlayer == null) {

                } else if (mMediaPlayer.isPlaying()) {
                    mMyService.pause();
                } else {
                    mMyService.reStart();
                }
                break;
            case R.id.next_button:
                mMyService.next();
                break;
            case R.id.prev_button:
                mMyService.peve();
                break;
        }
    }
}

