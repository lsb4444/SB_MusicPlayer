package com.lsb.sb_musicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lsb.sb_musicplayer.R;

import java.io.IOException;

public class MyMusicService extends Service {

    public static final String ACTION_PLAY = "com.lsb.simple_player.action.play";
    public static final String ACTION_PAUSE = "com.lsb.simple_player.action.pause";
    public static final String ACTION_PREV = "com.lsb.simple_player.action.prev";
    public static final String ACTION_NEXT = "com.lsb.simple_player.action.next";

    public MediaPlayer mMediaPlayer;

    private MyBinder mBinder = new MyBinder();

    MusicServiceCallback mCallback;
    MusicServiceCallback2 mCallback2;
    private Uri mUri;
    private int mLength;
    private int mNowPosition;
    private Cursor mCursor;


    public MyMusicService() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    // 서비스 시작
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        String action = intent.getAction();
        switch (action) {
            case ACTION_PLAY:
//                mUri = intent.getParcelableExtra("uri");
                mLength = intent.getIntExtra("length", -1);
                mNowPosition = intent.getIntExtra("now_position", -1);

                mCursor.moveToPosition(mNowPosition);
                try {
                    mMediaPlayer.reset();

                    mUri = Uri.parse(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    play(mUri);

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
                if (!mediaPlayer.isPlaying()) {
                    mCallback.onControllerCallback(mMediaPlayer, false);
                    mCallback2.onNowCallback(mMediaPlayer, false);
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


    // 재생 메소드
    public void play(Uri uri) throws IOException {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
        }
        mMediaPlayer.setDataSource(this, uri);
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }

        });
        mCallback.onControllerCallback(mMediaPlayer, true);
        mCallback2.onNowCallback(mMediaPlayer, true);

    }

    // 멈춤 메소드
    public void pause() {
        if (mMediaPlayer.isPlaying()) {
//            mMediaPlayer.stop();
//            mMediaPlayer.reset();
            mMediaPlayer.pause();
            mCallback.onControllerCallback(mMediaPlayer, false);
            mCallback2.onNowCallback(mMediaPlayer, false);
        }
    }

    // 리스타트 메소드
    public void reStart() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            mCallback.onControllerCallback(mMediaPlayer, true);
            mCallback2.onNowCallback(mMediaPlayer, true);
        }
    }

    // 다음곡 메소드
    public void next() {
        mMediaPlayer.reset();
        if (mMediaPlayer != null) {
            if (mNowPosition < mLength) {
                mNowPosition++;
                if (mNowPosition == mLength) {
                    mNowPosition = 0;
                }
            }
            mCursor.moveToPosition(mNowPosition);
            mUri = Uri.parse(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            try {
                play(mUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void peve() {
        mMediaPlayer.reset();
        if (mNowPosition != 0) {
            mNowPosition--;
            mCursor.moveToPrevious();
        } else {
            mNowPosition = mLength - 1;
            mCursor.moveToLast();
        }

        mUri = Uri.parse(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
        try {
            play(mUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 음악 사진, 제목, 가수 이름
    public void uiChange(ImageView imageView, TextView titleView, TextView artistView) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this, mUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();

        String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        titleView.setText(title);
        artistView.setText(artist);

        if (picture != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    public interface MusicServiceCallback {
        void onControllerCallback(MediaPlayer mediaPlayer, boolean play);
    }

    public interface MusicServiceCallback2 {
        void onNowCallback(MediaPlayer mediaPlayer, boolean play);
    }

    public void setControllerCallback(MusicServiceCallback callback) {
        mCallback = callback;
    }

    public void setNowCallback(MusicServiceCallback2 callback) {
        mCallback2 = callback;
    }
}

