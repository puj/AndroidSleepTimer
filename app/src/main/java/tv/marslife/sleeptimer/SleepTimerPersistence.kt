package tv.marslife.sleeptimer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SleepTimerPersistence {

    companion object {
        private const val SHARED_PREFERENCES_TAG = "SleepTimerPreferences";
        private const val SLEEP_TIMER_SLEEP_HOUR = "SleepTimerPreferencesHour"
        private const val SLEEP_TIMER_SLEEP_MINUTE = "SleepTimerPreferencesMinute"

        public fun saveHour(context: Context, hour: Int) {
            val prefs = context.getSharedPreferences(
                SHARED_PREFERENCES_TAG,
                AppCompatActivity.MODE_PRIVATE
            );
            val editor = prefs?.edit();
            editor?.putInt(SLEEP_TIMER_SLEEP_HOUR, hour);
            editor?.apply();
        }

        public fun saveMinute(context: Context, minute: Int) {
            val prefs = context.getSharedPreferences(
                SHARED_PREFERENCES_TAG,
                AppCompatActivity.MODE_PRIVATE
            );
            val editor = prefs?.edit();
            editor?.putInt(SLEEP_TIMER_SLEEP_MINUTE, minute);
            editor?.apply();
        }

        public fun getHour(context: Context): Int {
            val prefs = context.getSharedPreferences(
                SHARED_PREFERENCES_TAG,
                AppCompatActivity.MODE_PRIVATE
            );
            val c = Calendar.getInstance()
            val currentHour = c.get(Calendar.HOUR_OF_DAY)

            return prefs.getInt(SLEEP_TIMER_SLEEP_HOUR, currentHour)
        }


        public fun getMinute(context: Context): Int {
            val prefs = context.getSharedPreferences(
                SHARED_PREFERENCES_TAG,
                AppCompatActivity.MODE_PRIVATE
            );
            val c = Calendar.getInstance()
            val currentMinute = c.get(Calendar.MINUTE)

            return prefs.getInt(SLEEP_TIMER_SLEEP_MINUTE, currentMinute)
        }
    }
}