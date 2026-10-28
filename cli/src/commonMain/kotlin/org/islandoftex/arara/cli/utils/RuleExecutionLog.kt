// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.core.session.LinearExecutor

/** Diagnostic events for rule selection, tasks and directive control flow. */
interface RuleExecutionLog {
    /** Record a task starting within a rule. */
    fun entry(name: String, task: String)

    /** Record the task success flag. */
    fun taskResult(success: Boolean)

    /** Record the resolved rule path and engine. */
    fun rule(identifier: String, path: String, engine: String)

    /** Record each resolved legacy rule, including skipped directives and dry runs. */
    fun deprecatedYaml(identifier: String, path: String)

    /** Record a directive whose initial condition prevented command evaluation. */
    fun skipped(identifier: String, conditional: DirectiveConditional, engine: String)

    /** Record the effective halt request made while evaluating a directive, including status zero. */
    fun haltRequested(identifier: String, code: Int, engine: String)
}

private object DefaultRuleExecutionLog : RuleExecutionLog {
    /** Record a task starting within a rule. */
    override fun entry(name: String, task: String) = EventLog.info(
        "task.started", "Interpreting task \"$task\" from rule \"$name\"", mapOf("rule" to name, "task" to task)
    )

    /** Record the task success flag. */
    override fun taskResult(success: Boolean) = EventLog.info(
        "task.result", "Task result: " + if (success) "SUCCESS" else "FAILURE", mapOf("success" to success)
    )

    /** Record the resolved rule path and engine. */
    override fun rule(identifier: String, path: String, engine: String) = EventLog.info(
        "rule.resolved", "Rule file: \"$path\" (engine: $engine)",
        mapOf("rule" to identifier, "path" to path, "engine" to engine)
    )

    /** Record each resolved legacy rule, including skipped directives and dry runs. */
    override fun deprecatedYaml(identifier: String, path: String) = EventLog.warn(
        "rule.deprecated", "Deprecated YAML rule: $identifier ($path); YAML rules will be discontinued in v9.",
        mapOf("rule" to identifier, "path" to path, "engine" to "yaml", "removalVersion" to "9")
    )

    /** Record a directive whose initial condition prevented command evaluation. */
    override fun skipped(identifier: String, conditional: DirectiveConditional, engine: String) {
        if (!EventLog.isInfoEnabled) return
        EventLog.info("directive.skipped", "Directive $identifier skipped as its condition did not hold",
            ruleFields(identifier, engine) + mapOf(
                "conditionalType" to conditional.type.name.lowercase(), "condition" to conditional.condition
            ))
    }

    /** Record the effective halt request made while evaluating a directive, including status zero. */
    override fun haltRequested(identifier: String, code: Int, engine: String) {
        if (!EventLog.isInfoEnabled) return
        EventLog.info("halt.requested", "Rule $identifier requested a file halt with exit code $code",
            ruleFields(identifier, engine) + mapOf("exitCode" to code))
    }

    private fun ruleFields(identifier: String, engine: String): Map<String, Any> = buildMap {
        put("rule", identifier)
        put("engine", engine)
        LinearExecutor.currentFile?.let { put("inputFile", it.path.toString()) }
    }

}

internal val ruleExecutionLog: RuleExecutionLog get() = DefaultRuleExecutionLog
