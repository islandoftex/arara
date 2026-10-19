// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.interpreter

import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.CommandEvaluator
import org.islandoftex.arara.mvel.rules.SerialRuleCommand
import org.islandoftex.arara.mvel.utils.MvelState
import org.mvel2.templates.TemplateRuntime

/** Keeps MVEL expressions and helper bindings outside the execution loop. */
class MvelCommandEvaluator(private val command: SerialRuleCommand) : CommandEvaluator {
    @Suppress("TooGenericExceptionCaught")
    override fun evaluate(parameters: Map<String, Any>): Any? = try {
        TemplateRuntime.eval(command.commandString!!, parameters.plus(MvelState.ruleMethods))
    } catch (exception: RuntimeException) {
        throw AraraExceptionWithHeader(
            LanguageController.messages.ERROR_INTERPRETER_COMMAND_RUNTIME_ERROR,
            exception
        )
    }

    @Suppress("TooGenericExceptionCaught")
    override fun isSuccessful(exitCode: Int): Boolean {
        val check = try {
            TemplateRuntime.eval(
                "@{ " + (command.exit ?: "value == 0") + " }",
                mapOf("value" to exitCode)
            )
        } catch (exception: RuntimeException) {
            throw AraraExceptionWithHeader(
                LanguageController.messages.ERROR_INTERPRETER_EXIT_RUNTIME_ERROR,
                exception
            )
        }
        return check as? Boolean ?: throw AraraExceptionWithHeader(
            LanguageController.messages.ERROR_INTERPRETER_WRONG_EXIT_CLOSURE_RETURN
        )
    }
}
