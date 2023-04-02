@file:Suppress("DEPRECATION")

package tk.zwander.wifilist.util

import android.net.wifi.WifiConfiguration
import com.google.gson.GsonBuilder
import de.siegmar.fastcsv.writer.CsvWriter
import tk.zwander.wifilist.data.WiFiExportItem
import java.io.StringWriter

object WiFiExportGenerator {
    fun Collection<WifiConfiguration>.mapToExportItems(): List<WiFiExportItem> {
        return map {
            WiFiExportItem(
                ssid = it.printableSsid,
                security = WifiConfiguration.KeyMgmt.strings[it.authType],
                password = it.simpleKey ?: "",
            )
        }
    }

    fun List<WiFiExportItem>.toJson(): String {
        val gson = GsonBuilder().create()

        return gson.toJson(this)
    }

    fun List<WiFiExportItem>.toCsv(): String {
        val writer = StringWriter()
        val csv = CsvWriter.builder().build(writer)

        csv.writeRow("SSID", "PASSWORD", "SECURITY")

        forEach { item ->
            csv.writeRow(item.ssid, item.password, item.security)
        }

        csv.close()

        return writer.toString()
    }
}
