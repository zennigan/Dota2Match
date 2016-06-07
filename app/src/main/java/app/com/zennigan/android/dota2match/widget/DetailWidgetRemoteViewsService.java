package app.com.zennigan.android.dota2match.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import app.com.zennigan.android.dota2match.MatchDetailActivity;
import app.com.zennigan.android.dota2match.MatchDetailFragment;
import app.com.zennigan.android.dota2match.R;
import app.com.zennigan.android.dota2match.Utility;
import app.com.zennigan.android.dota2match.data.Dota2MatchContract;

import java.text.DecimalFormat;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] PROJECTION = {
        Dota2MatchContract.MatchEntry.COLUMN_ID,
        Dota2MatchContract.MatchEntry.COLUMN_RADIANT_WIN,
        Dota2MatchContract.MatchEntry.COLUMN_START_TIME,
        Dota2MatchContract.MatchEntry.COLUMN_DURATION,
        Dota2MatchContract.MatchEntry.COLUMN_LOBBY_TYPE_NAME,
        Dota2MatchContract.MatchEntry.COLUMN_GAME_MODE_NAME
	};

    // these indices must match the projection
    static final int INDEX_COLUMN_ID = 0;
    static final int INDEX_COLUMN_RADIANT_WIN = 1;
    static final int INDEX_COLUMN_START_TIME= 2;
    static final int INDEX_COLUMN_DURATION = 3;
    static final int INDEX_COLUMN_LOBBY_TYPE_NAME = 4;
    static final int INDEX_COLUMN_GAME_MODE_NAME= 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

               data = getContentResolver().query(Dota2MatchContract.MatchEntry.CONTENT_URI,
                        PROJECTION,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);
						
				String lobby_type_name = Utility.getStringForLobbyType(getApplicationContext(), data.getInt(INDEX_COLUMN_LOBBY_TYPE_NAME));
				String game_mode_name  = Utility.getStringForGameMode(getApplicationContext(), data.getInt(INDEX_COLUMN_GAME_MODE_NAME));
				String radiant_win = "true".equals(data.getString(INDEX_COLUMN_RADIANT_WIN)) ? getString(R.string.radiant_win): getString(R.string.dire_win);
              
                views.setTextViewText(R.id.widget_match_id, data.getString(INDEX_COLUMN_ID));
                views.setTextViewText(R.id.widget_radiant_win, radiant_win);
                views.setTextViewText(R.id.widget_lobby_type, lobby_type_name);
                views.setTextViewText(R.id.widget_game_mode, game_mode_name);
                views.setTextViewText(R.id.widget_start_time,data.getString(INDEX_COLUMN_START_TIME));
                views.setTextViewText(R.id.widget_duration,data.getString(INDEX_COLUMN_DURATION));

                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra(MatchDetailFragment.KEY_MATCH_ID, data.getString(INDEX_COLUMN_ID));
             
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_COLUMN_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}