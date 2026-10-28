// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.smoke

import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath

internal fun SmokeContext.loggingCases(): Map<String, (MPPPath) -> Unit> =
    LoggingChecks(this).cases

private class LoggingChecks(context: SmokeFixture) : SmokeFixture by context {
    val cases: Map<String, (MPPPath) -> Unit> = linkedMapOf(
        "configured_logname_receives_messages_in_both_formats" to ::configuredLognameReceivesMessagesInBothFormats,
        "dialog_choices_in_both_engines_and_presentations" to ::dialogChoicesInBothEnginesAndPresentations,
        "plain_and_styled_presentations_keep_identical_diagnostic_logs" to
            ::plainAndStyledPresentationsKeepIdenticalDiagnosticLogs,
        "diagnostic_log_records_exact_rule_command_and_application_outcomes" to
            ::diagnosticLogRecordsExactRuleCommandAndApplicationOutcomes,
        "english_log_labels_preserve_the_localized_console" to ::englishLogLabelsPreserveTheLocalizedConsole,
        "session_mutations_from_both_engines_are_traced_without_values" to
            ::sessionMutationsFromBothEnginesAreTracedWithoutValues,
        "tool_diagnostics_are_filtered_but_styled_summaries_keep_warnings" to
            ::toolDiagnosticsAreFilteredButStyledSummariesKeepWarnings,
        "log_retention_and_levels_follow_the_invocation_outcome" to ::logRetentionAndLevelsFollowTheInvocationOutcome,
        "json_logging_is_available_in_the_packaged_jar" to ::jsonLoggingIsAvailableInThePackagedJar,
    )

    private fun configuredLognameReceivesMessagesInBothFormats(
        root: MPPPath
    ) {

        rule(root, "return true;")
        for (format in listOf("lua", "yaml")) {
            val name = "$format-diagnostics.log"
            root.resolve("arararc.$format").writeText(if (format == "yaml") {
                "!config\npaths: ['.']\nlogname: '$name'\n"
            } else "return { paths = { cwd }, log = { file = '$name' } }")
            execute(root, 0, listOf("--log-format", "text"))
            contains(root.resolve(name).readText(), "Run completed with exit code 0")
            Files.delete(root.resolve("arararc.$format").toJVMPath())
        }
    }

    private fun dialogChoicesInBothEnginesAndPresentations(
        root: MPPPath
    ) {

        rule(root, "return showOptions(4, 'Options', 'Pick one', 'Only') == 0 && " +
            "showList(4, 'List', 'Optional', 'Only') == 0;")
        luaRule(root, "return show_options(4, 'Options', 'Pick one', 'Only') == 0 and " +
            "show_list(4, 'List', 'Optional', 'Only') == 0")
        listOf("lua", "yaml").forEach { engine ->
            listOf("plain", "styled").forEach { console ->
                execute(root, 0, listOf("--preferred-engine", engine, "--console", console), streams =
                    SmokeStreams(input = "0\n0\n"))
            }
        }
    }

    private fun plainAndStyledPresentationsKeepIdenticalDiagnosticLogs(
        root: MPPPath
    ) {

        rule(root, "return true;")
        fun logFor(mode: String): String {
            execute(root, 0, listOf("--console", mode, "--log"))
            val text = root.resolve("arara.log").readText()
            contains(text, "console=$mode")
            return text.replace(Regex("console=(plain|styled)"), "console=CONSOLE")
                .replace(Regex("(?m)^.*? (TRACE|DEBUG|INFO|WARN|ERROR) +- "), "$1 - ")
                .replace(Regex("Elapsed time: [0-9.,]+ seconds"), "Elapsed time: TIME seconds")
        }
        check(logFor("plain") == logFor("styled")) { "Console mode changed the diagnostic log" }
    }

    private fun diagnosticLogRecordsExactRuleCommandAndApplicationOutcomes(
        root: MPPPath
    ) {

        rule(root, native("7"))
        execute(root, FAILURE_STATUS, listOf("--log"))
        val log = root.resolve("arara.log").readText()
        contains(log, "Rule file: \"${root.resolve("probe.yaml")}\" (engine: yaml)")
        contains(log, "working directory: $root")
        contains(log, "System command exited with value 7 after ")
        contains(log, "Run failed with exit code 7")
        rule(root, native("sleep"))
        execute(root, APPLICATION_ERROR, listOf("--log", "--timeout", "500"))
        val timeout = root.resolve("arara.log").readText()
        contains(timeout, "System command failed (TIMEOUT) after ")
        contains(timeout, "Run failed with exit code 2")
    }

    private fun englishLogLabelsPreserveTheLocalizedConsole(
        root: MPPPath
    ) {

        rule(root, "return true;")
        val output = execute(root, 0, listOf("--log", "--language", "de"))
        contains(output, "ERFOLGREICH")
        val log = root.resolve("arara.log").readText()
        contains(log, "Task result: SUCCESS")
        contains(log, "Run completed with exit code 0")
        contains(log, "Elapsed time: ")
    }

    private fun sessionMutationsFromBothEnginesAreTracedWithoutValues(
        root: MPPPath
    ) {

        rule(root, "s=getSession(); s.put('trace', 'HIDDEN_SESSION_VALUE'); " +
            "s.put('trace', 'SECOND_HIDDEN_VALUE'); s.remove('trace'); return true;")
        luaRule(root, "local s=get_session(); s.put('trace', 'HIDDEN_SESSION_VALUE'); " +
            "s.put('trace', 'SECOND_HIDDEN_VALUE'); s.remove('trace'); return true")
        for (engine in listOf("lua", "yaml")) {
            execute(root, 0, listOf("--log", "--preferred-engine", engine))
            val log = root.resolve("arara.log").readText()
            contains(log, "Session variable 'trace' set.")
            contains(log, "Session variable 'trace' overwritten.")
            contains(log, "Session variable 'trace' removed.")
            check("HIDDEN_SESSION_VALUE" !in log && "SECOND_HIDDEN_VALUE" !in log) {
                "Session mutation records included variable values"
            }
        }
    }

    private fun toolDiagnosticsAreFilteredButStyledSummariesKeepWarnings(
        root: MPPPath
    ) {

        rule(root, native("diagnostics"))
        luaRule(root, luaNative("diagnostics"))
        for (engine in listOf("lua", "yaml")) {
            val flags = listOf("--preferred-engine", engine, "--console", "styled",
                "--log-format", "json", "--log-level", "error")
            val output = execute(root, 0, flags)
            contains(output, "Tool diagnostics")
            contains(output, "native-warning")
            contains(output, "native-error")
            contains(output, "TOTAL SUCCESS")
            val events = root.resolve("arara.log").readLines().map { Json.parseToJsonElement(it).jsonObject }
            val diagnostics = events.filter { it["event"]?.jsonPrimitive?.content == "tool.diagnostic" }
            check(diagnostics.size == 1) { "Severity filtering lost or duplicated a diagnostic" }
            check(diagnostics.single().getValue("level").jsonPrimitive.content == "ERROR")
            execute(root, 0, flags + listOf("--log-format", "none"))
            val discarded = execute(root, 0, flags + listOf("--log-retention", "on-failure"))
            contains(discarded, "native-error")
            check(!root.resolve("arara.log").exists) { "Text diagnostics changed the successful run outcome" }
        }
    }

    private fun logRetentionAndLevelsFollowTheInvocationOutcome(
        root: MPPPath
    ) {

        rule(root, "return true;")
        val log = root.resolve("arara.log")
        val flags = listOf("--log-format", "json", "--log-retention", "on-failure", "--log-level", "error")
        execute(root, 0, flags)
        check(!log.exists) { "Successful invocation retained its log" }
        rule(root, "return false;")
        execute(root, 1, flags)
        val records = log.readLines().map { Json.parseToJsonElement(it).jsonObject }
        check(records.isNotEmpty()) { "Failed invocation lost its log" }
        check(records.all { it.getValue("level").jsonPrimitive.content == "ERROR" }) {
            "Log severity filter was not applied"
        }
        contains(log.readText(), "run.failed")
    }

    private fun jsonLoggingIsAvailableInThePackagedJar(
        root: MPPPath
    ) {

        rule(root, "return true;")
        execute(root, 0)
        check(!root.resolve("arara.log").exists) { "Default format created a log file" }
        val output = execute(root, 0, listOf("--log-format", "json"))
        contains(output, "SUCCESS")
        val records = root.resolve("arara.log").readLines().map { Json.parseToJsonElement(it).jsonObject }
        check(records.isNotEmpty()) { "JSON logging produced no events" }
        check(records.any { it["message"]?.jsonPrimitive?.content == "Run completed with exit code 0" }) {
            "JSON log has no closing run record"
        }
        root.resolve("arararc.lua").writeText(
            "return { paths = { cwd }, log = { " +
                "file = cwd .. '/configured.jsonl', format = 'json' } }")
        execute(root, 0)
        root.resolve("configured.jsonl").readLines().forEach { Json.parseToJsonElement(it).jsonObject }
        val previous = root.resolve("configured.jsonl").readText()
        execute(root, 0, listOf("-l", "--log-format", "none"))
        check(root.resolve("configured.jsonl").readText() == previous) { "NONE did not override logging" }
        execute(root, 0, listOf("-l"))
        check(!root.resolve("configured.jsonl").readText().startsWith("{")) { "Text shorthand was ignored" }
        execute(root, 0, listOf("--log", "--log-format", "json"))
        root.resolve("configured.jsonl").readLines().forEach { Json.parseToJsonElement(it).jsonObject }
    }

}
