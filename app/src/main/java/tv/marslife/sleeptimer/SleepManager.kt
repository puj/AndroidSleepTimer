package tv.marslife.sleeptimer

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.concurrent.futures.ResolvableFuture
import androidx.work.*
import androidx.work.ListenableWorker
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.TimeUnit


class SleepManager {

    class StartSleepWorker(
        context: Context,
        params: WorkerParameters
    ) : ListenableWorker(context, params) {
        private val mContext: Context = context;

        @RequiresApi(Build.VERSION_CODES.M)
        override fun startWork(): ListenableFuture<Result> {
            return CallbackToFutureAdapter.getFuture { completer: CallbackToFutureAdapter.Completer<Result> ->
                startSleep(mContext);
                completer.set(Result.success());
            }
        }

    }

    class StartWakeWorker(
        context: Context,
        params: WorkerParameters
    ) : ListenableWorker(context, params) {
        private val mContext: Context = context;

        @RequiresApi(Build.VERSION_CODES.M)
        override fun startWork(): ListenableFuture<Result> {
            return CallbackToFutureAdapter.getFuture { completer: CallbackToFutureAdapter.Completer<Result> ->
                stopSleep(mContext);
                completer.set(Result.success());
            }
        }

    }


    companion object {
        const val SLEEP_TIMEOUT_LONG = 1000 * 60 * 15;
        const val SLEEP_TIMEOUT_SHORT = 1000 * 15;
        const val SLEEP_TIMER_WORK_TAG = "SleepTimerWorkTag";
        const val WAKE_TIMER_WORK_TAG = "WakeTimerWorkTag";


        @RequiresApi(Build.VERSION_CODES.M)
        public fun scheduleSleep(
            context: Context,
            initialDelay: Long,
            periodicDelay: Long = 24,
            periodicDelayTimeUnit: TimeUnit = TimeUnit.HOURS
        ) {
            Log.i("SleepManager", "Scheduling sleep $initialDelay min from now");
            Log.i("SleepManager", "Scheduled to repeat every $periodicDelay $periodicDelayTimeUnit");

            // Don't let anything prevent this from happening
            val constraints: Constraints = Constraints.Builder().apply {
                setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                setRequiresBatteryNotLow(false)
                setRequiresCharging(false)
                setRequiresDeviceIdle(false)
            }.build()

            // Create the task
            val request: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
                StartSleepWorker::class.java,
                periodicDelay,
                periodicDelayTimeUnit,
            )
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .build()

            // Queue up the periodic task
            val workManager: WorkManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(
                SLEEP_TIMER_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            );
        }

        @RequiresApi(Build.VERSION_CODES.M)
        public fun scheduleWake(
            context: Context,
            initialDelay: Long,
            periodicDelay: Long = 24,
            periodicDelayTimeUnit: TimeUnit = TimeUnit.HOURS
        ) {
            Log.i("SleepManager", "Scheduling wake $initialDelay min from now");

            // Don't let anything prevent this from happening
            val constraints: Constraints = Constraints.Builder().apply {
                setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                setRequiresBatteryNotLow(false)
                setRequiresCharging(false)
                setRequiresDeviceIdle(false)
            }.build()

            // Create the task
            val request: PeriodicWorkRequest = PeriodicWorkRequest.Builder(
                StartWakeWorker::class.java,
                periodicDelay,
                periodicDelayTimeUnit,
            )
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .build()

            // Queue up the periodic task
            val workManager: WorkManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(
                WAKE_TIMER_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            );
        }

        public fun startSleep(context: Context) {
            setStayOnWhilePluggedIn(context, false);
            setBrightness(context, 0f);
            setMuted(context, true);

            val manager =
                context.getSystemService(AppCompatActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            manager.lockNow()
        }

        public fun stopSleep(context: Context) {
            setStayOnWhilePluggedIn(context, true);
            setBrightness(context, .7f);
            setMuted(context, false);
        }

        private fun setBrightness(context: Context, brightness: Float) {
            Settings.System.putInt(
                context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                (brightness * 255.0).toInt()
            )
        }

        private fun setStayOnWhilePluggedIn(context: Context, isEnabled: Boolean) {
            val intStayOn = if (isEnabled) 1 else 0;
            val intScreenTimeout = if (isEnabled) SLEEP_TIMEOUT_LONG else SLEEP_TIMEOUT_SHORT;


            Settings.System.putInt(
                context.contentResolver,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                intStayOn
            );
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT, intScreenTimeout
            );
        }

        private fun setMuted(context: Context, isMuted: Boolean) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (isMuted) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_MUTE,
                    AudioManager.FLAG_SHOW_UI
                );
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_NOTIFICATION,
                    AudioManager.ADJUST_MUTE,
                    AudioManager.FLAG_SHOW_UI
                );
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_SYSTEM,
                    AudioManager.ADJUST_MUTE,
                    AudioManager.FLAG_SHOW_UI
                );
            } else {
                val flags: Int = AudioManager.ADJUST_RAISE or AudioManager.FLAG_SHOW_UI;
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_UNMUTE,
                    flags
                );
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_NOTIFICATION,
                    AudioManager.ADJUST_UNMUTE,
                    flags
                );
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_SYSTEM,
                    AudioManager.ADJUST_UNMUTE,
                    flags
                );
            }
        }
    }
}