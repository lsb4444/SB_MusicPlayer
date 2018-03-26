package com.lsb.sb_musicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import android.support.v4.media.app.NotificationCompat;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lsb.sb_musicplayer.MainActivity;
import com.lsb.sb_musicplayer.R;

import java.io.IOException;


public class MyMusicService extends Service {

    public static final String ACTION_PLAY = "com.lsb.simple_player.action.play";
    public static final String ACTION_PAUSE = "com.lsb.simple_player.action.pause";
    public static final String ACTION_PREV = "com.lsb.simple_player.action.prev";
    public static final String ACTION_NEXT = "com.lsb.simple_player.action.next";

    public static final String ACTION_RESTART = "com.lsb.simple_player.action.restart";

    public static final String ACTION_ONE_REPEAT = "com.lsb.simple_player.action.onerepeat";
    public static final String ACTION_REPEAT = "com.lsb.simple_player.action.repeat";

    public static final String ACTION_NOTI_PLAY = "com.lsb.simple_player.action.notiplay";
    public static final String ACTION_CLOSE = "com.lsb.simple_player.action.close";

    private boolean mOneRepeat;
    private boolean mRepeat;

    public MediaPlayer mMediaPlayer;

    public boolean mMediaPlayerCheck;

    private MyBinder mBinder = new MyBinder();

    MusicServiceCallback mCallback;
    MusicServiceCallback2 mCallback2;
    private Uri mUri;
    private int mLength;
    private int mNowPosition = -1;
    private Cursor mCursor;


    public MyMusicService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();

    }

    // 서비스 시작
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        mCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.MediaColumns.MIME_TYPE + "='" + "audio/mpeg" + "'",
                null,
                null);

        switch (action) {
            case ACTION_PLAY:
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }

//                mUri = intent.getParcelableExtra("uri");
                mLength = intent.getIntExtra("length", -1);
                mNowPosition = intent.getIntExtra("now_position", -1);

                if (mNowPosition != -1) {
                    mCursor.moveToPosition(mNowPosition);
                }

                try {
                    mMediaPlayer.reset();

                    mUri = Uri.parse(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    play(mUri);

                } catch (IOException e) {
                    Toast.makeText(this, "I can't playing a song", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;

            case ACTION_RESTART:
                reStart();
                break;
            case ACTION_PAUSE:
                pause();
                break;

            case ACTION_PREV:
                peve();
                break;

            case ACTION_NEXT:
                next();
                break;


            case ACTION_ONE_REPEAT:
                if (!mOneRepeat) {
                    mOneRepeat = true;
                } else {
                    mOneRepeat = false;
                }
                break;
            case ACTION_REPEAT:
                if (!mRepeat) {
                    mRepeat = true;
                } else {
                    mRepeat = false;
                }
                break;

            case ACTION_NOTI_PLAY:

                if (mMediaPlayer.isPlaying()) {
                    pause();
                } else {
                    reStart();
                }
                break;
            case ACTION_CLOSE:
                mMediaPlayerCheck = false;
                mMediaPlayer = null;
//                if (mMediaPlayer.isPlaying()) {
//                    mMediaPlayer.stop();
//                    mMediaPlayer.release();
//                    mMediaPlayer = null;
//                } else {
//                    mMediaPlayer.release();
//                    mMediaPlayer = null;
//                }

                stopForeground(true);
                stopService(new Intent(this, MyMusicService.class));
                System.exit(0);
        }

        // 재생 완료 후 버튼 처리 및 이벤트 처리
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (!mediaPlayer.isPlaying()) {
                        if (!mOneRepeat && !mRepeat) {
                            mCallback.onControllerCallback(mMediaPlayer, false);
                            mCallback2.onNowCallback(mMediaPlayer, false);
                            pause();
                        } else if (mRepeat) {
                            next();
                        } else if (mOneRepeat) {
                            one_repeat();
                        }
                    }
                }
            });
        }

        return START_NOT_STICKY;
    }

    private void createNotification() {

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this, mUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();

        String title = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String artist = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        Bitmap bitmap = null;
        if (picture != null) {
            bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }


        String channerl_id = "music_channer1";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channelMessage = new NotificationChannel(channerl_id, "music_controller", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channelMessage);

        }

        android.support.v7.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(this);
        builder.setContentTitle(title);
        builder.setContentText(artist);
        builder.setStyle(new NotificationCompat.MediaStyle());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(bitmap);


        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channerl_id);
        }

            // 뒤로가기
        Intent intent_prev = new Intent(this, MyMusicService.class);
        intent_prev.setAction(ACTION_PREV);

        PendingIntent pendingPrevIntent = PendingIntent.getService(this, 1002, intent_prev, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.ic_skip_previous_black_24dp,
                "prev", pendingPrevIntent);


        // 플레이
        Intent intent_play = new Intent(this, MyMusicService.class);
        intent_play.setAction(ACTION_NOTI_PLAY);

        PendingIntent pendingPlayIntent = PendingIntent.getService(this, 1001, intent_play, PendingIntent.FLAG_UPDATE_CURRENT);
        if (mMediaPlayerCheck) {
            builder.addAction(R.drawable.ic_pause_circle_filled_black_24dp, "play", pendingPlayIntent);
        } else {
            builder.addAction(R.drawable.ic_play_circle_filled_black_24dp, "play", pendingPlayIntent);
        }

        // 넥스트
        Intent intent_next = new Intent(this, MyMusicService.class);
        intent_next.setAction(ACTION_NEXT);

        PendingIntent pendingNextIntent = PendingIntent.getService(this, 1003, intent_next, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.addAction(R.drawable.ic_skip_next_black_24dp,
                "next", pendingNextIntent);

        Intent intent_close = new Intent(this, MyMusicService.class);
        intent_close.setAction(ACTION_CLOSE);

        PendingIntent pendingCloseIntent = PendingIntent.getService(this, 1003, intent_close, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.addAction(R.drawable.ic_close_black_24dp,
                "close", pendingCloseIntent);

        startForeground(1, builder.build());
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
        mMediaPlayerCheck = true;
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
        createNotification();
    }

    // 멈춤 메소드
    public void pause() {
        if (mMediaPlayer.isPlaying()) {
//            mMediaPlayer.stop();
//            mMediaPlayer.reset();
            mMediaPlayerCheck = false;
            mMediaPlayer.pause();
            mCallback.onControllerCallback(mMediaPlayer, false);
            mCallback2.onNowCallback(mMediaPlayer, false);
        }
        createNotification();
    }

    // 리스타트 메소드
    public void reStart() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            mMediaPlayerCheck = true;
            mCallback.onControllerCallback(mMediaPlayer, true);
            mCallback2.onNowCallback(mMediaPlayer, true);
        }
        createNotification();
    }

    // 다음곡 메소드
    public void next() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
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

    // 이전곡 메소드
    public void peve() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            if (mNowPosition != 0) {
                mNowPosition--;
            } else {
                mNowPosition = mLength - 1;
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

    // 한곡 반복
    public void one_repeat() {
        mMediaPlayer.reset();

        mCursor.moveToPosition(mNowPosition);
        mUri = Uri.parse(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
        try {
            play(mUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 미디어 플레이어 넘기기
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    // 재생성 업데이트
    public void uiUpdata() {
        if (mMediaPlayer.isPlaying()) {
            mCallback.onControllerCallback(mMediaPlayer, true);
            mCallback2.onNowCallback(mMediaPlayer, true);

        } else {
            mCallback.onControllerCallback(mMediaPlayer, false);
            mCallback2.onNowCallback(mMediaPlayer, false);

        }
    }

    // 음악 사진, 제목, 가수 이름
    public void uiChange(ImageView imageView, TextView titleView, TextView artistView) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this, mUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();

        //수정 전 코드
//        String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//        String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        if (mNowPosition != -1) {
            mCursor.moveToPosition(mNowPosition);
        }
        String title = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String artist = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

        titleView.setText(title);
        artistView.setText(artist);

        if (picture != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }


    // play버튼 체인지 메소드
    public void palyButtonChange(ImageButton imageButton, boolean play) {
        if (play) {
            Drawable drawable = ActivityCompat.getDrawable(this, R.drawable.ic_pause_circle_filled_black_24dp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                VectorDrawable vectorDrawable = (VectorDrawable) drawable;
                imageButton.setImageDrawable(vectorDrawable);
            } else {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                imageButton.setImageDrawable(bitmapDrawable);
            }
        } else {
            Drawable drawable = ActivityCompat.getDrawable(this, R.drawable.ic_play_circle_filled_black_24dp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                VectorDrawable vectorDrawable = (VectorDrawable) drawable;
                imageButton.setImageDrawable(vectorDrawable);
            } else {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                imageButton.setImageDrawable(bitmapDrawable);
            }
        }
    }


    // 시간 메소드
    public void playTime(final SeekBar seekBar, TextView maxText, final TextView nowText) {

        int duration = mMediaPlayer.getDuration();
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        maxText.setText((String.format("%d:%02d", min, sec)));
        seekBar.setMax(duration);

        new Thread(new Runnable() {
            @Override
            public void run() {
                int CurrentPosition;
                if (mMediaPlayerCheck) {
                    CurrentPosition = mMediaPlayer.getCurrentPosition();
                    int min = CurrentPosition / 1000 / 60;
                    int sec = CurrentPosition / 1000 % 60;
                    seekBar.setProgress(CurrentPosition);

                    nowText.setText(String.format("%d:%02d", min, sec));
                    nowText.postDelayed(this, 100);
                } else {
                    nowText.removeCallbacks(this);
                }
            }
        }).run();
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

