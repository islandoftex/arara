// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import kotlin.time.TimeSource
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.cli.diagnostics.ToolOutputAnalysis
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.RunLog
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.SystemCommandResult

/**
 * Runs system commands, logs captured output, and maps process failures to localized errors.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
internal object CommandRunner {
    /**
     * Runs the command in the underlying operating system.
     *
     * @param command An object representing the command.
     * @return An integer value representing the exit code.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    internal fun run(command: Command): Int = runResult(command).exitCode

    internal fun runResult(command: Command, rule: String? = null): SystemCommandResult.Completed {
        val start = TimeSource.Monotonic.markNow()
        val result = Environment.executeSystemCommandResult(
            command,
            !LinearExecutor.executionOptions.verbose,
            LinearExecutor.executionOptions.timeoutValue
        )
        val milliseconds = start.elapsedNow().inWholeMilliseconds
        return result.let {
            ToolOutputAnalysis.record(command, rule, when (it) {
                is SystemCommandResult.Completed -> it.output
                is SystemCommandResult.Failed -> it.output
            })
            if (it is SystemCommandResult.Failed) {
                RunLog.commandFailed(command, it.kind.name, milliseconds, it.exception)
                DisplayUtils.renderer.printCommandFailure(it.output)
                throw AraraException(
                    LanguageController.messages.run {
                        when (it.kind) {
                            SystemCommandResult.FailureKind.IO -> errorRunIoException
                            SystemCommandResult.FailureKind.TIMEOUT -> errorRunTimeoutException
                            SystemCommandResult.FailureKind.INTERRUPTED -> errorRunInterruptedException
                            SystemCommandResult.FailureKind.INVALID_EXIT_VALUE -> errorRunInvalidExitValueException
                            SystemCommandResult.FailureKind.OTHER -> errorRunGenericException
                        }
                    },
                    it.exception
                )
            }
            it as SystemCommandResult.Completed
            RunLog.commandCompleted(command, it.exitCode, milliseconds)
            it
        }
    }
}
