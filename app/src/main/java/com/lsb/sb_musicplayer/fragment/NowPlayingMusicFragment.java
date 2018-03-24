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
    private TextView mNowTime;
    private TextView mMaxTime;

    private ImageButton mRepeat;
    private ImageButton mOneRepeat;
    private ImageButton mPervious;
    private ImageButton mPlay;
    private ImageButton mNext;
    private ServiceConnection serviceConnection;
    private MyMusicService mMyService;
    private boolean mBound;
    private MediaPlayer mMediaPlayer;
    private NowPlayingMusicFragment fragment;

    public NowPlayingMusicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent service = new Intent(getContext(), MyMusicService.class);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mMyService = ((MyMusicService.MyBinder) iBinder).getService();
                fragment = (NowPlayingMusicFragment) getFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + 0);
                mMyService.setNowCallback(fragment);
                mBound = true;
                if (mMyService.mMediaPlayer != null){
                    mMyService.uiUpdata();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBound = false;
            }
        };
        getActivity().bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onDestroy() {
        if (mBound) {
            getContext().unbindService(serviceConnection);
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_now_playing_music, container, false);

        mNowImage = v.findViewById(R.id.now_image);
        mTitleText = v.findViewById(R.id.now_title);
        mArtistText = v.findViewById(R.id.now_artist);

        mSeekbar = v.findViewById(R.id.now_seekBar);
        mMaxTime = v.findViewById(R.id.max_time_view);
        mNowTime = v.findViewById(R.id.now_time_view);


        mOneRepeat = v.findViewById(R.id.now_one_repeat_button);
        mRepeat = v.findViewById(R.id.now_repeat_button);


        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMyService.getMediaPlayer().seekTo(mSeekbar.getProgress());
            }
        });
        mOneRepeat.setOnClickListener(this);
        mRepeat.setOnClickListener(this);


        mPlay = v.findViewById(R.id.now_play_button);
        mNext = v.findViewById(R.id.now_next_button);
        mPervious = v.findViewById(R.id.now_pervious_button);

        mPlay.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPervious.setOnClickListener(this);
        mOneRepeat.setOnClickListener(this);
        mRepeat.setOnClickListener(this);


        return v;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.now_one_repeat_button:
                if (!mRepeat.isSelected()) {
                    mOneRepeat.setSelected(!mOneRepeat.isSelected());
                    Intent one_repeat = new Intent(getContext(), MyMusicService.class);
                    one_repeat.setAction(MyMusicService.ACTION_ONE_REPEAT);
                    getContext().startService(one_repeat);
                }
                break;

            case R.id.now_repeat_button:
                if (!mOneRepeat.isSelected()) {
                    mRepeat.setSelected(!mRepeat.isSelected());
                    Intent reapeat = new Intent(getContext(), MyMusicService.class);
                    reapeat.setAction(MyMusicService.ACTION_REPEAT);
                    getContext().startService(reapeat);
                }
                break;

            case R.id.now_play_button:
                if (mMediaPlayer.isPlaying()) {
                    Intent pause = new Intent(getContext(), MyMusicService.class);
                    pause.setAction(MyMusicService.ACTION_PAUSE);
                    getContext().startService(pause);
                } else {
                    Intent re_start = new Intent(getContext(), MyMusicService.class);
                    re_start.setAction(MyMusicService.ACTION_RESTART);
                    getContext().startService(re_start);
                }
                break;

            case R.id.now_next_button:
                Intent next = new Intent(getContext(), MyMusicService.class);
                next.setAction(MyMusicService.ACTION_NEXT);
                getContext().startService(next);
                break;

            case R.id.now_pervious_button:
                Intent prev = new Intent(getContext(), MyMusicService.class);
                prev.setAction(MyMusicService.ACTION_PREV);
                getContext().startService(prev);
                break;
        }
    }

    @Override
    public void onNowCallback(MediaPlayer mediaPlayer, boolean play) {

        mMediaPlayer = mediaPlayer;

        mMyService.palyButtonChange(mPlay, play);

        playTime(play);

    }


    // 시간 표시 해주는 메소드
    private void playTime(final boolean play) {
        mMyService.uiChange(mNowImage, mTitleText, mArtistText);

        int duration = mMyService.getMediaPlayer().getDuration();
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        mMaxTime.setText((String.format("%d:%02d", min, sec)));
        mSeekbar.setMax(duration);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int CurrentPosition;
                if (play) {
                    CurrentPosition = mMyService.getMediaPlayer().getCurrentPosition();
                    int min = CurrentPosition / 1000 / 60;
                    int sec = CurrentPosition / 1000 % 60;
                    mSeekbar.setProgress(CurrentPosition);

                    mNowTime.setText(String.format("%d:%02d", min, sec));
                    mNowTime.postDelayed(this, 1000);
                } else {
                    mNowTime.removeCallbacks(this);
                }
            }
        }).run();
    }
}
