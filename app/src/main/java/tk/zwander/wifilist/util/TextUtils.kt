package tk.zwander.wifilist.util

fun String.stripQuotes(): String {
    return if (startsWith("\"") && endsWith("\"")) {
        substring(1 until lastIndex)
    } else {
        this
    }
}