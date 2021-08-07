package tk.zwander.wifilist

import android.app.Application
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        HiddenApiBypass.addHiddenApiExemptions("L")
    }
}