// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.time

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Platform-independent object to represent a date and time.
 */
public actual class DateTime {
    public val year: Int
    public val month: Int
    public val day: Int
    public val hour: Int
    public val minute: Int
    public val second: Int

    private val timezone: ZoneId = ZoneId.systemDefault()

    /**
     * Constructor that builds the object from each part of a date (year,
     * month, and day) and time (hour, minute, second).
     */
    public actual constructor(
            year: Int,
            month: Int,
            day: Int,
            hour: Int,
            minute: Int,
            second: Int
    ) {
        this.year = year
        this.month = month
        this.day = day
        this.hour = hour
        this.minute = minute
        this.second = second
    }

    /**
     * Constructor that builds the object from a long timestamp.
     */
    public actual constructor(timestamp: Long) {
        Instant.ofEpochMilli(timestamp)
                .atZone(timezone).toLocalDateTime().let {
            year = it.year
            month = it.monthValue
            day = it.dayOfMonth
            hour = it.hour
            minute = it.minute
            second = it.second
        }
    }

    override fun toString(): String = "%d-%02d-%02d %02d:%02d:%02d"
            .format(year, month, day, hour, minute, second)

    /**
     * Convert the [DateTime] object to a [Long] representation considering
     * the provided [ZoneId].
     */
    public fun toLong(timezone: ZoneId): Long =
            LocalDateTime.of(year, month, day, hour, minute, second)
                    .atZone(timezone).toInstant().toEpochMilli()

    /**
     * Convert the [DateTime] object to a [Long] representation considering
     * the system's default [ZoneId].
     */
    public fun toLong(): Long = toLong(timezone)
}
