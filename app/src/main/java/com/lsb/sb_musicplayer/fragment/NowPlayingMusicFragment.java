package com.lsb.sb_musicplayer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lsb.sb_musicplayer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NowPlayingMusicFragment extends Fragment {


    public NowPlayingMusicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_now_playing_music, container, false);
    }

}
