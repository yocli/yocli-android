package io.yocli.yo.notification

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.yocli.yo.R
import timber.log.Timber

private const val CHANNEL_COMPLETE = "complete"

class YocliFcmService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val nmc = NotificationManagerCompat.from(this)

        Timber.w("received notification")
        val rNotif = message.notification ?: return

        val channel = nmc.getOrCreateChannel(CHANNEL_COMPLETE)
        val notification = NotificationCompat.Builder(this, channel.id)
            .setContentTitle(rNotif.title)
            .setContentText(rNotif.body)
            .setSmallIcon(IconCompat.createWithResource(this.applicationContext, R.drawable.ic_yo_notif))
            .build()
        nmc.notify(1, notification)
    }
}

private fun NotificationManagerCompat.getOrCreateChannel(channelId: String): NotificationChannelCompat {
    return getNotificationChannelCompat(channelId) ?: run {
        createNotificationChannel(
            NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Yo!")
                .setShowBadge(true)
                .setDescription("Notifies when your Yo-CLI task is complete")
                .build())
        getNotificationChannelCompat(channelId)!!
    }
}
