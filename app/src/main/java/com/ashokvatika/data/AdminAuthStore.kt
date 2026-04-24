package com.ashokvatika.data

import android.content.Context
import androidx.core.content.edit
import java.security.MessageDigest

class AdminAuthStore(
  context: Context,
) {
  private val prefs = context.getSharedPreferences("admin_auth", Context.MODE_PRIVATE)

  fun isProvisioned(): Boolean = prefs.contains(KEY_PATTERN_HASH) && prefs.contains(KEY_PIN_HASH)

  fun setCredentials(pattern: List<Int>, pin: String) {
    val patternHash = sha256("pattern:${pattern.joinToString("-")}")
    val pinHash = sha256("pin:$pin")
    prefs.edit {
      putString(KEY_PATTERN_HASH, patternHash)
      putString(KEY_PIN_HASH, pinHash)
    }
  }

  fun verifyPattern(pattern: List<Int>): Boolean {
    val stored = prefs.getString(KEY_PATTERN_HASH, null) ?: return false
    val candidate = sha256("pattern:${pattern.joinToString("-")}")
    return stored == candidate
  }

  fun verifyPin(pin: String): Boolean {
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

  private companion object {
    private const val KEY_PATTERN_HASH = "pattern_hash"
    private const val KEY_PIN_HASH = "pin_hash"
  }
}
