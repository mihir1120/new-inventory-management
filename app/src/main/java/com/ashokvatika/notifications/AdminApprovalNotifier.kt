package com.ashokvatika.notifications

import android.Manifest
import android.annotation.SuppressLint
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
internal const val ApprovalActionRequestId = "approval_request_id"
internal const val ApprovalActionApprove = "com.ashokvatika.notifications.APPROVE_ACCESS_REQUEST"
internal const val ApprovalActionDeny = "com.ashokvatika.notifications.DENY_ACCESS_REQUEST"

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

  @SuppressLint("MissingPermission")
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
    val approveIntent = PendingIntent.getBroadcast(
      context,
      "${requestId}:approve".hashCode(),
      approvalActionIntent(context, requestId, ApprovalActionApprove),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val denyIntent = PendingIntent.getBroadcast(
      context,
      "${requestId}:deny".hashCode(),
      approvalActionIntent(context, requestId, ApprovalActionDeny),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notificationText = if (customerPhone.isBlank()) {
      customerName
    } else {
      "$customerName - $customerPhone"
    }

    val notification = NotificationCompat.Builder(context, ApprovalChannelId)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("Incoming customer call")
      .setContentText(notificationText)
      .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_CALL)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .addAction(NotificationCompat.Action.Builder(0, "Deny", denyIntent).setAuthenticationRequired(true).build())
      .addAction(NotificationCompat.Action.Builder(0, "Accept", approveIntent).setAuthenticationRequired(true).build())
      .build()

    NotificationManagerCompat.from(context).notify(requestId.hashCode(), notification)
  }

  fun cancelIncomingApproval(
    context: Context,
    requestId: String,
  ) {
    NotificationManagerCompat.from(context).cancel(requestId.hashCode())
  }

  private fun approvalActionIntent(
    context: Context,
    requestId: String,
    action: String,
  ): Intent {
    return Intent(context, AdminApprovalActionReceiver::class.java).apply {
      this.action = action
      putExtra(ApprovalActionRequestId, requestId)
    }
  }

  private fun canPostNotifications(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
  }
}
