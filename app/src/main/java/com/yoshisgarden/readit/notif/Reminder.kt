package com.yoshisgarden.readit.notif

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.yoshisgarden.readit.MainActivity
import com.yoshisgarden.readit.R
import java.util.Calendar

const val REMINDER_CHANNEL_ID = "readit_daily_reminder"
private const val REMINDER_REQUEST = 4001

object ReminderScheduler {

    fun ensureChannel(context: Context) {
        val mgr = context.getSystemService<NotificationManager>() ?: return
        if (mgr.getNotificationChannel(REMINDER_CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    "学習リマインダー",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "毎日の学習リマインダー通知" },
            )
        }
    }

    fun schedule(context: Context, hour: Int, minute: Int) {
        ensureChannel(context)
        val alarm = context.getSystemService<AlarmManager>() ?: return
        val pi = pendingIntent(context)
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }
        alarm.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            next.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pi,
        )
    }

    fun cancel(context: Context) {
        val alarm = context.getSystemService<AlarmManager>() ?: return
        alarm.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.ensureChannel(context)
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ReadIT で今日の英語学習を 📖")
            .setContentText("今日の復習カードが待っています。すき間時間に1分だけでも！")
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        val mgr = context.getSystemService<NotificationManager>() ?: return
        mgr.notify(REMINDER_REQUEST, notification)
    }
}
