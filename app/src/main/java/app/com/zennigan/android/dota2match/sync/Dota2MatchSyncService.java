package app.com.zennigan.android.dota2match.sync;

/**
 * Created by Zennigan on 6/4/2016.
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class Dota2MatchSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static Dota2MatchSyncAdapter sDota2MatchSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sDota2MatchSyncAdapter == null) {
                sDota2MatchSyncAdapter = new Dota2MatchSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sDota2MatchSyncAdapter.getSyncAdapterBinder();
    }
}