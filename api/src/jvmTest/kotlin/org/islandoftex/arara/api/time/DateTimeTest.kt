// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.time

import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeTest {
    @Test
    fun `correctly parse date to string`() {
        assertEquals("2025-08-01 17:46:01", DateTime(2025, 8, 1, 17, 46, 1).toString())
    }

    @Test
    fun `correctly parse date to long`() {
        assertEquals(1754017751000L, DateTime(2025, 8, 1, 3, 9, 11).toLong(ZoneId.of("UTC")))
    }

    @Test
    fun `correctly use system's default timezone`() {
        val date = DateTime(2025, 8, 1, 3, 9, 11)
        assertEquals(date.toLong(ZoneId.systemDefault()), date.toLong())
    }

    @Test
    fun `correctly get every date component`() {
        with(DateTime(2025, 8, 1, 3, 9, 11)) {
            assertEquals(2025, year)
            assertEquals(8, month)
            assertEquals(1, day)
            assertEquals(3, hour)
            assertEquals(9, minute)
            assertEquals(11, second)
        }
    }
}
