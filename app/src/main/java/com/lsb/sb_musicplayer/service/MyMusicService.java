package com.lsb.sb_musicplayer.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

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
    public static final String ACTION_FOREGRUOUND = "com.lsb.simple_player.action.foreground";

    private boolean mOneRepeat;
    private boolean mRepeat;

    public MediaPlayer mMediaPlayer;

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
        mCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.MediaColumns.MIME_TYPE + "='" + "audio/mpeg" + "'",
                null,
                null);
    }

    // 서비스 시작
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_PLAY:
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }

//                mUri = intent.getParcelableExtra("uri");
                mLength = intent.getIntExtra("length", -1);
                mNowPosition = intent.getIntExtra("now_position", -1);

                mCursor.moveToPosition(mNowPosition);
                try {
                    mMediaPlayer.reset();

                    mUri = Uri.parse(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                    play(mUri);

                } catch (IOException e) {
                    Toast.makeText(this, "I can't play a song", Toast.LENGTH_SHORT).show();
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
                stopSelf();
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

            case ACTION_FOREGRUOUND:
                startForeground(1, createNotification());
//                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                manager.notify(1, createNotification());
                break;
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

    private Notification createNotification() {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this, mUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();

        String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notify);
        contentView.setTextViewText(R.id.noti_title, title);
        contentView.setTextViewText(R.id.noti_artist, artist);
        if (picture != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
            contentView.setImageViewBitmap(R.id.noti_image, bitmap);
        } else {

        }
//        contentView.setImageViewBitmap(R.id.noti_image2,bitmap);


        Notification mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("SB_Player")
                .setContent(contentView)
                .build();


        return mBuilder;

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
        mCallback.onControllerCallback(mMediaPlayer, true);
        mCallback2.onNowCallback(mMediaPlayer, true);
    }

    // 음악 사진, 제목, 가수 이름
    public void uiChange(ImageView imageView, TextView titleView, TextView artistView) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(this, mUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();

        //수정 전 코드
//        String title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//        String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        mCursor.moveToPosition(mNowPosition);
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

