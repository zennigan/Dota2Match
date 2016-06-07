package app.com.zennigan.android.dota2match;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import app.com.zennigan.android.dota2match.data.Dota2MatchContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener{

    private SimpleCursorAdapter mCursorAdapter;
    private ListView listview_match;

    private final int LOADER_ID = 80;

    private static final String[] FROM_IDS = {
            Dota2MatchContract.MatchEntry.COLUMN_ID,
            Dota2MatchContract.MatchEntry.COLUMN_RADIANT_WIN,
            Dota2MatchContract.MatchEntry.COLUMN_LOBBY_TYPE_NAME,
            Dota2MatchContract.MatchEntry.COLUMN_GAME_MODE_NAME,
            Dota2MatchContract.MatchEntry.COLUMN_START_TIME,
            Dota2MatchContract.MatchEntry.COLUMN_DURATION
    };

    private final static int[] TO_IDS = {
            R.id.list_item_match_id_textView,
            R.id.list_item_win_result_textView,
            R.id.list_lobby_textView,
            R.id.list_mode_textView,
            R.id.list_item_date_textView,
            R.id.list_item_duration_textView
    };
    private static final String[] PROJECTION = {
            Dota2MatchContract.MatchEntry.COLUMN_ID,
            Dota2MatchContract.MatchEntry.COLUMN_RADIANT_WIN,
            Dota2MatchContract.MatchEntry.COLUMN_LOBBY_TYPE_NAME,
            Dota2MatchContract.MatchEntry.COLUMN_GAME_MODE_NAME,
            Dota2MatchContract.MatchEntry.COLUMN_START_TIME,
            Dota2MatchContract.MatchEntry.COLUMN_DURATION
    };


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        listview_match =  (ListView) getActivity().findViewById(R.id.listview_match);
        mCursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_match,
                null,
                FROM_IDS, TO_IDS,
                0){
            @Override
            public void setViewText(TextView v, String text) {
                if(v.getId() == R.id.list_item_match_id_textView){
                    text =  getString(R.string.match_id_lbl, text);
                }
                else if(v.getId() == R.id.list_lobby_textView){
                    text =  Utility.getStringForLobbyType(getContext(), Integer.parseInt(text));
                }
                else if(v.getId() == R.id.list_mode_textView){
                    text =  Utility.getStringForGameMode(getContext(), Integer.parseInt(text));
                }
                else if(v.getId() == R.id.list_item_win_result_textView){
                    text =  "true".equals(text) ? getString(R.string.radiant_win): getString(R.string.dire_win);
                }
                else if(v.getId() == R.id.list_item_date_textView){
                    text = Utility.formatDate(Long.valueOf(text));
                }
                super.setViewText(v, text);
            }

        };
        listview_match.setAdapter(mCursorAdapter);
        listview_match.setOnItemClickListener(this);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                Dota2MatchContract.MatchEntry.CONTENT_URI,
                PROJECTION,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

        Intent detailIntent = new Intent(getActivity(), MatchDetailActivity.class);
        detailIntent.putExtra(MatchDetailFragment.KEY_MATCH_ID, String.valueOf(id));
        startActivity(detailIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getActivity().overridePendingTransition(android.R.anim.fade_in,
                    android.R.anim.slide_out_right);
        }
    }

}
