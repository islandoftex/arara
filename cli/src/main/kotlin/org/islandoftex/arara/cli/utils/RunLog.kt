// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraAPI
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.core.configuration.DefaultConfiguration
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.Session

/**
 * Writes the run's log entries: the single home of every log line
 * the display side emits and of the block framing every log block
 * uses, kept deliberately free of console knowledge. The framing
 * follows the session width, never a presentation, so the file
 * reads the same whichever console rendered the run - and the log
 * blocks the interpreter, the command runner and the directive
 * processing build all frame through the same helpers.
 *
 * @author Island of TeX
 */
object RunLog {
    /**
     * The logger of the run log.
     */
    private val logger = KotlinLogging.logger { }

    /**
     * The line that frames a log block, following the session width
     * so no presentation can ever move it.
     */
    internal fun separator() =
        "-".repeat(Session.userInterfaceOptions.terminalOutputWidth)

    /**
     * The line that opens or closes a named log block, framing the
     * title in dashes at the session width.
     *
     * @param message The block title.
     */
    internal fun outputSeparator(message: String) =
        " $message ".center(
            Session.userInterfaceOptions.terminalOutputWidth,
            '-'
        )

    /**
     * Writes the welcome entry of the run.
     */
    fun welcome() = logger.info {
        "Welcome to arara ${AraraAPI.version}!" + "\n" + separator()
    }

    /**
     * Writes the environment entry of the run: the application, the
     * Java runtime, the operating system, the user home and the
     * configuration file in use.
     *
     * @param configurationFileName The configuration file name.
     */
    fun environment(configurationFileName: String) {
        val javaVersion = Environment.getSystemProperty(
            "java.version",
            "[unknown version]"
        )
        val javaVendor = Environment.getSystemProperty(
            "java.vendor",
            "[unknown vendor]"
        )
        val javaHome = Environment.getSystemProperty(
            "java.home",
            "[unknown location]"
        )
        val osName = Environment.getSystemProperty(
            "os.name",
            "[unknown OS name]"
        )
        val osArch = Environment.getSystemProperty(
            "os.arch",
            "[unknown OS arch]"
        )
        val osVersion = Environment.getSystemProperty(
            "os.version",
            "[unknown OS version]"
        )
        val userHome = Environment.getSystemProperty(
            "user.home",
            "[unknown user's home directory]"
        )
        logger.debug {
            "Environment details\n" +
                "::: arara @ ${DefaultConfiguration.applicationPath}\n" +
                "::: Java $javaVersion, $javaVendor\n" +
                "::: $javaHome\n" +
                "::: $osName, $osArch, $osVersion\n" +
                "::: user.home @ $userHome\n" +
                "::: CF @ $configurationFileName"
        }
    }

    /**
     * Writes the entry announcing the file the run processes, the
     * English twin of the localized console line.
     *
     * @param file The project file.
     */
    fun fileInformation(file: ProjectFile) = logger.info {
        "Processing \"${file.path.fileName}\" (size: " +
            "${byteSizeToString(file.path.fileSize)}, " +
            "last modified: ${file.path.lastModified}), " +
            "please wait."
    }

    /**
     * Writes the entry announcing an interpreted task.
     *
     * @param name The rule name.
     * @param task The task name.
     */
    fun entry(name: String, task: String) = logger.info {
        "I am ready to interpret task \"$task\" from rule \"$name\"."
    }

    /**
     * Writes the entry recording a task outcome.
     *
     * @param value The task outcome.
     */
    fun taskResult(value: Boolean) = logger.info {
        // a fixed English token keeps the outcome greppable across
        // every language and every future rewording of the labels
        "Task result: " + if (value) "SUCCESS" else "FAILURE"
    }

    /**
     * Writes the entry recording the elapsed time of the run. The
     * log speaks English regardless of the session locale, so the
     * format pins the root locale: its dot notation never bends to
     * the user's language.
     *
     * @param seconds The elapsed seconds.
     */
    fun executionTime(seconds: Double) = logger.info {
        "Elapsed time: " +
            String.format(java.util.Locale.ROOT, "%.4f", seconds) +
            " seconds"
    }

    /**
     * Writes the closing entry recording the verdict of the whole
     * run: the exit code the application returns, with the level
     * carrying the outcome.
     *
     * @param exitCode The exit code of the run.
     */
    fun executionOutcome(exitCode: Int) {
        if (exitCode == 0) {
            logger.info { "Run completed with exit code $exitCode" }
        } else {
            logger.error { "Run failed with exit code $exitCode" }
        }
    }

    /**
     * Writes the entries recording an exception: the bare message,
     * as thrown, and the underlying cause details when present. The
     * pointer to the details is console presentation, so only the
     * display carries it.
     *
     * @param message The exception message, possibly null.
     * @param details The underlying cause details, if any.
     */
    fun exception(message: String?, details: String?) {
        logger.error { message ?: "EXCEPTION PROVIDES NO MESSAGE" }
        if (details != null) {
            logger.error { details }
        }
    }
}
