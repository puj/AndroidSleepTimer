package tv.marslife.sleeptimer

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


class SettingsActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    var fabEditButton: FloatingActionButton? = null
    var fabRestoreButton: FloatingActionButton? = null
    var timeTextView: TextView? = null


    class TimePickerFragment(listener: TimePickerDialog.OnTimeSetListener) : DialogFragment(),
        TimePickerDialog.OnTimeSetListener {
        val mListener = listener;
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current time as the default values for the picker

            // Restore previously used value
            val startHour = SleepTimerPersistence.getHour(requireContext())
            val startMinute = SleepTimerPersistence.getMinute(requireContext())

            return TimePickerDialog(
                activity,
                this,
                startHour,
                startMinute,
                DateFormat.is24HourFormat(activity)
            )
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            mListener.onTimeSet(view, hourOfDay, minute);
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        SleepTimerPersistence.saveHour(this, hourOfDay)
        SleepTimerPersistence.saveMinute(this, minute)
        refreshTimeText();

        // Calculate time until sleep (in minutes)
        val minutesUntilSleep: Long = getMinutesUntil(hourOfDay, minute).toLong();
        SleepManager.scheduleSleep(this, minutesUntilSleep);

        // Calculate an appropriate time for wake
        val minutesUntilWake: Long = getMinutesUntil(8, 30).toLong();
        SleepManager.scheduleWake(this, minutesUntilWake);
    }

    private fun getMinutesUntil(hour: Int, minute: Int): Int {
        val c = Calendar.getInstance()
        val currentHour = c.get(Calendar.HOUR_OF_DAY)
        val currentMinute = c.get(Calendar.MINUTE)
        val totalCurrent = currentHour * 60 + currentMinute;

        val totalTarget = hour * 60 + minute;

        if (totalCurrent <= totalTarget) {
            // We haven't passed the time for today
            return totalTarget - totalCurrent;
        }

        return (totalTarget + 60 * 24 - totalCurrent);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun refreshTimeText() {
        val hour = SleepTimerPersistence.getHour(this)
        val minute = SleepTimerPersistence.getMinute(this)
        timeTextView?.setText("$hour:$minute");
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fabEditButton = findViewById<FloatingActionButton>(R.id.fabEditTimer);
        fabRestoreButton = findViewById<FloatingActionButton>(R.id.fabRestore);
        timeTextView = findViewById<TextView>(R.id.time);

        timeTextView?.setText("Yooooooooo!");
        fabEditButton?.setOnClickListener { view ->
            TimePickerFragment(this).show(supportFragmentManager, "timePicker")
        }
        fabRestoreButton?.setOnClickListener { view ->
            SleepManager.stopSleep(this);
        }
        refreshTimeText();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(applicationContext)) {
                val intent =
                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
                startActivityForResult(intent, 200)
            }


            val componentName: ComponentName =
                ComponentName(this, SleepTimerAdminReceiver::class.java)
            if (!SleepTimerAdminReceiver.isActiveAdmin(this)) {
                val activateDeviceAdminIntent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                activateDeviceAdminIntent.putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    componentName
                )

                activateDeviceAdminIntent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Time to lock your screen automatically"
                )
                startActivityForResult(
                    activateDeviceAdminIntent,
                    200
                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val canWrite = Settings.System.canWrite(this)
    }


}