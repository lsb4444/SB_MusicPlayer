package com.lsb.sb_musicplayer.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lsb.sb_musicplayer.MainActivity;
import com.lsb.sb_musicplayer.R;
import com.lsb.sb_musicplayer.adapter.MyMusicCursorAdapter;
import com.lsb.sb_musicplayer.service.MyMusicService;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicListFragment extends Fragment {


    private ListView mListView;
    private MyMusicCursorAdapter mAdapter;

    public MusicListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_music_list, container, false);
        mListView = (ListView) v.findViewById(R.id.music_list_view);
        songList();

        return v;
    }


    // 노래 불러오고 uri 넘기기 서비스시작.
    private void songList() {
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);

        mAdapter = new MyMusicCursorAdapter(getActivity(), cursor);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor currentCursor = (Cursor) parent.getAdapter().getItem(position);

//                String uri = currentCursor.getString(currentCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                ((MainActivity) getActivity()).getViewPager().setCurrentItem(0);


                // https://developer.android.com/guide/topics/media/mediaplayer.html
                Intent intent = new Intent(getContext(), MyMusicService.class);
                intent.setAction(MyMusicService.ACTION_PLAY);
                intent.putExtra("length", mListView.getCount());
//                intent.putExtra("uri", Uri.parse(uri));
                intent.putExtra("now_position", currentCursor.getPosition());
                getContext().startService(intent);

            }
        });
    }
}
