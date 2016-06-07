package app.com.zennigan.android.dota2match.data;

/**
 * Created by Zennigan on 6/4/2016.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import app.com.zennigan.android.dota2match.data.Dota2MatchContract.MatchEntry;

public class Dota2MatchDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "dota2match.db";

    public Dota2MatchDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_HEROES_TABLE = "CREATE TABLE " + Dota2MatchContract.HeroEntry.TABLE_NAME + " (" +
                Dota2MatchContract.HeroEntry._ID + " TEXT PRIMARY KEY," +
                Dota2MatchContract.HeroEntry.COLUMN_NAME + " TEXT  NULL, " +
                Dota2MatchContract.HeroEntry.COLUMN_IMAGE_URL + " TEXT  NULL " +
        " );";

        sqLiteDatabase.execSQL(SQL_CREATE_HEROES_TABLE);


        final String SQL_CREATE_MATCHES_TABLE = "CREATE TABLE " + MatchEntry.TABLE_NAME + " (" +
                MatchEntry._ID + " TEXT PRIMARY KEY," +
                MatchEntry.COLUMN_RADIANT_WIN + " TEXT  NULL, " +
                MatchEntry.COLUMN_START_TIME + " TEXT  NULL, " +
                MatchEntry.COLUMN_DURATION + " TEXT  NULL, " +
                MatchEntry.COLUMN_LOBBY_TYPE_NAME + " TEXT  NULL," +
                MatchEntry.COLUMN_GAME_MODE_NAME + " TEXT  NULL" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_MATCHES_TABLE);

        final String SQL_CREATE_MATCH_PLAYERS_TABLE = "CREATE TABLE " + Dota2MatchContract.MatchPlayerEntry.TABLE_NAME + " (" +
                Dota2MatchContract.MatchPlayerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Dota2MatchContract.MatchPlayerEntry.COLUMN_MATCH_ID + " TEXT  NULL, " +
                Dota2MatchContract.MatchPlayerEntry.COLUMN_ACCOUNT_ID + " TEXT  NULL, " +
                Dota2MatchContract.MatchPlayerEntry.COLUMN_ACCOUNT_NAME + " TEXT NULL, " +
                Dota2MatchContract.MatchPlayerEntry.COLUMN_HERO_ID + " TEXT  NULL, " +
                Dota2MatchContract.MatchPlayerEntry.COLUMN_LEVEL + " TEXT  NULL, " +
                Dota2MatchContract.MatchPlayerEntry.COLUMN_KILLS + " TEXT  NULL, " +
                Dota2MatchContract.MatchPlayerEntry.COLUMN_DEATHS + " TEXT  NULL, " +
                Dota2MatchContract.MatchPlayerEntry.COLUMN_ASSISTS + " TEXT  NULL " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_MATCH_PLAYERS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MatchEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Dota2MatchContract.MatchPlayerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Dota2MatchContract.HeroEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
