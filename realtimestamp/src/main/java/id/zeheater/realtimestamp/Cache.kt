package id.zeheater.realtimestamp

import android.app.Application
import android.content.Context.MODE_PRIVATE

class Cache(context: Application) : CacheInterface {

    private val KEY_CACHED_SHARED_PREFS = "id.zeheater.realtimestamp.shared_preferences"

    private val _sharedPreferences = context.getSharedPreferences(KEY_CACHED_SHARED_PREFS, MODE_PRIVATE)

    override fun put(key: String?, value: Long) {
        _sharedPreferences.edit().putLong(key, value).apply()
    }

    override fun put(key: String?, value: String?) {
        _sharedPreferences.edit().putString(key, value).apply()
    }

    override fun get(key: String?, defaultValue: Long): Long {
        return _sharedPreferences.getLong(key, defaultValue)
    }

    override fun get(key: String?, defaultValue: String?): String? {
        return _sharedPreferences.getString(key, defaultValue)
    }

    override fun clear() {
        remove(CacheInterface.KEY_CACHED_BOOT_TIME)
        remove(CacheInterface.KEY_CACHED_BOOT_ID)
        remove(CacheInterface.KEY_CACHED_DEVICE_UPTIME)
        remove(CacheInterface.KEY_CACHED_SNTP_TIME)
    }

    private fun remove(key : String) {
        _sharedPreferences.edit().remove(key).apply()
    }

}