package com.lsb.sb_musicplayer.fragment;

import android.Manifest;
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
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lsb.sb_musicplayer.MainActivity;
import com.lsb.sb_musicplayer.R;
import com.lsb.sb_musicplayer.adapter.MyMusicCursorAdapter;
import com.lsb.sb_musicplayer.service.MyMusicService;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicListFragment extends Fragment {


    private ListView mListView;
    private MyMusicCursorAdapter mAdapter;
    private Cursor mCurrentCursor;

    public MusicListFragment() {
        // Required empty public constructor
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            songList();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {

        }


    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_music_list, container, false);
        mListView = (ListView) v.findViewById(R.id.music_list_view);

        TedPermission.with(getContext())
                .setRationaleMessage("[필수권한] 이 기능은 외부 저장소에 접근 권한이 필요합니다.")
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("설정 메뉴에서 언제든지 권한을 변경 할 수 있습니다.\n\n [설정] - [권한] 으로 이동하셔서 권한을 허용하신후 이용하시기 바랍니다.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

        return v;
    }


    // 노래 불러오고 uri 넘기기 서비스시작.
    private void songList() {
        mCurrentCursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.MediaColumns.MIME_TYPE + "='" + "audio/mpeg" + "'",
                null,
                null);

        mAdapter = new MyMusicCursorAdapter(getActivity(), mCurrentCursor);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentCursor = (Cursor) parent.getAdapter().getItem(position);


                // https://developer.android.com/guide/topics/media/mediaplayer.html

//                String uri = currentCursor.getString(currentCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                ((MainActivity) getActivity()).getViewPager().setCurrentItem(0);
                Intent intent = new Intent(getContext(), MyMusicService.class);
                intent.setAction(MyMusicService.ACTION_PLAY);
                intent.putExtra("length", mListView.getCount());
//                intent.putExtra("uri", Uri.parse(uri));
                intent.putExtra("now_position", mCurrentCursor.getPosition());
                getContext().startService(intent);

            }
        });
    }
}
