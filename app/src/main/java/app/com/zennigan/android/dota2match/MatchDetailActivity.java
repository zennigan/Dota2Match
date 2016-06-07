package app.com.zennigan.android.dota2match;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.support.design.widget.FloatingActionButton;
import android.graphics.Bitmap;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class MatchDetailActivity extends AppCompatActivity {

    private static final String MATCHDETAILFRAGMENT_TAG = "MDFTAG";

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);

        if (savedInstanceState == null) {

            MatchDetailFragment frag = new MatchDetailFragment();
            Bundle args = new Bundle();
            if(getIntent() == null){
                args.putString(MatchDetailFragment.KEY_MATCH_ID, null);
            }else {
                args.putString(MatchDetailFragment.KEY_MATCH_ID, getIntent().getStringExtra(MatchDetailFragment.KEY_MATCH_ID));
            }
            frag.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.match_detail_container, frag, MATCHDETAILFRAGMENT_TAG)
                    .commit();
        }
		

    }

}