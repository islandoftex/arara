// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.time

/**
 * Platform-independent object to represent a date and time.
 */
public expect class DateTime {

    /**
     * Constructor that builds the object from each part of a date (year,
     * month, and day) and time (hour, minute, second).
     */
    // detekt does not identify expect / actual classes yet
    @Suppress("unused")
    public constructor(
            year: Int,
            month: Int,
            day: Int,
            hour: Int,
            minute: Int,
            second: Int,
    )

    /**
     * Constructor that builds the object from a long timestamp.
     */
    // detekt does not identify expect / actual classes yet
    @Suppress("unused")
    public constructor(timestamp: Long)
}
