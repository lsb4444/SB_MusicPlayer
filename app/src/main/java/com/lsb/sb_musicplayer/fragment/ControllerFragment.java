package com.lsb.sb_musicplayer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


import com.lsb.sb_musicplayer.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ControllerFragment extends Fragment {


    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;

    public ControllerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_controller, container, false);
        mPlayButton = (ImageButton) v.findViewById(R.id.play_button);
        mNextButton = (ImageButton) v.findViewById(R.id.next_button);
        mPrevButton = (ImageButton) v.findViewById(R.id.prev_button);
        return v;
    }

}
