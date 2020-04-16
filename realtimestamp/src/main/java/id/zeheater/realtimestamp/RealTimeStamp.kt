package id.zeheater.realtimestamp

import android.app.Application
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.BufferedReader
import java.io.FileReader
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


open class RealTimeStamp {

    private lateinit var appContext : Application
    private lateinit var cacheController : Cache

    private object sntpClient : SntpClient()

    private var disposeme : Disposable? = null

    private var isInitCalled : Boolean = false
    private var isAllowFallbackToSystemClock = false
    private var isNtpReady : AtomicBoolean = AtomicBoolean(false)

    companion object {

        private const val _rootDelayMax = 100f
        private const val _rootDispersionMax = 100f
        private const val _serverResponseDelayMax = 750
        private const val _udpSocketTimeoutInMillis = 30000
        private const val _onErrorDelayMaxMillis = 3000L

        private const val _ntpHost = "time.google.com"
    }

    /**
     * Initialize library
     *
     * @param applicationContext the application context this library called
     * @param allowFallback allow library to fallback using System's currentTimeMillis
     */
    fun init(applicationContext: Application, allowFallback : Boolean = false) {
        appContext = applicationContext
        cacheController = Cache(appContext)
        isAllowFallbackToSystemClock = allowFallback
        isInitCalled = true
        val cachedBootId = getCacheBootId()
        if (cachedBootId == "") {
            isNtpReady.set(false)
            requestInitialTime()
        } else {
            if (cachedBootId != getCurrentBootId()) {
                isNtpReady.set(false)
                requestInitialTime()
            } else {
                isInitCalled = true
                isNtpReady.set(true)
                // Do Nothing
            }
        }
        // Flow
        // 1. Check cached entry
        //         |
        //         |_______exist________[ isCachedBootId == currentBootId ]
        //         |                                                      |
        //         |______!exist____[ requestIntialTime ]                 |____YES____ [ Done ]
        //                                                                |
        //                                                                |_____NO____ [ requestInitialTime ]
    }

    /**
     * Get current unix timestamp
     *
     * @return current unix timestamp in millisecond
     * @throws IllegalStateException
     */
    fun now() : Long {
        return _Now().time
    }

    /**
     * Get formatted current unix timestamp
     *
     * @param pattern format current unix timestamp with pattern
     * @return formatted current unix timestamp
     * @throws IllegalStateException
     */
    fun now(pattern: String) : String {
        return java.text.SimpleDateFormat(pattern, Locale.getDefault()).format(_Now())
    }

    private fun _Now(): Date {
        try {
            check(isInitCalled) { "You need to call init() at least once." }
            check(isNtpReady.get()) { "NTP Not yet ready." }

            val cachedSntpTime: Long = getCachedSntpTime()
            val cachedDeviceUptime: Long = getCachedDeviceUptime()
            val deviceUptime = SystemClock.elapsedRealtime()
            val now = cachedSntpTime + (deviceUptime - cachedDeviceUptime)
            return Date(now)
        } catch (ex: IllegalStateException) {
            if (isAllowFallbackToSystemClock) {
                return Date(System.currentTimeMillis())
            } else {
                throw ex
            }
        }
    }

    private val requestSntp = Single.fromCallable {
        android.util.Log.d(BuildConfig.LIBRARY_PACKAGE_NAME.substring(23), "requestSntp()")
        val res = sntpClient.requestTime(
            _ntpHost,
            _rootDelayMax,
            _rootDispersionMax,
            _serverResponseDelayMax,
            _udpSocketTimeoutInMillis
        )
        val sntpTime = sntpClient.cachedSntpTime
        val deviceUpTime = sntpClient.cachedDeviceUptime
        val bootTime = sntpTime - deviceUpTime

        cacheController.put(CacheInterface.KEY_CACHED_BOOT_ID, getCurrentBootId())
        cacheController.put(CacheInterface.KEY_CACHED_BOOT_TIME, bootTime)
        cacheController.put(CacheInterface.KEY_CACHED_DEVICE_UPTIME, deviceUpTime)
        cacheController.put(CacheInterface.KEY_CACHED_SNTP_TIME, sntpTime)
        isNtpReady.set(true)

        return@fromCallable res
    }

    private fun requestInitialTime() {
        requestSntp.doOnSubscribe {
            disposeme = it
        }.subscribeOn(
            Schedulers.single()
        ).observeOn(
            AndroidSchedulers.mainThread()
        ).retryWhen { f ->
            android.util.Log.d(BuildConfig.LIBRARY_PACKAGE_NAME.substring(23), "requestSntp() failed, retrying...")
            f.delay(_onErrorDelayMaxMillis, TimeUnit.MILLISECONDS)
        }.subscribe({ _ -> disposeme?.dispose() }, { err ->
            android.util.Log.e(BuildConfig.LIBRARY_PACKAGE_NAME.substring(23), err?.message?:"null message")
            throw err
        })
    }

    private fun getCurrentBootId() : String {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return Settings.Global.getInt(appContext.contentResolver, Settings.Global.BOOT_COUNT).toString()
        } else {
            var bufferedReader : BufferedReader? = null
            try {
                bufferedReader = BufferedReader(FileReader("proc/sys/kernel/random/boot_id"))
                return bufferedReader.readText()
            } catch(ex : Exception) {
                return ""
            } finally {
                bufferedReader?.close()
            }
        }
    }

    private fun getCachedSntpTime() : Long {
        return cacheController.get(CacheInterface.KEY_CACHED_SNTP_TIME, 0L)
    }

    private fun getCachedDeviceUptime() : Long {
        return cacheController.get(CacheInterface.KEY_CACHED_DEVICE_UPTIME, 0L)
    }

    private fun getCacheBootId() : String? {
        return cacheController.get(CacheInterface.KEY_CACHED_BOOT_ID, "")
    }
}