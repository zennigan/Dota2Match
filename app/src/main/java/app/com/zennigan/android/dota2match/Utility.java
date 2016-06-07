package app.com.zennigan.android.dota2match;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utility {

	public static String formatDate(long dateInMillis) {
		Time time = new Time();
		time.setToNow();
		SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
		return dbDateFormat.format(dateInMillis);
	}

	public static long convertUnixTime(long time){
		return 1000L*time;
	}


	public static String formatDuration(int seconds){
		int min = seconds/60;
		int sec = seconds - (min*60);

		return String.format("%02d:%02d",min,sec);
	}


	public static final String DATE_FORMAT = "dd-MMM hh:mm";
	public static final String DURATION_FORMAT = "mm:ss";

	public static String getStringForLobbyType(Context context, int lobby_type) {
        int stringId;
        switch (lobby_type) {
			  case 0:
						stringId = R.string.lobby_type_0;
						break;
			  case 1:
						stringId = R.string.lobby_type_1;
						break;
			  case 2:
						stringId = R.string.lobby_type_2;
						break;
			  case 3:
						stringId = R.string.lobby_type_3;
						break;
			  case 4:
						stringId = R.string.lobby_type_4;
						break;
			  case 5:
						stringId = R.string.lobby_type_5;
						break;
			  case 6:
						stringId = R.string.lobby_type_6;
						break;
			  case 7:
						stringId = R.string.lobby_type_7;
						break;
			  case 8:
						stringId = R.string.lobby_type_8;
						break;
			default:
                return context.getString(R.string.lobby_type_unknown);
		}
		return context.getString(stringId);
	}
	
	public static String getStringForGameMode(Context context, int game_mode) {
        int stringId;
        switch (game_mode) {
			  case 0:
						stringId = R.string.game_mode_0;
						break;
			  case 1:
						stringId = R.string.game_mode_1;
						break;
			  case 2:
						stringId = R.string.game_mode_2;
						break;
			  case 3:
						stringId = R.string.game_mode_3;
						break;
			  case 4:
						stringId = R.string.game_mode_4;
						break;
			  case 5:
						stringId = R.string.game_mode_5;
						break;
			  case 6:
						stringId = R.string.game_mode_6;
						break;
			  case 7:
						stringId = R.string.game_mode_7;
						break;
			  case 8:
						stringId = R.string.game_mode_8;
						break;
			  case 9:
						stringId = R.string.game_mode_9;
						break;
			  case 10:
						stringId = R.string.game_mode_10;
						break;
			  case 11:
						stringId = R.string.game_mode_11;
						break;
			  case 12:
						stringId = R.string.game_mode_12;
						break;
			  case 13:
						stringId = R.string.game_mode_13;
						break;
			  case 14:
						stringId = R.string.game_mode_14;
						break;
			  case 16:
						stringId = R.string.game_mode_16;
						break;
			default:
                return context.getString(R.string.game_mode_unknown);
		}
		return context.getString(stringId);
	}
	
}