// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.ui.InputHandling
import org.islandoftex.arara.mvel.interpreter.AraraExceptionWithHeader
import org.mvel2.templates.TemplateRuntime
import org.slf4j.LoggerFactory

/**
 * The rule argument model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
class RuleArgument : RuleArgument<Any?> {
    @Transient
    private val logger = LoggerFactory.getLogger(RuleArgument::class.java)

    override var identifier: String = ""

    @SerialName("required")
    override var isRequired: Boolean = false

    private var flag: String? = null
        get() = field?.trim()

    override val defaultValue: Any?
        get() = default?.trim()

    private val default: String? = null

    @Transient
    override val processor: (Any?, Map<String, Any>) -> List<String> = { input, context ->
        try {
            val resolvedContext = context.plus(
                "parameters" to (
                    context
                        .getValue("parameters") as Map<*, *>
                    )
                    .plus(identifier to input!!),
            )
            flag?.let { fn ->
                when (val output = TemplateRuntime.eval(fn, resolvedContext)) {
                    is String -> listOf(output)
                    is List<*> -> InputHandling.flatten(output).map { it.toString() }
                    else -> {
                        logger.warn(
                            "You are using an unsupported return type " +
                                "which may be deprecated in future versions of " +
                                "arara. Please use String or List<String> instead.",
                        )
                        listOf(output.toString())
                    }
                }
            } ?: listOf(input.toString())
        } catch (exception: RuntimeException) {
            throw AraraExceptionWithHeader(
                LanguageController.messages
                    .ERROR_INTERPRETER_FLAG_RUNTIME_EXCEPTION,
                exception,
            )
        }
    }

    /**
     * Validate the argument for later processing.
     */
    fun validate(ruleErrorHeader: String): Boolean = if (identifier.isNotBlank()) {
        if (flag != null || defaultValue != null) {
            true
        } else {
            throw AraraException(
                ruleErrorHeader + LanguageController
                    .messages.ERROR_VALIDATEBODY_MISSING_KEYS,
            )
        }
    } else {
        throw AraraException(
            ruleErrorHeader + LanguageController
                .messages.ERROR_VALIDATEBODY_NULL_ARGUMENT_ID,
        )
    }

    override fun toString(): String {
        return "RuleArgument(identifier='$identifier', isRequired=$isRequired, " +
            "flag=$flag, defaultValue=$defaultValue)"
    }
}
