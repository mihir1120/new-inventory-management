package com.ashokvatika.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteBooleanValueTest {
  @Test fun acceptsValuesWrittenByDifferentClients() {
    listOf(true, 1L, "true", "YES", "1").forEach { assertEquals(true, parseRemoteBoolean(it)) }
    listOf(false, 0L, "false", "NO", "0").forEach { assertEquals(false, parseRemoteBoolean(it)) }
  }

  @Test fun rejectsMissingOrMalformedValuesWithoutThrowing() {
    listOf(null, "unknown", mapOf("value" to true), listOf(true)).forEach {
      assertNull(parseRemoteBoolean(it))
    }
  }
}
