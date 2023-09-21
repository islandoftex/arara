// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.session

import com.soywiz.korio.async.async
import com.soywiz.korio.async.runBlockingNoJs
import com.soywiz.korio.file.baseName
import com.soywiz.korio.file.std.localVfs
import com.soywiz.korio.lang.Environment
import com.soywiz.korio.util.OS
import kotlinx.coroutines.awaitAll
import mu.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMFile
import org.islandoftex.arara.api.session.Command
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer
import org.zeroturnaround.exec.stream.TeeOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

/**
 * An object to handle interaction with the operating system.
 */
object Environment {
    // get the logger context from a factory
    private val logger = KotlinLogging.logger { }

    /**
     * Gets the system property according to the provided key, or resort to the
     * fallback value if an exception is thrown or if the key is invalid.
     *
     * @param key The system property key.
     * @param fallback The fallback value.
     * @return A string containing the system property value or the fallback.
     */
    @JvmStatic
    fun getSystemProperty(key: String, fallback: String): String =
        System.getProperties().runCatching {
            getOrDefault(key, fallback).toString().takeIf { it != "" }
        }.getOrNull() ?: fallback

    /**
     * Access a system property.
     *
     * @param key The key of the property.
     * @return The value of the system property or null if there is an
     *   exception.
     */
    @JvmStatic
    fun getSystemPropertyOrNull(key: String): String? =
        System.getProperties().runCatching { getValue(key).toString() }
            .getOrNull()

    /**
     * Determine whether arara is executed in a Cygwin environment (lazily to
     * save expensive system calls).
     *
     * The test executes a new system call to `uname -s` and determines by its
     * output whether we are running a Cygwin kernel.
     */
    private val inCygwinEnvironment by lazy {
        executeSystemCommand(
            // execute the Cygwin detection; we do not specify a working
            // directory because for the test it is irrelevant
            Command(listOf("uname", "-s")),
        ).second.lowercase().startsWith("cygwin")
    }

    /**
     * The range of supported operating systems by the OS detection
     * function [checkOS].
     */
    enum class SupportedOS {
        WINDOWS,
        LINUX,
        MACOS,
        UNIX,
        CYGWIN,
    }

    /**
     * Checks if the provided operating system string holds according to the
     * underlying operating system.
     *
     * Supported operating systems:
     *
     *   * Windows
     *   * Linux
     *   * Mac OS X
     *   * Unix (Linux || Mac OS)
     *   * Cygwin
     *
     * @param value A string representing an operating system.
     * @return A boolean value indicating if the provided string refers to the
     * underlying operating system.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @JvmStatic
    @Throws(AraraException::class)
    fun checkOS(value: SupportedOS): Boolean =
        when (value) {
            SupportedOS.WINDOWS -> OS.isWindows
            SupportedOS.LINUX -> OS.isLinux
            SupportedOS.MACOS -> OS.isMac
            SupportedOS.UNIX -> OS.isMac || OS.isLinux
            SupportedOS.CYGWIN -> inCygwinEnvironment
        }

    /**
     * Generates a list of filenames from the provided command based on a list
     * of extensions for each underlying operating system.
     *
     * @param command A string representing the command.
     * @return A list of filenames.
     */
    private fun appendExtensions(command: String): List<String> {
        // list of extensions, specific for
        // each operating system (in fact, it
        // is more Windows specific)
        val extensions = if (checkOS(SupportedOS.WINDOWS)) {
            // the application is running on
            // Windows, so let's look for the
            // following extensions in order

            // this list is actually a sublist from
            // the original Windows PATHEXT environment
            // variable which holds the list of executable
            // extensions that Windows supports
            listOf(".com", ".exe", ".bat", ".cmd")
        } else {
            // no Windows, so the default
            // extension will be just an
            // empty string
            listOf("")
        }

        // return the resulting list holding the
        // filenames generated from the
        // provided command
        return extensions.map { "$command$it" }
    }

    /**
     * Checks if the provided command name is reachable from the system path.
     *
     * @param command A string representing the command.
     * @return A logic value.
     */
    @JvmStatic
    fun isOnPath(command: String): Boolean {
        // first and foremost, let's build the list
        // of filenames based on the underlying
        // operating system
        val filenames = appendExtensions(command)
        return kotlin.runCatching {
            runBlockingNoJs {
                // break the path into several parts
                // based on the path separator symbol
                (Environment["PATH"] ?: Environment["Path"])
                    ?.split(File.pathSeparator)
                    ?.map { async { localVfs(it).listSimple() } }
                    ?.awaitAll()
                    // if the search does not return an empty
                    // list, one of the filenames got a match,
                    // and the command is available somewhere
                    // in the system path
                    ?.firstOrNull {
                        it.any { file ->
                            filenames.contains(file.baseName) &&
                                !file.isDirectory()
                        }
                    }?.let { true }
            }
        }.getOrNull() ?: false
        // otherwise (and in case of an exception) it is not in the path
    }

    /**
     * When executing a system call goes wrong, this status code is returned.
     */
    const val errorExitStatus = -99

    /**
     * Executes a system command from the underlying operating system and
     * returns a pair containing the exit status and the command output as a
     * string.
     *
     * @param command The system command to be executed.
     * @param silenceSystemOut If true, the system output will not be appended
     *   to. Otherwise, a split is performed between [System.out] and an
     *   internal capture buffer.
     * @param timeout An optional timeout. Non-zero values will be applied to
     *   the executor.
     * @return A pair containing the exit status and the system command output
     *   as a string. In case of an error, return a pair of [errorExitStatus]
     *   and the string representation of the throwable.
     */
    @JvmStatic
    fun executeSystemCommand(
        command: Command,
        silenceSystemOut: Boolean = true,
        timeout: Duration = Duration.ZERO,
    ): Pair<Int, String> = ByteArrayOutputStream().use { buffer ->
        ProcessExecutor(command.elements).runCatching {
            addDestroyer(ShutdownHookProcessDestroyer())

            // use the command's working directory as the preferred working
            // directory; although it may be missing in which case we will use
            // arara's execution directory by default
            val workingDirectory = command.workingDirectory
                ?: MPPPath(".")
            directory(workingDirectory.toJVMFile())

            // implement output redirection if necessary for verbose
            // output
            val tee = if (silenceSystemOut) {
                buffer
            } else {
                redirectInput(System.`in`)
                TeeOutputStream(System.out, buffer)
            }
            redirectOutput(tee).redirectError(tee)

            // add non-zero timeout to the executor to restrict runtime
            if (timeout != Duration.ZERO) {
                timeout(timeout.inWholeNanoseconds, TimeUnit.NANOSECONDS)
            }

            execute().exitValue to buffer.toString()
        }.getOrElse {
            logger.debug {
                "Caught an exception when executing " +
                    "$command returning $errorExitStatus"
            }
            errorExitStatus to "${it::class.java.name}: ${it.message}"
        }
    }
}
