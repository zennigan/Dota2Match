package app.com.zennigan.android.dota2match;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.com.zennigan.android.dota2match.data.Dota2MatchContract;


public class MatchDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>{

    public static final String KEY_MATCH_ID = "match_id";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private final int LOADER_ID = 20;

    private static final String[] FROM_IDS = {	
            Dota2MatchContract.HeroEntry.COLUMN_NAME,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_ACCOUNT_NAME,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_LEVEL,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_KILLS,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_DEATHS,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_ASSISTS,
            Dota2MatchContract.HeroEntry.COLUMN_IMAGE_URL	
    };

    private final static int[] TO_IDS = {
			R.id.hero_name,
            R.id.account_name,
            R.id.level,
            R.id.kills,
            R.id.deaths,
            R.id.assists,
			R.id.hero_ic
    };

    private ListView mMatchPlayerList;
    private SimpleCursorAdapter mCursorAdapter;
    private String match_id;

    private int selected;
    private static final int SELECTED_ID_INDEX = 0;

    private static final String[] PROJECTION = {
            Dota2MatchContract.MatchEntry.TABLE_NAME+
                    "." + Dota2MatchContract.MatchEntry.COLUMN_ID,
            Dota2MatchContract.HeroEntry.COLUMN_NAME,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_ACCOUNT_NAME,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_LEVEL,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_KILLS,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_DEATHS,
            Dota2MatchContract.MatchPlayerEntry.COLUMN_ASSISTS,		
            Dota2MatchContract.HeroEntry.COLUMN_IMAGE_URL
    };

    public MatchDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            match_id = arguments.getString(KEY_MATCH_ID);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_match_detail, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setTitle(getString(R.string.match_id_lbl, match_id));

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verifyStoragePermissions()) {
                    shareActivity(mMatchPlayerList);
                }
            }
        });

        return rootView;
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMatchPlayerList =  (ListView) getActivity().findViewById(R.id.match_player_list);
        mCursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.match_player_list_item,
                null,
                FROM_IDS, TO_IDS,
                0){
					   @Override
						public void setViewImage(ImageView v, String value) {
							//super.setViewImage(v, value);
							if(value!= null && !value.isEmpty())
								Picasso.with(getActivity()).load(value).into(v);
							
							RelativeLayout parent =  ((RelativeLayout) v.getParent());
							TextView v_hero_name = (TextView)parent.findViewById(R.id.hero_name);
							if(v_hero_name != null){
								v.setContentDescription(v_hero_name.getText().toString());
							}
						}
						
						@Override
						public void setViewText(TextView v, String text) {
                            if(v.getId() == R.id.kills) {
								v.setContentDescription(text + getString(R.string.kills_lbl));
							}
							else if(v.getId() == R.id.deaths) {
								v.setContentDescription(text + getString(R.string.deaths_lbl));
							}
							else if(v.getId() == R.id.assists) {
								v.setContentDescription(text + getString(R.string.assists_lbl));
							}
							else if(v.getId() == R.id.account_name) {
								if(text == null || text.isEmpty()){
									text = getString(R.string.anonymous);
								}
							}
							super.setViewText(v, text);
						}
				};
        mMatchPlayerList.setAdapter(mCursorAdapter);
        //mMatchPlayerList.setEnabled(false);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                getActivity(),
                Dota2MatchContract.MatchPlayerEntry.buildWithMatchIDUri(Long.valueOf(match_id)),
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    shareActivity(mMatchPlayerList);
                } else {
                    // Permission Denied
                    Toast.makeText(getActivity(), getString(R.string.permission_external_storage_denied), Toast.LENGTH_SHORT)
                            .show();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public boolean verifyStoragePermissions() {
        int permission = ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return false;
        }
        return true;
    }

    private void shareActivity(View view){
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);

        String dirPath = Environment.getExternalStorageDirectory() + "/"+ Environment.DIRECTORY_DCIM + "/Screenshots";

        File dir = new File(dirPath);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd-hhmmss");
        String timestamp = s.format(new Date());

        File file = new File(dirPath, getString(R.string.screenshot_filename,timestamp));
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        Uri uri = Uri.fromFile(file);
        Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                .setType("image/*")
                .getIntent()
                .putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
    }
}