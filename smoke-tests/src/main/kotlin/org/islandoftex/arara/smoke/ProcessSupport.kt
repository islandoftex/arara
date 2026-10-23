// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import kotlin.concurrent.thread
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMFile
import org.islandoftex.arara.api.files.toJVMPath
import org.islandoftex.arara.api.files.toMPPPath

private const val PROCESS_TIMEOUT_SECONDS = 30L
private const val READER_TIMEOUT_MILLIS = 5000L

internal data class Result(val status: Int, val output: String)
internal data class CaseResult(val name: String, val failure: Throwable?)

/** Checks use MPPPath; ProcessBuilder requires a File at its working-directory boundary. */
internal fun run(command: List<String>, directory: MPPPath, input: String = "",
                environment: Map<String, String?> = emptyMap(),
                timeoutSeconds: Long = PROCESS_TIMEOUT_SECONDS): Result {
    val process = ProcessBuilder(command).directory(directory.toJVMFile()).redirectErrorStream(true)
        .apply {
            environment.forEach { (key, value) ->
                if (value == null) environment().remove(key) else environment()[key] = value
            }
        }.start()
    val output = ByteArrayOutputStream()
    val reader = thread(isDaemon = true, name = "smoke-output") {
        process.inputStream.use { it.copyTo(output) }
    }
    try {
        process.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(input) }
        check(process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            "Process timed out: $command\n${output.toString(Charsets.UTF_8)}"
        }
        reader.join(READER_TIMEOUT_MILLIS)
        check(!reader.isAlive) { "Process output did not close: $command" }
        return Result(process.exitValue(), output.toString(Charsets.UTF_8))
    } finally {
        process.descendants().use { children -> children.forEach { it.destroyForcibly() } }
        if (process.isAlive) process.destroyForcibly()
        reader.join(READER_TIMEOUT_MILLIS)
    }
}

private fun XMLStreamWriter.writeCase(case: CaseResult, suiteName: String) {
    writeStartElement("testcase")
    writeAttribute("classname", suiteName)
    writeAttribute("name", case.name)
    case.failure?.let { failure ->
        writeStartElement("failure")
        writeCharacters(failure.stackTraceToString().filter { it >= ' ' || it in "\n\r\t" })
        writeEndElement()
    }
    writeEndElement()
}

internal fun report(destination: MPPPath, cases: List<CaseResult>, suiteName: String = "arara.process-smoke") {
    destination.parent.createDirectories()
    Files.newOutputStream(destination.toJVMPath()).use { stream ->
        val xml = XMLOutputFactory.newFactory().createXMLStreamWriter(stream, "UTF-8")
        try {
            xml.writeStartDocument("UTF-8", "1.0")
            xml.writeStartElement("testsuite")
            xml.writeAttribute("name", suiteName)
            xml.writeAttribute("tests", cases.size.toString())
            xml.writeAttribute("failures", cases.count { it.failure != null }.toString())
            xml.writeAttribute("errors", "0")
            cases.forEach { xml.writeCase(it, suiteName) }
            xml.writeEndElement()
            xml.writeEndDocument()
        } finally {
            xml.close()
        }
    }
}

internal fun temporary(body: (MPPPath) -> Unit) {
    val root = Files.createTempDirectory("arara smoke ").toRealPath().toMPPPath()
    try { body(root) } finally { root.removeDirectory() }
}
