package com.lsb.sb_musicplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lsb.sb_musicplayer.R;

/**
 * Created by LLLocal on 2018-03-17.
 */

public class MyMusicCursorAdapter extends CursorAdapter {

    public static final String TAG = MyMusicCursorAdapter.class.getSimpleName();

    public MyMusicCursorAdapter(Context context, Cursor c) {
        super(context, c, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        View v = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);

        viewHolder.mImageView = v.findViewById(R.id.image_view);
        viewHolder.mTitleView = v.findViewById(R.id.title_text_view);
        viewHolder.mArtistView = v.findViewById(R.id.artist_text_view);

        v.setTag(viewHolder);

        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

        viewHolder.mTitleView.setText(title);
        viewHolder.mArtistView.setText(artist);

        final Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(context, uri);
            byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();

            if (picture != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                viewHolder.mImageView.setImageBitmap(bitmap);
            } else {
                viewHolder.mImageView.setImageResource(R.mipmap.ic_launcher);
            }
        } catch (Exception e) {
            Log.d(TAG, "bindView: " + cursor.getPosition());
        }
    }

    static class ViewHolder {
        ImageView mImageView;
        TextView mTitleView;
        TextView mArtistView;
    }
}
