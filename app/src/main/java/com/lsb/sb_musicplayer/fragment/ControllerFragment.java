package com.lsb.sb_musicplayer.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
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

    private ImageView mImageView;
    private TextView mTitleText;
    private TextView mArtistText;
    private ControllerFragment fragment;
    private ServiceConnection serviceConnection;

    public ControllerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent service = new Intent(getContext(), MyMusicService.class);
        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                fragment = (ControllerFragment) getFragmentManager().findFragmentById(R.id.controller_fragment);
                mMyService = ((MyMusicService.MyBinder) iBinder).getService();
                mMyService.setControllerCallback(fragment);

                if (mMyService.mMediaPlayer != null){
                    mMyService.uiUpdata();
                }
                mBound = true;
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBound = false;
            }
        };
        getContext().bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mMyService != null) {
            mMyService.uiChange(mImageView, mTitleText, mArtistText);
        }
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_controller, container, false);

        mImageView = v.findViewById(R.id.controller_image);
        mTitleText = v.findViewById(R.id.controller_title);
        mArtistText = v.findViewById(R.id.controller_artist);

        mPlayButton = (ImageButton) v.findViewById(R.id.play_button);
        mNextButton = (ImageButton) v.findViewById(R.id.next_button);
        mPrevButton = (ImageButton) v.findViewById(R.id.prev_button);


        // 서비스 바인드


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


    // 콜백
    @Override
    public void onControllerCallback(MediaPlayer mediaPlayer, boolean play) {

        mMediaPlayer = mediaPlayer;

        mMyService.palyButtonChange(mPlayButton, play);
        mMyService.uiChange(mImageView, mTitleText, mArtistText);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_button:
                if (mMediaPlayer == null) {

                } else if (mMediaPlayer.isPlaying()) {
                    Intent pause = new Intent(getContext(), MyMusicService.class);
                    pause.setAction(MyMusicService.ACTION_PAUSE);
                    getContext().startService(pause);
                } else {
                    Intent re_start = new Intent(getContext(), MyMusicService.class);
                    re_start.setAction(MyMusicService.ACTION_RESTART);
                    getContext().startService(re_start);
                }
                break;
            case R.id.next_button:
                Intent next = new Intent(getContext(), MyMusicService.class);
                next.setAction(MyMusicService.ACTION_NEXT);
                getContext().startService(next);
                break;
            case R.id.prev_button:
                Intent prev = new Intent(getContext(), MyMusicService.class);
                prev.setAction(MyMusicService.ACTION_PREV);
                getContext().startService(prev);
                break;
        }
    }
}

