@file:Suppress("DEPRECATION")

package tk.zwander.wifilist.util

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiConfiguration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.reflect.Type

object Preferences {
    object Keys {
        val CACHE_NETWORKS = booleanPreferencesKey("cache_networks")
        val CACHED_INFO = stringPreferencesKey("cached_info")
    }

    val Context.store by preferencesDataStore("settings")
    val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriDeserializer)
        .create()

    val Context.cacheNetworks: Flow<Boolean>
        get() = store.data.map { it[Keys.CACHE_NETWORKS] ?: false }

    val Context.cachedInfo: Flow<List<WifiConfiguration>>
        get() = store.data.map {
            gson.fromJson(
                it[Keys.CACHED_INFO],
                object : TypeToken<ArrayList<WifiConfiguration>>() {}.type
            ) ?: listOf()
        }

    suspend fun Context.updateCacheNetworks(cache: Boolean) {
        store.edit {
            it[Keys.CACHE_NETWORKS] = cache
        }
    }

    suspend fun Context.updateCachedInfo(info: List<WifiConfiguration>) {
        store.edit {
            it[Keys.CACHED_INFO] = gson.toJson(info)
        }
    }

    object UriDeserializer : JsonDeserializer<Uri> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Uri {
            return Uri.parse(json?.toString())
        }
    }
}
