package id.zeheater.samplerts

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btntime.setOnClickListener {
            try {
                timestampview.text = SampleApp.Companion.Rtc.now("yyyy-MM-dd HH:mm:ss")
            } catch (ex: IllegalStateException) {

            }
        }
    }
}
