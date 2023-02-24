package tk.zwander.wifilist

import android.app.Application
import com.bugsnag.android.Bugsnag
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Bugsnag.start(this)

        HiddenApiBypass.addHiddenApiExemptions("L")
    }
}