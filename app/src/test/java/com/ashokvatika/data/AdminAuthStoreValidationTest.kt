package com.ashokvatika.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdminAuthStoreValidationTest {
  @Test
  fun rejectsShortPins() {
    assertEquals("Use at least 6 digits.", AdminAuthStore.validatePin("12345"))
  }

  @Test
  fun rejectsRepeatedDigits() {
    assertEquals("Choose a PIN that is not repeated digits.", AdminAuthStore.validatePin("111111"))
  }

  @Test
  fun rejectsSimpleSequences() {
    assertEquals("Choose a PIN that is not a simple sequence.", AdminAuthStore.validatePin("123456"))
    assertEquals("Choose a PIN that is not a simple sequence.", AdminAuthStore.validatePin("654321"))
  }

  @Test
  fun acceptsStrongerPins() {
    assertNull(AdminAuthStore.validatePin("482951"))
  }
}
