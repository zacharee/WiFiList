package tk.zwander.wifilist.util

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

val Context.hasShizukuPermission: Boolean
    get() {
        if (!Shizuku.pingBinder()) {
            return false
        }

        return if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            checkCallingOrSelfPermission(ShizukuProvider.PERMISSION) == PackageManager.PERMISSION_GRANTED
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }