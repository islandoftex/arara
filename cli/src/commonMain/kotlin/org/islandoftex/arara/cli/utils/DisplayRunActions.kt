// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import kotlin.time.Duration.Companion.seconds
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.ui.DisplayRunActions
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor

private object DefaultDisplayRunActions : DisplayRunActions {
    private val renderer get() = DisplayUtils.renderer
    private val presentation get() = DisplayUtils.presentation

    /** Finish the current file in the run ledger; the final summary renders its status. */
    override fun printFileResult(exitCode: Int) = presentation.finish(exitCode)

    /** Present the retained invocation ledger with the aggregate success flag. */
    override fun printRunSummary(success: Boolean) {
        renderer.printRunSummary(presentation.summary(
            success, LinearExecutor.executionOptions.executionMode == ExecutionMode.DRY_RUN
        ))
        if (presentation.deprecatedYamlUsed) renderer.printWrapped(LanguageController.messages.warningDeprecatedYaml)
    }

    /** Retain compatibility warnings across files and project configuration changes. */
    override fun deprecatedYamlUsed() = presentation.deprecatedYaml()

    /** Count a conditional skip and present it through the active renderer. */
    override fun printSkippedDirective(identifier: String) {
        presentation.skip()
        renderer.printSkippedDirective(identifier)
    }

    /** Start file accounting and present its metadata. */
    override fun printFileInformation(file: ProjectFile) {
        presentation.start(file, LinearExecutor.executionOptions.executionMode == ExecutionMode.DRY_RUN)
        renderer.printFileInformation(file)
    }

    /** Record an application error, finish file accounting with status 2 and present cause details. */
    override fun printException(exception: AraraException) {
        presentation.finish(2)
        val text = (exception.message ?: "EXCEPTION PROVIDES NO MESSAGE") +
            if (exception.hasException()) " " + LanguageController.messages.infoDisplayExceptionMoreDetails else ""
        val details = exception.exception?.let { it.message ?: it.toString() }
        RunLog.exception(exception.message, details, exception.exception ?: exception)
        renderer.printException(text, details)
    }

    /** Log and present elapsed time in seconds. */
    override fun printTime(seconds: Double) {
        RunLog.executionTime(seconds)
        renderer.printTime(seconds)
    }

}

internal val displayRunActions: DisplayRunActions get() = DefaultDisplayRunActions
