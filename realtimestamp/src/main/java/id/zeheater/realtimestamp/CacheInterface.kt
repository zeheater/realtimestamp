package id.zeheater.realtimestamp

interface CacheInterface {

    companion object {
        val KEY_CACHED_BOOT_TIME     = "id.zeheater.realtimestamp.cached_boot_time"
        val KEY_CACHED_DEVICE_UPTIME = "id.zeheater.realtimestamp.cached_device_uptime"
        val KEY_CACHED_SNTP_TIME     = "id.zeheater.realtimestamp.cached_sntp_time"
        val KEY_CACHED_BOOT_ID       = "id.zeheater.realtimestamp.cached_boot_id"
    }

    fun put(key: String?, value: Long)

    fun put(key: String?, value: String?)

    fun get(key: String?, defaultValue: Long): Long

    fun get(key: String?, defaultValue: String?): String?

    fun clear()
}