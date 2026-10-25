// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.core.session.Environment

/** Records effective settings after project configuration and command-line overrides. */
internal object RuntimeDiagnostics {
    fun record(project: Project, preferredEngine: String, console: String, configurationFile: String?) {
        if (!EventLog.isInfoEnabled) return
        RunLog.project(project.name, project.workingDirectory.toString())
        val fields = buildMap<String, Any> {
            put("project", project.name)
            put("workingDirectory", project.workingDirectory.toString())
            put("invocationDirectory", MPPPath(".").toString())
            put("applicationPath", RunLog.applicationPath)
            put("preferredEngine", preferredEngine)
            put("console", console)
            configurationFile?.let { put("configurationFile", it) }
            for ((field, property) in listOf(
                "javaVersion" to "java.version", "javaVendor" to "java.vendor", "javaHome" to "java.home",
                "osName" to "os.name", "osArch" to "os.arch", "osVersion" to "os.version"
            )) {
                Environment.getSystemPropertyOrNull(property)?.let { put(field, it) }
            }
        }
        val message = "Runtime context: " + fields.entries.joinToString { "${it.key}=${it.value}" }
        EventLog.info("run.context", message, fields)
    }
}
