// SPDX-License-Identifier: BSD-3-Clause
import org.islandoftex.arara.api.configuration.LogLevel
import org.islandoftex.arara.api.files.ForeignPath
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.PathSyntax
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.diagnostics.ToolOutputAnalyzer
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.reporting.RunLedger
import org.islandoftex.arara.core.rules.RuleInventory
import org.islandoftex.arara.core.rules.RuleLookup
import org.islandoftex.arara.core.session.Command

fun main() {
    verifyApiContracts()
    verifyCoreServices()
    val path = MPPPath(".").resolve("publication-check")
    check(path.toOkioPath().isAbsolute)
    check(ForeignPath.parse("C:/book/../chapter.tex", PathSyntax.WINDOWS).normalize().fileName == "chapter.tex")
    val options = ExecutionOptions()
    check(path.toString().isNotEmpty() && !options.verbose) {
        "Published library defaults are not usable"
    }
    println("Published core and transitive API are usable from Kotlin on Java 21.")
}

@OptIn(kotlin.time.ExperimentalTime::class)
private fun verifyCoreServices() {
    val directory = MPPPath(".").normalize()
    val file = ProjectFile(directory.resolve("book.tex"), FileType("tex", "^%"))
    val ledger = RunLedger().apply {
        prepare(listOf(Project("book", directory, setOf(file))))
        start(file, false)
        task()
        ToolOutputAnalyzer.analyze(Command(listOf("pdflatex")), "LaTeX Warning: Rerun.").forEach(::diagnostic)
        finish(0)
    }
    check(ledger.summary(true, false).files.single().diagnostics.single().level == LogLevel.WARN)
    check(RuleLookup.candidates("probe", setOf(directory), directory, RuleEngine.LUA).first().fileName == "probe.lua")
    check(RuleInventory.discover(ExecutionOptions(rulePaths = emptySet()), directory, RuleEngine.LUA).rules.isEmpty())
    listOf("cli.CLIKt", "lua.LuaInterpreter", "mvel.utils.MvelState").forEach { name ->
        check(runCatching { Class.forName("org.islandoftex.arara.$name", false, Thread.currentThread().contextClassLoader) }.isFailure)
    }
}
