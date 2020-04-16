# RealTimeStamp

Android library for time stamping using NTP Protocol.

Based on Instacart's TrueTime library with patch from https://github.com/martinfilipekcz/truetime-android.git

Re-factored, reduce and re-implemented in Kotlin.

## Motivation

Java's System.currentTimeMillis is not reliable for timestamping, prone to user configuration changes to the device.
Using NTP Protocol to get current time is more reliable.

## Download

```
dependencies {
     ...
     implementation 'id.zeheater.realtimestamp:realtimestamp:0.1-alpha'
     ...
}
```

## Usage

Initialize the library by subclassing your application


```
class SampleApp : Application() {

    companion object {
        object Rtc : RealTimeStamp()
    }

    override fun onCreate() {
        super.onCreate()
        Rtc.init(this@SampleApp)
    }
}
```


```
SampleApp.Companion.Rtc.now("yyyy-MM-dd HH:mm:ss")
```
