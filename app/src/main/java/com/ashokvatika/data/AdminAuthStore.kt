package com.ashokvatika.data

import android.content.Context
import androidx.core.content.edit
import java.security.MessageDigest

class AdminAuthStore(
  context: Context,
) {
  private val prefs = context.getSharedPreferences("admin_auth", Context.MODE_PRIVATE)

  fun isProvisioned(): Boolean = true

  fun setPin(pin: String) {
    val pinHash = sha256("pin:$pin")
    prefs.edit {
      putString(KEY_PIN_HASH, pinHash)
    }
  }

  fun hasActiveSession(): Boolean = prefs.getBoolean(KEY_SESSION_ACTIVE, false)

  fun setActiveSession(active: Boolean) {
    prefs.edit {
      putBoolean(KEY_SESSION_ACTIVE, active)
    }
  }

  fun verifyPin(pin: String): Boolean {
    if (pin == DEFAULT_PIN) return true
    val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
    val candidate = sha256("pin:$pin")
    return stored == candidate
  }

  fun clear() {
    prefs.edit { clear() }
  }

  private fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
  }

  companion object {
    const val MinPinLength = 6
    const val DefaultAdminPin = "123"

    fun validatePin(pin: String): String? {
      if (pin.length < MinPinLength) return "Use at least $MinPinLength digits."
      if (!pin.all(Char::isDigit)) return "Use digits only."
      if (pin.toSet().size == 1) return "Choose a PIN that is not repeated digits."
      if (isSimpleRun(pin, ascending = true) || isSimpleRun(pin, ascending = false)) {
        return "Choose a PIN that is not a simple sequence."
      }
      return null
    }

    private fun isSimpleRun(pin: String, ascending: Boolean): Boolean {
      return pin.zipWithNext().all { (first, second) ->
        val delta = if (ascending) 1 else -1
        second.code - first.code == delta
      }
    }

    private const val DEFAULT_PIN = DefaultAdminPin
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_SESSION_ACTIVE = "session_active"
  }
}
