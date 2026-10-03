// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies the standard stream adaptation's decision table: the
 * signature of a Unix-style terminal claim mediating a Windows
 * pipe, and nothing else - every other platform, every console,
 * every claimless stream stays exactly as the JVM built it.
 */
class StandardStreamsTest {
    @Test
    fun `a windows pipe behind a terminal claim speaks utf8`() {
        assertTrue(
            StandardStreams.shouldSpeakUTF8("Windows 11", true, false),
            "the pty signature: a terminal claim on a Windows pipe"
        )
    }

    @Test
    fun `every other platform never adapts`() {
        assertFalse(
            StandardStreams.shouldSpeakUTF8("Linux", true, false),
            "Linux already speaks UTF-8 natively"
        )
        assertFalse(
            StandardStreams.shouldSpeakUTF8("Mac OS X", true, false),
            "the other Unix world speaks for itself"
        )
    }

    @Test
    fun `windows without a terminal claim stays native`() {
        assertFalse(
            StandardStreams.shouldSpeakUTF8("Windows 11", false, false),
            "cmd and PowerShell keep their legacy codepage"
        )
    }

    @Test
    fun `a windows console keeps the codepage it reads`() {
        assertFalse(
            StandardStreams.shouldSpeakUTF8("Windows 11", true, true),
            "a console-attached stream matches the console already"
        )
    }
}
