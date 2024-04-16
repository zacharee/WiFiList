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
                security = it.securityParamsObj?.type?.name ?: "UNKNOWN",
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

        csv.writeRecord("SSID", "PASSWORD", "SECURITY")

        forEach { item ->
            csv.writeRecord(item.ssid, item.password, item.security)
        }

        csv.close()

        return writer.toString()
    }
}
