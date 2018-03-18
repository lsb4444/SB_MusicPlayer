package com.lsb.sb_musicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

public class MyMusicService extends Service {

    public static final String ACTION_PLAY = "com.lsb.simple_player.action.play";
    public static final String ACTION_PAUSE = "com.lsb.simple_player.action.pause";
    public static final String ACTION_PREV = "com.lsb.simple_player.action.prev";
    public static final String ACTION_NEXT = "com.lsb.simple_player.action.next";

    public MediaPlayer mMediaPlayer;

    private MyBinder mBinder = new MyBinder();

    MusicServiceCallback mCallback;


    public MyMusicService() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_PLAY:
                Uri uri = intent.getParcelableExtra("uri");
                try {
                    play(uri);
                    mCallback.onCallback(mMediaPlayer, true);
                } catch (IOException e) {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            case ACTION_PAUSE:
                pause();
                break;

            case ACTION_PREV:
                break;

            case ACTION_NEXT:
                break;
        }

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (!mediaPlayer.isPlaying()){
                    mCallback.onCallback(mMediaPlayer, false);
                    mMediaPlayer.reset();
                }
            }
        });
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public MyMusicService getService() {
            return MyMusicService.this;
        }
    }


    public void play(Uri uri) throws IOException {
        if (mMediaPlayer.isPlaying()) {
            pause();
        }
        mMediaPlayer.setDataSource(this, uri);
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }

        });

    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

    public interface MusicServiceCallback {
        void onCallback(MediaPlayer mediaPlayer, boolean play);
    }

    public void setCallback(MusicServiceCallback callback) {
        mCallback = callback;
    }
}

