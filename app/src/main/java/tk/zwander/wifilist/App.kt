package tk.zwander.wifilist

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.getkeepsafe.relinker.ReLinker
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        ReLinker.loadLibrary(this, "bugsnag-ndk")
        ReLinker.loadLibrary(this, "bugsnag-plugin-android-anr")

        Bugsnag.start(this)

        HiddenApiBypass.addHiddenApiExemptions("L")
    }
}