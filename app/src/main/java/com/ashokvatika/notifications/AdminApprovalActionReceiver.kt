package com.ashokvatika.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.KeyguardManager
import android.util.Log
import com.ashokvatika.data.AdminFirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AdminApprovalActionReceiver : BroadcastReceiver() {
  override fun onReceive(
    context: Context,
    intent: Intent,
  ) {
    if ((context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked) {
      return
    }
    val requestId = intent.getStringExtra(ApprovalActionRequestId).orEmpty()
    if (requestId.isBlank()) return

    val approved = when (intent.action) {
      ApprovalActionApprove -> true
      ApprovalActionDeny -> false
      else -> return
    }

    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
      try {
        AdminFirestoreRepository(context.applicationContext).updateAccessRequestStatus(
          requestId = requestId,
          approved = approved,
        )
        AdminApprovalNotifier.cancelIncomingApproval(context, requestId)
      } catch (error: Throwable) {
        Log.w("AdminApprovalAction", "Unable to update access request $requestId", error)
      } finally {
        pendingResult.finish()
      }
    }
  }
}
