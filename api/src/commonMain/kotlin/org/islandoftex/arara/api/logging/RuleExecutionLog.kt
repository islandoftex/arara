// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.logging

import org.islandoftex.arara.api.rules.DirectiveConditional

/** Diagnostic events for rule selection, tasks and directive control flow. */
public interface RuleExecutionLog {
    /** Record a task starting within a rule. */
    public fun entry(name: String, task: String)

    /** Record the task success flag. */
    public fun taskResult(success: Boolean)

    /** Record the resolved rule path and engine. */
    public fun rule(identifier: String, path: String, engine: String)

    /** Record each resolved legacy rule, including skipped directives and dry runs. */
    public fun deprecatedYaml(identifier: String, path: String)

    /** Record a directive whose initial condition prevented command evaluation. */
    public fun skipped(identifier: String, conditional: DirectiveConditional, engine: String)

    /** Record the effective halt request made while evaluating a directive, including status zero. */
    public fun haltRequested(identifier: String, code: Int, engine: String)
}
