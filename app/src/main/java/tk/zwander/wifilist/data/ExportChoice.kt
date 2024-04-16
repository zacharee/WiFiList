@file:Suppress("DEPRECATION")

package tk.zwander.wifilist.data

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.net.wifi.WifiConfiguration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import tk.zwander.wifilist.R
import tk.zwander.wifilist.util.WiFiExportGenerator.mapToExportItems
import tk.zwander.wifilist.util.WiFiExportGenerator.toCsv
import tk.zwander.wifilist.util.WiFiExportGenerator.toJson
import java.util.Date

data class ExportChoice(
    val mimeType: String,
    @StringRes val titleRes: Int,
    val content: () -> String,
)

@Composable
fun rememberExportChoices(configs: List<WifiConfiguration>): List<ExportChoice> {
    return remember(configs.toList()) {
        val exportItems = configs.mapToExportItems()

        listOf(
            ExportChoice(
                mimeType = "text/csv",
                titleRes = R.string.csv,
                content = { exportItems.toCsv() },
            ),
            ExportChoice(
                mimeType = "application/json",
                titleRes = R.string.json,
                content = { exportItems.toJson() },
            ),
        )
    }
}

@Composable
fun rememberExportChoiceLaunchers(choices: List<ExportChoice>): Map<String, () -> Unit> {
    val context = LocalContext.current

    return choices.associate {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(it.mimeType)) { uri ->
            context.handleChoiceLauncherResult(uri, it.content())
        }

        it.mimeType to {
            val dateFormat = SimpleDateFormat.getDateTimeInstance()
            val fileName = "WiFiList_${dateFormat.format(Date())}.${it.mimeType.split("/").last()}"

            launcher.launch(fileName)
        }
    }
}

private fun Context.handleChoiceLauncherResult(uri: Uri?, content: String) {
    uri?.let { safeUri ->
        contentResolver.openOutputStream(safeUri)?.use { output ->
            output.bufferedWriter().use { writer ->
                writer.write(content)
            }
        }
    }
}
