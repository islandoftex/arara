// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies the standard stream adaptation's decision table: the
 * signature of a Unix-style terminal claim mediating a Windows
 * stream, pipe or console alike, and nothing else - every other
 * platform, every claimless stream stays exactly as the JVM
 * built it. The console's borrow-and-return lives behind the
 * decision, guarded by the platform's own answers.
 */
class StandardStreamsTest {
    @Test
    fun `a windows stream behind a terminal claim speaks utf8`() {
        assertTrue(
            StandardStreams.shouldSpeakUTF8("Windows 11", true),
            "the terminal claim: a pipe or a console alike"
        )
    }

    @Test
    fun `every other platform never adapts`() {
        assertFalse(
            StandardStreams.shouldSpeakUTF8("Linux", true),
            "Linux already speaks UTF-8 natively"
        )
        assertFalse(
            StandardStreams.shouldSpeakUTF8("Mac OS X", true),
            "the other Unix world speaks for itself"
        )
    }

    @Test
    fun `windows without a terminal claim stays native`() {
        assertFalse(
            StandardStreams.shouldSpeakUTF8("Windows 11", false),
            "cmd and PowerShell keep their legacy codepage"
        )
    }

    @Test
    fun `the adaptation never throws`() {
        // the adaptation is the startup's first breath: its own
        // refusal path must be silent on every platform, and
        // this suite runs where the signature never holds, so
        // the call returns having touched nothing
        StandardStreams.adaptForTerminal()
    }
}
