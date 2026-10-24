// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import java.io.IOException
import java.util.concurrent.TimeoutException
import org.zeroturnaround.exec.InvalidExitValueException

/** A completed process or a failure to start or finish one. */
sealed class SystemCommandResult {
    /**
     * A process that exited, including unsuccessful exit codes.
     * @property exitCode The process exit status.
     * @property output Captured standard output and standard error.
     */
    data class Completed(val exitCode: Int, val output: String) : SystemCommandResult()
    /**
     * A process that could not start or finish normally.
     * @property exception Original backend failure.
     * @property output Output captured before the failure.
     */
    data class Failed(val exception: Exception, val output: String) : SystemCommandResult() {
        /** Backend-neutral classification of [exception]. */
        val kind: FailureKind
            get() = when (exception) {
                is IOException -> FailureKind.IO
                is TimeoutException -> FailureKind.TIMEOUT
                is InterruptedException -> FailureKind.INTERRUPTED
                is InvalidExitValueException -> FailureKind.INVALID_EXIT_VALUE
                else -> FailureKind.OTHER
            }
    }

    /** Classify backend exceptions without exposing the process library to callers. */
    enum class FailureKind { IO, TIMEOUT, INTERRUPTED, INVALID_EXIT_VALUE, OTHER }
}
