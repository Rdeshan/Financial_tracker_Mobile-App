package com.example.mywalletapp.MyWallet.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mywalletapp.MyWallet.ui.MainActivity
import com.example.mywalletapp.R
import java.text.NumberFormat
import java.util.Locale

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "budget_alerts"
        const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Budget Alerts"
                val descriptionText = "Notifications for budget thresholds"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showBudgetNotification(monthlyBudget: Double) {
        try {
            val title = "Budget Set"
            val message = "Your monthly budget has been set to ${formatCurrency(monthlyBudget)}"
            showNotification(title, message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showBudgetAlert(title: String, message: String, isWarning: Boolean) {
        try {
            showNotification(title, message, isWarning)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotification(title: String, message: String, isWarning: Boolean) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            if (isWarning) {
                builder.setColor(context.getColor(R.color.errorColor))
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            format.format(amount)
        } catch (e: Exception) {
            e.printStackTrace()
            "$0.00"
        }
    }
}