package tk.zwander.wifilist.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.launchUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)

        startActivity(intent)
    } catch (_: Exception) {}
}