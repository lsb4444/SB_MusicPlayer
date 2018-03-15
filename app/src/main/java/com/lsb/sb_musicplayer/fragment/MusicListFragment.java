package com.lsb.sb_musicplayer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lsb.sb_musicplayer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicListFragment extends Fragment {


    private ListView mListView;

    public MusicListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_music_list, container, false);
        mListView = (ListView) v.findViewById(R.id.music_list_view);
        return v;

    }

}
