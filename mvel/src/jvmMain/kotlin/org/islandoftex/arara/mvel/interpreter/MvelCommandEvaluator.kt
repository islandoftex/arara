// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.interpreter

import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.rules.CommandEvaluator
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.mvel.rules.SerialRuleCommand
import org.islandoftex.arara.mvel.utils.MvelState
import org.mvel2.templates.TemplateRuntime

/** Keeps MVEL expressions and helper bindings outside the execution loop. */
class MvelCommandEvaluator(private val command: SerialRuleCommand) : CommandEvaluator {
    /** Evaluate the command template with processed parameters and the shared rule helpers. */
    override fun evaluate(parameters: Map<String, Any>): Any? = withMvelErrorHandling(header = true,
        message = { LanguageController.messages.errorInterpreterCommandRuntimeError }
    ) {
        TemplateRuntime.eval(command.commandString!!, parameters.plus(MvelState.ruleMethods))
    }

    /** Evaluate the exit predicate as a strict boolean; only zero succeeds when none is supplied. */
    override fun isSuccessful(exitCode: Int): Boolean {
        val check = withMvelErrorHandling(header = true,
            message = { LanguageController.messages.errorInterpreterExitRuntimeError }
        ) {
            TemplateRuntime.eval(
                "@{ " + (command.exit ?: "value == 0") + " }",
                mapOf("value" to exitCode)
            )
        }
        return check as? Boolean ?: throw AraraExceptionWithHeader(
            LanguageController.messages.errorInterpreterWrongExitClosureReturn
        )
    }
}
