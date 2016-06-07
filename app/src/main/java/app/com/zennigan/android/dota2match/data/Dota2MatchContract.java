package app.com.zennigan.android.dota2match.data;

/**
 * Created by Zennigan on 6/4/2016.
 */
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the weather database.
 */
public class Dota2MatchContract {

    public static final String CONTENT_AUTHORITY = "app.com.zennigan.android.dota2match";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_HEROES = "heroes";
    public static final String PATH_MATCHES = "matches";
    public static final String PATH_PLAYERS = "players";
    public static final String PATH_MATCH_PLAYERS = "match_players";


    public static final class HeroEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HEROES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HEROES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HEROES;

        public static final String TABLE_NAME = "heroes";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE_URL = "image_url";

        public static Uri buildMatchUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIDFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }


    public static final class MatchEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MATCHES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MATCHES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MATCHES;

        public static final String TABLE_NAME = "matches";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_RADIANT_WIN = "radiant_win";
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_LOBBY_TYPE_NAME = "lobby_type_name";
        public static final String COLUMN_GAME_MODE_NAME = "game_mode_name";

        public static Uri buildMatchUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIDFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class MatchPlayerEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MATCH_PLAYERS).build();

        public static final Uri CONTENT_URI_WITH_MATCH_ID =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MATCH_PLAYERS).appendPath(PATH_MATCHES).build();

        public static final Uri CONTENT_URI_WITH_ACCOUNT_ID =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MATCH_PLAYERS).appendPath(PATH_PLAYERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MATCH_PLAYERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MATCH_PLAYERS;

        public static final String TABLE_NAME = "match_players";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_ACCOUNT_ID = "account_id";
        public static final String COLUMN_ACCOUNT_NAME= "account_name";
        public static final String COLUMN_MATCH_ID = "match_id";
        public static final String COLUMN_HERO_ID = "hero_id";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_KILLS = "kills";
        public static final String COLUMN_DEATHS = "deaths";
        public static final String COLUMN_ASSISTS = "assists";

        public static Uri buildMatchPlayerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWithMatchIDUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI_WITH_MATCH_ID, id);
        }

        public static Uri buildWithAccountIDUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI_WITH_ACCOUNT_ID, id);
        }

        public static String getIDFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getMatchIDFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getAccountIDFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }
}