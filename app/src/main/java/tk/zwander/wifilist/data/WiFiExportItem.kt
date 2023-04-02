package tk.zwander.wifilist.data

data class WiFiExportItem(
    val ssid: String,
    val security: String,
    val password: String,
)