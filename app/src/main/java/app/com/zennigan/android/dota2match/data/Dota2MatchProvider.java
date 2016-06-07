package app.com.zennigan.android.dota2match.data;

/**
 * Created by Zennigan on 6/4/2016.
 */
import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;



public class Dota2MatchProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private Dota2MatchDbHelper mOpenHelper;

    public static final String ACTION_DATA_UPDATED =
            "app.com.zennigan.android.dota2match.ACTION_DATA_UPDATED";

    static final int HEROES = 100;
    static final int HEROES_WITH_ID = 200;
    static final int MATCHES = 300;
    static final int MATCHES_WITH_ID = 400;
    static final int MATCH_PLAYERS = 500;
    static final int MATCH_PLAYERS_WITH_ID = 600;
    static final int MATCH_PLAYERS_WITH_ACCOUNT_ID = 700;
    static final int MATCH_PLAYERS_WITH_MATCH_ID = 800;

    private static final SQLiteQueryBuilder heroQueryBuilder;
    private static final SQLiteQueryBuilder matchQueryBuilder;
    private static final SQLiteQueryBuilder match_playerQueryBuilder;
    static{
        heroQueryBuilder = new SQLiteQueryBuilder();
        heroQueryBuilder.setTables(Dota2MatchContract.HeroEntry.TABLE_NAME);


        matchQueryBuilder = new SQLiteQueryBuilder();
        matchQueryBuilder.setTables(Dota2MatchContract.MatchEntry.TABLE_NAME);

        match_playerQueryBuilder = new SQLiteQueryBuilder();
        match_playerQueryBuilder.setTables(
                Dota2MatchContract.MatchEntry.TABLE_NAME + " LEFT JOIN " +
                        Dota2MatchContract.MatchPlayerEntry.TABLE_NAME +
                        " ON " + Dota2MatchContract.MatchPlayerEntry.TABLE_NAME  +
                        "." + Dota2MatchContract.MatchPlayerEntry.COLUMN_MATCH_ID +
                        " = " + Dota2MatchContract.MatchEntry.TABLE_NAME +
                        "." + Dota2MatchContract.MatchEntry._ID + " LEFT JOIN " +
                        Dota2MatchContract.HeroEntry.TABLE_NAME +
                        " ON " + Dota2MatchContract.MatchPlayerEntry.TABLE_NAME  +
                        "." + Dota2MatchContract.MatchPlayerEntry.COLUMN_HERO_ID +
                        " = " + Dota2MatchContract.HeroEntry.TABLE_NAME +
                        "." + Dota2MatchContract.HeroEntry._ID
        );
    }


    private static final String sByMatchIDSelection =
            Dota2MatchContract.MatchEntry.TABLE_NAME+
                    "." + Dota2MatchContract.MatchEntry.COLUMN_ID + " = ? ";

    private static final String sByAccountIDSelection =
            Dota2MatchContract.MatchPlayerEntry.TABLE_NAME+
                    "." + Dota2MatchContract.MatchPlayerEntry.COLUMN_ACCOUNT_ID + " = ? ";

    private static final String sMatchPlayerByIDSelection =
            Dota2MatchContract.MatchPlayerEntry.TABLE_NAME+
                    "." + Dota2MatchContract.MatchPlayerEntry.COLUMN_ID + " = ? ";

    private static final String sByHeroIDSelection =
            Dota2MatchContract.HeroEntry.TABLE_NAME+
                    "." + Dota2MatchContract.HeroEntry.COLUMN_ID + " = ? ";


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Dota2MatchContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, Dota2MatchContract.PATH_HEROES, HEROES);
        matcher.addURI(authority, Dota2MatchContract.PATH_HEROES + "/*", HEROES_WITH_ID);

        matcher.addURI(authority, Dota2MatchContract.PATH_MATCHES, MATCHES);
        matcher.addURI(authority, Dota2MatchContract.PATH_MATCHES + "/*", MATCHES_WITH_ID);

        matcher.addURI(authority, Dota2MatchContract.PATH_MATCH_PLAYERS, MATCH_PLAYERS);
        matcher.addURI(authority, Dota2MatchContract.PATH_MATCH_PLAYERS + "/#", MATCH_PLAYERS_WITH_ID);
        matcher.addURI(authority, Dota2MatchContract.PATH_MATCH_PLAYERS + "/" + Dota2MatchContract.PATH_PLAYERS + "/*", MATCH_PLAYERS_WITH_ACCOUNT_ID);
        matcher.addURI(authority, Dota2MatchContract.PATH_MATCH_PLAYERS + "/" + Dota2MatchContract.PATH_MATCHES + "/*", MATCH_PLAYERS_WITH_MATCH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new Dota2MatchDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case HEROES:
                return Dota2MatchContract.HeroEntry.CONTENT_TYPE;
            case HEROES_WITH_ID:
                return Dota2MatchContract.HeroEntry.CONTENT_TYPE;
            case MATCHES:
                return Dota2MatchContract.MatchEntry.CONTENT_TYPE;
            case MATCHES_WITH_ID:
                return Dota2MatchContract.MatchEntry.CONTENT_TYPE;
            case MATCH_PLAYERS:
                return Dota2MatchContract.MatchPlayerEntry.CONTENT_TYPE;
            case MATCH_PLAYERS_WITH_ID:
                return Dota2MatchContract.MatchPlayerEntry.CONTENT_TYPE;
            case MATCH_PLAYERS_WITH_MATCH_ID:
                return Dota2MatchContract.MatchPlayerEntry.CONTENT_TYPE;
            case MATCH_PLAYERS_WITH_ACCOUNT_ID:
                return Dota2MatchContract.MatchPlayerEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case HEROES: {
                retCursor = heroQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case HEROES_WITH_ID: {
                retCursor = heroQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sByHeroIDSelection,
                        new String[]{Dota2MatchContract.HeroEntry.getIDFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MATCHES: {
                retCursor = matchQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MATCHES_WITH_ID: {
                retCursor = matchQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sByMatchIDSelection,
                        new String[]{Dota2MatchContract.MatchEntry.getIDFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MATCH_PLAYERS: {
                retCursor =  match_playerQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MATCH_PLAYERS_WITH_ID: {
                retCursor = match_playerQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sMatchPlayerByIDSelection,
                        new String[]{Dota2MatchContract.MatchPlayerEntry.getIDFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MATCH_PLAYERS_WITH_ACCOUNT_ID: {
                retCursor =  match_playerQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sByAccountIDSelection,
                        new String[]{Dota2MatchContract.MatchPlayerEntry.getAccountIDFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MATCH_PLAYERS_WITH_MATCH_ID: {
                retCursor =  match_playerQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sByMatchIDSelection,
                        new String[]{Dota2MatchContract.MatchPlayerEntry.getMatchIDFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case HEROES: {
                long _id = db.insert(Dota2MatchContract.HeroEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    returnUri = Dota2MatchContract.HeroEntry.buildMatchUri(_id);
                }
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MATCHES: {
                long _id = db.insert(Dota2MatchContract.MatchEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    returnUri = Dota2MatchContract.MatchEntry.buildMatchUri(_id);
                }
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MATCH_PLAYERS: {
                long _id = db.insert(Dota2MatchContract.MatchPlayerEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = Dota2MatchContract.MatchPlayerEntry.buildMatchPlayerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        if ( null == selection ) selection = "1";
        switch (match) {
            case HEROES:
                rowsDeleted = db.delete(Dota2MatchContract.HeroEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MATCHES:
                rowsDeleted = db.delete(Dota2MatchContract.MatchEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MATCH_PLAYERS:
                rowsDeleted = db.delete(Dota2MatchContract.MatchPlayerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case HEROES:
                rowsUpdated = db.update(Dota2MatchContract.HeroEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MATCHES:
                rowsUpdated = db.update(Dota2MatchContract.MatchEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MATCH_PLAYERS:
                rowsUpdated = db.update(Dota2MatchContract.MatchPlayerEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case HEROES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(Dota2MatchContract.HeroEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case MATCHES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(Dota2MatchContract.MatchEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case MATCH_PLAYERS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(Dota2MatchContract.MatchPlayerEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
