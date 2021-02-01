package io.yocli.yo.ui

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import io.yocli.yo.R

private const val KEY_TOKEN = "token"

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private var prefs: SharedPreferences? = app.applicationContext.getSharedPreferences("yocli", Context.MODE_PRIVATE)

    fun persistPairing(deviceToken: String) {
        prefs?.edit {
            putString(KEY_TOKEN, deviceToken)
        }
    }

    fun rePairDevice() {
        prefs?.edit { remove(KEY_TOKEN) }
    }

    override fun onCleared() {
        super.onCleared()
        prefs = null
    }

    fun getAppState(notificationManager: NotificationManagerCompat): AppUiState {
        return when {
            prefs!!.getString(KEY_TOKEN, null).isNullOrEmpty() -> AppUiState(
                icon = R.drawable.yo,
                title = R.string.unpaired_title,
                subtitle = R.string.unpaired_body1,
                body = R.string.unpaired_body2,
                infoButtonBlurb = R.string.instructions_blurb,
                infoButtonIcon = R.drawable.ic_copy,
                infoButton = R.string.instructions_link_button,
                scanButtonBlurb = R.string.scan_blurb,
                scanButton = R.string.scan_button
            )
            !notificationManager.areNotificationsEnabledCompat() -> AppUiState(
                icon = R.drawable.ic_announce,
                title = R.string.disabled_notifications_title,
                subtitle = R.string.disabled_notifications_body1,
                body = R.string.disabled_notifications_body2,
                infoButtonBlurb = null,
                infoButtonIcon = R.drawable.ic_settings,
                infoButton = R.string.disabled_notifications_change_button,
                scanButtonBlurb = null,
                scanButton = null
            )
            else -> AppUiState(
                icon = R.drawable.ic_linked,
                title = R.string.paired_title,
                subtitle = R.string.paired_body1,
                body = R.string.paired_body2,
                infoButtonBlurb = R.string.instructions_blurb,
                infoButtonIcon = R.drawable.ic_scan_qr,
                infoButton = R.string.instructions_link_button,
                scanButtonBlurb = R.string.paired_scan_instructions,
                scanButton = R.string.paired_scan_button
            )
        }
    }

    private fun NotificationManagerCompat.areNotificationsEnabledCompat() = when {
        !areNotificationsEnabled() -> false
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            notificationChannels.none { it.importance == NotificationManager.IMPORTANCE_NONE }
        }
        else -> true
    }
}

data class AppUiState(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    @StringRes val body: Int,
    @StringRes val infoButtonBlurb: Int?,
    @StringRes val infoButton: Int,
    @DrawableRes val infoButtonIcon: Int,
    @StringRes val scanButtonBlurb: Int?,
    @StringRes val scanButton: Int?,
)