package id.zeheater.samplerts

import android.app.Application
import id.zeheater.realtimestamp.RealTimeStamp

class SampleApp : Application() {

    companion object {
        object Rtc : RealTimeStamp()
    }

    override fun onCreate() {
        super.onCreate()
        Rtc.init(this@SampleApp)
    }
}