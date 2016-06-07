package app.com.zennigan.android.dota2match.sync;

/**
 * Created by Zennigan on 6/4/2016.
 */
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import app.com.zennigan.android.dota2match.R;
import app.com.zennigan.android.dota2match.Utility;
import app.com.zennigan.android.dota2match.data.Dota2MatchContract;

public class Dota2MatchSyncAdapter extends AbstractThreadedSyncAdapter{

    public final String LOG_TAG = Dota2MatchSyncAdapter.class.getSimpleName();

    public static final String ACTION_DATA_UPDATED =
            "app.com.zennigan.android.dota2match.app.ACTION_DATA_UPDATED";

    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 1440 = 24 hours
    public static final int SYNC_INTERVAL = 60 * 1440;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    final String API_BASE_URL = "http://api.steampowered.com/";
    final String API_PATH_GETMATCHHISTORY = "IDOTA2Match_570/GetMatchHistory/V001/";
    final String API_PATH_GETMATCHDETAILS = "IDOTA2Match_570/GetMatchDetails/V001/";
    final String API_PATH_GETHEROES = "IEconDOTA2_570/GetHeroes/v0001/";
    final String API_PATH_GETPLAYERSUMMARIES = "ISteamUser/GetPlayerSummaries/v0002/";
    final String API_HERO_IMAGE_URL = "http://cdn.dota2.com/apps/dota2/images/heroes/";
    final String API_HERO_PREFIX = "npc_dota_hero_";
    final String API_HERO_SUFFIX = "_full.png";

	final String API_KEY = "key";
    final String API_MATCHES_REQ = "matches_requested";
    final int NO_OF_MATCHES =10;
	final String API_LANG = "language";
    final String API_RESULTS__KEY = "result";
    final String API_RESPONSE__KEY = "response";
    final String API_MATCHES_KEY = "matches";
    final String API_MATCH_ID_KEY = "match_id";
    final String API_RADIANT_WIN_KEY = "radiant_win";
    final String API_START_TIME_KEY = "start_time";
    final String API_DURATION_KEY = "duration";
    final String API_LOBBY_TYPE_KEY = "lobby_type";
    final String API_GAME_MODE_KEY = "game_mode";
    final String API_PLAYERS_KEY = "players";
    final String API_ACCOUNT_ID_KEY = "account_id";
    final String API_HERO_ID_KEY = "hero_id";
    final String API_KILLS_KEY = "kills";
    final String API_DEATHS_KEY = "deaths";
    final String API_ASSISTS_KEY = "assists";
    final String API_LEVEL_KEY = "level";
    final String API_HEROES_KEY = "heroes";
    final String API_ID_KEY = "id";
    final String API_HERO_LOCALIZED_NAME_KEY = "localized_name";
    final String API_HERO_NAME_KEY = "name";
    final String API_PLAYER_NAME_KEY = "personaname";
    final String API_STEAMID_KEY = "steamid";

	private String api_key_val;

    public Dota2MatchSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
		api_key_val = getContext().getString(R.string.api_key);

        long now = System.currentTimeMillis() - DAY_IN_MILLIS;
        getContext().getContentResolver().delete(Dota2MatchContract.MatchEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(Dota2MatchContract.MatchPlayerEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().delete(Dota2MatchContract.HeroEntry.CONTENT_URI, null, null);

        syncHerosTable();

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon().appendEncodedPath(API_PATH_GETMATCHHISTORY)
                    .appendQueryParameter(API_KEY, api_key_val)
                    .build();

            Log.v(LOG_TAG, builtUri.toString());
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                //setDota2APIStatus(getContext(), STATUS_SERVER_DOWN);
              return;
            }

            JSONObject responseJson = new JSONObject(buffer.toString()).getJSONObject(API_RESULTS__KEY);
            JSONArray matchArray = responseJson.getJSONArray(API_MATCHES_KEY);
            int count = 0;
            for(int i = 0; i < matchArray.length() && count<NO_OF_MATCHES; i++) {
                count = count+ getMatchDetails(matchArray.getJSONObject(i).getString(API_MATCH_ID_KEY), now);
            }
            Log.v(LOG_TAG, "count="+count);
            updateWidgets();
            retrievePlayerNames();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
           //setDota2APIStatus(getContext(), STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            //setDota2APIStatus(getContext(), STATUS_SERVER_INVALID);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    private int getMatchDetails(String match_id, long now)
    {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon().appendEncodedPath(API_PATH_GETMATCHDETAILS)
                    .appendQueryParameter(API_KEY, api_key_val)
                    .appendQueryParameter(API_MATCH_ID_KEY, match_id)
                    .build();

            Log.v(LOG_TAG, builtUri.toString());
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return 0;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                //setDota2APIStatus(getContext(), STATUS_SERVER_DOWN);
                return 0;
            }

            JSONObject matchObj = new JSONObject(buffer.toString()).getJSONObject(API_RESULTS__KEY);
            JSONArray matchPlayerArrayObj = matchObj.getJSONArray(API_PLAYERS_KEY);

            if(Utility.convertUnixTime(matchObj.getLong(API_START_TIME_KEY))< now || matchObj.getString(API_DURATION_KEY).equals("0")){
                return 0;
            }
            String lobby_type_name = matchObj.getString(API_LOBBY_TYPE_KEY); //TO DO: need LOBBY_TYPE_NAME
            String game_mode_name = matchObj.getString(API_GAME_MODE_KEY);//TO DO: need LOBBY_TYPE_NAME

            ContentValues matchValues = new ContentValues();
            matchValues.put(Dota2MatchContract.MatchEntry.COLUMN_ID, match_id);
            matchValues.put(Dota2MatchContract.MatchEntry.COLUMN_RADIANT_WIN, matchObj.getString(API_RADIANT_WIN_KEY));
            matchValues.put(Dota2MatchContract.MatchEntry.COLUMN_START_TIME, Utility.convertUnixTime(matchObj.getLong(API_START_TIME_KEY)));
            matchValues.put(Dota2MatchContract.MatchEntry.COLUMN_LOBBY_TYPE_NAME, lobby_type_name);
            matchValues.put(Dota2MatchContract.MatchEntry.COLUMN_GAME_MODE_NAME, game_mode_name);
            matchValues.put(Dota2MatchContract.MatchEntry.COLUMN_DURATION, Utility.formatDuration(matchObj.getInt(API_DURATION_KEY)));

            //insert match into db
            getContext().getContentResolver().insert(Dota2MatchContract.MatchEntry.CONTENT_URI, matchValues);

            Vector<ContentValues> matchPlayerVector = new Vector<ContentValues>(matchPlayerArrayObj.length());

            //iterate through players in match
            for(int i = 0; i < matchPlayerArrayObj.length(); i++) {
                JSONObject matchPlayerObject = matchPlayerArrayObj.getJSONObject(i);

                ContentValues matchPlayerValues = new ContentValues();
                matchPlayerValues.put(Dota2MatchContract.MatchPlayerEntry.COLUMN_ACCOUNT_ID, convert32bitTo64bit(matchPlayerObject.getLong(API_ACCOUNT_ID_KEY)));
                matchPlayerValues.put(Dota2MatchContract.MatchPlayerEntry.COLUMN_MATCH_ID, match_id);
                matchPlayerValues.put(Dota2MatchContract.MatchPlayerEntry.COLUMN_HERO_ID, matchPlayerObject.getString(API_HERO_ID_KEY));
                matchPlayerValues.put(Dota2MatchContract.MatchPlayerEntry.COLUMN_LEVEL, matchPlayerObject.getString(API_LEVEL_KEY));
                matchPlayerValues.put(Dota2MatchContract.MatchPlayerEntry.COLUMN_KILLS, matchPlayerObject.getString(API_KILLS_KEY));
                matchPlayerValues.put(Dota2MatchContract.MatchPlayerEntry.COLUMN_DEATHS, matchPlayerObject.getString(API_DEATHS_KEY));
                matchPlayerValues.put(Dota2MatchContract.MatchPlayerEntry.COLUMN_ASSISTS, matchPlayerObject.getString(API_ASSISTS_KEY));
                matchPlayerVector.add(matchPlayerValues);
            }

            //insert match player into db
            ContentValues[] matchPlayerArray = new ContentValues[matchPlayerVector.size()];
            matchPlayerVector.toArray(matchPlayerArray);
            int count =getContext().getContentResolver().bulkInsert(Dota2MatchContract.MatchPlayerEntry.CONTENT_URI, matchPlayerArray);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            //setDota2APIStatus(getContext(), STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            //setDota2APIStatus(getContext(), STATUS_SERVER_INVALID);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return 1;
    }

    private long convert32bitTo64bit(long x){
        return x + Long.valueOf("76561197960265728");

    }

    private void retrievePlayerNames(){
        String steamids = "";

        Cursor c = getContext().getContentResolver().query(Dota2MatchContract.MatchPlayerEntry.CONTENT_URI,
                new String[]{" distinct "+Dota2MatchContract.MatchPlayerEntry.COLUMN_ACCOUNT_ID }, null, null, null);

        while(c.moveToNext()){
            if(c.getString(0) != null)
                steamids = steamids + c.getString(0) + "," ;
        }
        if(!steamids.isEmpty())
            steamids = steamids.substring(0, steamids.lastIndexOf(",")-1);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon().appendEncodedPath(API_PATH_GETPLAYERSUMMARIES)
                    .appendQueryParameter(API_KEY, api_key_val)
                    .appendQueryParameter("steamids", steamids)
                    .build();

            Log.v(LOG_TAG, builtUri.toString());

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
               // setDota2APIStatus(getContext(), STATUS_SERVER_DOWN);
                return;
            }

            JSONObject responseJson = new JSONObject(buffer.toString()).getJSONObject(API_RESPONSE__KEY);
            JSONArray playerArray = responseJson.getJSONArray(API_PLAYERS_KEY);

            for(int i = 0; i < playerArray.length(); i++) {
                JSONObject pjo = playerArray.getJSONObject(i);
                ContentValues playerValues = new ContentValues();
                playerValues.put(Dota2MatchContract.MatchPlayerEntry.COLUMN_ACCOUNT_NAME, pjo.getString(API_PLAYER_NAME_KEY));

                //update match_players
                getContext().getContentResolver().update(Dota2MatchContract.MatchPlayerEntry.CONTENT_URI, playerValues, Dota2MatchContract.MatchPlayerEntry.COLUMN_ACCOUNT_ID + " = ?", new String[]{pjo.getString(API_STEAMID_KEY)});
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            //setDota2APIStatus(getContext(), STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            //setDota2APIStatus(getContext(), STATUS_SERVER_INVALID);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

    }

    private void syncHerosTable(){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon().appendEncodedPath(API_PATH_GETHEROES)
                    .appendQueryParameter(API_KEY, api_key_val)
                    .appendQueryParameter(API_LANG, getContext().getString(R.string.api_lang))
                    .build();

            Log.v(LOG_TAG, builtUri.toString());

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                //setDota2APIStatus(getContext(), STATUS_SERVER_DOWN);
                return;
            }

            JSONObject responseJson = new JSONObject(buffer.toString()).getJSONObject(API_RESULTS__KEY);
            JSONArray heroArray = responseJson.getJSONArray(API_HEROES_KEY);

            Vector<ContentValues> heroesVector = new Vector<ContentValues>(heroArray.length());

            for(int i = 0; i < heroArray.length(); i++) {
                JSONObject hjo = heroArray.getJSONObject(i);

                ContentValues heroValues = new ContentValues();
                heroValues.put(Dota2MatchContract.HeroEntry.COLUMN_ID, hjo.getString(API_ID_KEY));
                heroValues.put(Dota2MatchContract.HeroEntry.COLUMN_NAME, hjo.getString(API_HERO_LOCALIZED_NAME_KEY));
                heroValues.put(Dota2MatchContract.HeroEntry.COLUMN_IMAGE_URL, API_HERO_IMAGE_URL + hjo.getString(API_HERO_NAME_KEY).replace(API_HERO_PREFIX,"")+ API_HERO_SUFFIX);
                heroesVector.add(heroValues);
                Log.v(LOG_TAG, heroValues.getAsString(Dota2MatchContract.HeroEntry.COLUMN_IMAGE_URL));
            }
            //insert heroes into db
            ContentValues[] heroesArray = new ContentValues[heroesVector.size()];
            heroesVector.toArray(heroesArray);
            getContext().getContentResolver().bulkInsert(Dota2MatchContract.HeroEntry.CONTENT_URI, heroesArray);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            //setDota2APIStatus(getContext(), STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            //setDota2APIStatus(getContext(), STATUS_SERVER_INVALID);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }


    private void updateWidgets() {
        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
      
        /*
         * Since we've created an account
         */
        Dota2MatchSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
       // syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

}
