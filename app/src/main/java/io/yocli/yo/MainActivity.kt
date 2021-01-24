package io.yocli.yo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.sentry.SentryLevel.ERROR
import io.sentry.SentryLevel.INFO
import io.sentry.android.core.SentryAndroid
import io.sentry.android.timber.SentryTimberIntegration
import io.yocli.yo.R.id
import io.yocli.yo.R.layout
import io.yocli.yo.ui.IntroFragment
import timber.log.Timber

class MainActivity : AppCompatActivity(layout.main_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        SentryAndroid.init(this) { options ->
            options.release = "yocli.@${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
//            options.setDebug(BuildConfig.DEBUG)
            options.isAnrEnabled = true
            options.isEnableSessionTracking = true;

            options.addIntegration(
                SentryTimberIntegration(
                    minEventLevel = ERROR,
                    minBreadcrumbLevel = INFO
                )
            )
        }
    }
}