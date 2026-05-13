package com.ashokvatika.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ashokvatika.MainActivity
import com.ashokvatika.app.R

private const val ApprovalChannelId = "approval_requests"
private const val ApprovalChannelName = "Approval Requests"

object AdminApprovalNotifier {
  fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
    val channel = NotificationChannel(
      ApprovalChannelId,
      ApprovalChannelName,
      NotificationManager.IMPORTANCE_HIGH,
    ).apply {
      description = "Incoming customer approval requests"
    }
    notificationManager.createNotificationChannel(channel)
  }

  fun showIncomingApproval(
    context: Context,
    requestId: String,
    customerName: String,
    customerPhone: String,
  ) {
    ensureChannel(context)
    if (!canPostNotifications(context)) return

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      requestId.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, ApprovalChannelId)
      .setSmallIcon(R.drawable.ic_launcher)
      .setContentTitle("Incoming customer call")
      .setContentText(
        if (customerPhone.isBlank()) customerName else "$customerName • $customerPhone",
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_CALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    NotificationManagerCompat.from(context).notify(requestId.hashCode(), notification)
  }

  private fun canPostNotifications(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
  }
}
