package tv.marslife.sleeptimer

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlin.coroutines.coroutineContext

class SleepTimerAdminReceiver : BroadcastReceiver() {

    companion object {
        private var dpm: DevicePolicyManager? = null
        public fun isActiveAdmin(context: Context): Boolean {
            if (dpm == null) {
                dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            };
            return dpm!!.isAdminActive(ComponentName(context, this::class.java))
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
    }

    private fun showToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun onEnabled(context: Context, intent: Intent) {
        showToast(context, "Admin enabled");
    }


}