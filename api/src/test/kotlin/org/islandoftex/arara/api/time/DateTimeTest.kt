// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.time

import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the [DateTime] model: the string and epoch representations
 * and the individual date components.
 */
class DateTimeTest {
    @Test
    fun `formats the date to its string representation`() {
        assertEquals(
            "2025-08-01 17:46:01",
            DateTime(2025, 8, 1, 17, 46, 1).toString(),
            "string representation of the date"
        )
    }

    @Test
    fun `converts the date to its epoch representation`() {
        assertEquals(
            1754017751000L,
            DateTime(2025, 8, 1, 3, 9, 11).toLong(ZoneId.of("UTC")),
            "epoch millisecond representation of the date in UTC"
        )
    }

    @Test
    fun `correctly uses the system default timezone`() {
        assertEquals(
            DateTime(2025, 8, 1, 3, 9, 11).toLong(ZoneId.systemDefault()),
            DateTime(2025, 8, 1, 3, 9, 11).toLong(),
            "epoch milliseconds when relying on the default timezone"
        )
    }

    @Test
    fun `correctly gets every date component`() {
        val dateTime = DateTime(2025, 8, 1, 3, 9, 11)
        assertEquals(2025, dateTime.year, "year component")
        assertEquals(8, dateTime.month, "month component")
        assertEquals(1, dateTime.day, "day component")
        assertEquals(3, dateTime.hour, "hour component")
        assertEquals(9, dateTime.minute, "minute component")
        assertEquals(11, dateTime.second, "second component")
    }
}
