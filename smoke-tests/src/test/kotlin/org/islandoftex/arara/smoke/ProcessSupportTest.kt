// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import org.islandoftex.arara.api.files.MPPPath
import java.io.StringReader
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProcessSupportTest {
    private fun command(mode: String): List<String> {
        val executable = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
        val classpath = listOf(ProcessFixture::class.java, Unit::class.java).map {
            Path.of(it.protectionDomain.codeSource.location.toURI()).toString()
        }.distinct().joinToString(System.getProperty("path.separator"))
        return listOf(MPPPath(System.getProperty("java.home")).resolve("bin/$executable").toString(),
            "-cp", classpath, ProcessFixture::class.java.name, mode)
    }

    @Test
    fun capturesBothStreamsAndNativeExitStatus() = temporary { root ->
        val result = run(command("failure"), root)
        assertEquals(7, result.status)
        assertTrue("OUTPUT" in result.output)
        assertTrue("ERROR" in result.output)
    }

    @Test
    fun timeoutReportsOutputAndTerminatesTheChild() = temporary { root ->
        val failure = assertFailsWith<IllegalStateException> {
            run(command("sleep"), root, timeoutSeconds = 1)
        }
        assertTrue("Process timed out" in failure.message.orEmpty())
        assertTrue("OUTPUT" in failure.message.orEmpty())
        val pid = Regex("PID=(\\d+)").find(failure.message.orEmpty())!!.groupValues[1].toLong()
        assertTrue(ProcessHandle.of(pid).map { it.onExit().get(5, java.util.concurrent.TimeUnit.SECONDS); !it.isAlive }.orElse(true))
    }

    @Test
    fun reportsFailuresAsEscapedJunitXml() = temporary { root ->
        val destination = root.resolve("reports/results.xml")
        report(destination, listOf(CaseResult("pass", null),
            CaseResult("failure < &", IllegalStateException("bad < & \u0001"))), "arara.test")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(InputSource(StringReader(destination.readText())))
        assertEquals("2", document.documentElement.getAttribute("tests"))
        assertEquals("1", document.documentElement.getAttribute("failures"))
        assertEquals(1, document.getElementsByTagName("failure").length)
        assertEquals("arara.test", document.getElementsByTagName("testcase").item(0)
            .attributes.getNamedItem("classname").nodeValue)
    }

    @Test
    fun temporaryFixturesAreRemovedOnFailure() {
        var directory: MPPPath? = null
        assertFailsWith<IllegalStateException> {
            temporary { root -> directory = root; root.resolve("file").writeText("fixture"); error("failure") }
        }
        assertFalse(directory!!.exists)
    }
}

object ProcessFixture {
    @JvmStatic
    fun main(args: Array<String>) {
        println("OUTPUT PID=${ProcessHandle.current().pid()}")
        System.err.println("ERROR")
        if (args.single() == "sleep") Thread.sleep(60000)
        kotlin.system.exitProcess(7)
    }
}
