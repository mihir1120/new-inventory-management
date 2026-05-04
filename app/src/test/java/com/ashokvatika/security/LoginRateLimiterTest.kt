package com.ashokvatika.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginRateLimiterTest {
  @Test
  fun allowsAttemptsBeforeThreshold() {
    var now = 1_000L
    val limiter = LoginRateLimiter(
      maxAttemptsBeforeLockout = 3,
      lockoutDurationMs = 10_000L,
      clock = { now },
    )

    assertTrue(limiter.beforeAttempt().allowed)
    assertTrue(limiter.recordFailure().allowed)
    assertTrue(limiter.beforeAttempt().allowed)
    assertTrue(limiter.recordFailure().allowed)
  }

  @Test
  fun locksAfterThresholdAndReopensAfterCooldown() {
    var now = 10_000L
    val limiter = LoginRateLimiter(
      maxAttemptsBeforeLockout = 2,
      lockoutDurationMs = 5_000L,
      clock = { now },
    )

    assertTrue(limiter.recordFailure().allowed)
    val gate = limiter.recordFailure()
    assertFalse(gate.allowed)
    assertEquals(5L, gate.waitSeconds)

    val blocked = limiter.beforeAttempt()
    assertFalse(blocked.allowed)
    assertEquals(5L, blocked.waitSeconds)

    now += 5_100L
    assertTrue(limiter.beforeAttempt().allowed)
  }

  @Test
  fun resetsStateAfterSuccessfulLogin() {
    var now = 5_000L
    val limiter = LoginRateLimiter(
      maxAttemptsBeforeLockout = 2,
      lockoutDurationMs = 20_000L,
      clock = { now },
    )

    limiter.recordFailure()
    limiter.recordSuccess()

    assertTrue(limiter.beforeAttempt().allowed)
    val gate = limiter.recordFailure()
    assertTrue(gate.allowed)
  }
}
