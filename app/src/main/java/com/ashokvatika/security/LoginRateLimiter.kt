package com.ashokvatika.security

class LoginRateLimiter(
  private val maxAttemptsBeforeLockout: Int = 5,
  private val lockoutDurationMs: Long = 30_000L,
  private val clock: () -> Long = System::currentTimeMillis,
) {
  private var failedAttempts = 0
  private var lockoutEndsAtMs = 0L

  fun beforeAttempt(): AttemptGate {
    val remainingMs = lockoutEndsAtMs - clock()
    return if (remainingMs > 0L) {
      AttemptGate(allowed = false, waitSeconds = remainingMs.toRoundedUpSeconds())
    } else {
      AttemptGate(allowed = true, waitSeconds = 0)
    }
  }

  fun recordFailure(): AttemptGate {
    failedAttempts += 1
    if (failedAttempts < maxAttemptsBeforeLockout) {
      return AttemptGate(allowed = true, waitSeconds = 0)
    }
    lockoutEndsAtMs = clock() + lockoutDurationMs
    failedAttempts = 0
    return AttemptGate(allowed = false, waitSeconds = lockoutDurationMs.toRoundedUpSeconds())
  }

  fun recordSuccess() {
    failedAttempts = 0
    lockoutEndsAtMs = 0L
  }
}

data class AttemptGate(
  val allowed: Boolean,
  val waitSeconds: Long,
)

private fun Long.toRoundedUpSeconds(): Long {
  return ((this + 999L) / 1000L).coerceAtLeast(1L)
}
