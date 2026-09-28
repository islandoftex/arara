// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.time

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * A date and time representation.
 */
public class DateTime {
    /**
     * The year of the date.
     */
    public val year: Int
    /**
     * The month of the year, from 1 (January) to 12 (December).
     */
    public val month: Int
    /**
     * The day of the month, from 1 to 31.
     */
    public val day: Int
    /**
     * The hour of the day, from 0 to 23.
     */
    public val hour: Int
    /**
     * The minute of the hour, from 0 to 59.
     */
    public val minute: Int
    /**
     * The second of the minute, from 0 to 59.
     */
    public val second: Int

    private val timezone: ZoneId = ZoneId.systemDefault()

    /**
     * Constructor that builds the object from each part of a date (year,
     * month, and day) and time (hour, minute, second).
     */
    public constructor(
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
    public constructor(timestamp: Long) {
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

    /**
     * The point in time as a timestamp of the form
     * `year-month-day hour:minute:second`, with the numbers zero
     * padded.
     */
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
