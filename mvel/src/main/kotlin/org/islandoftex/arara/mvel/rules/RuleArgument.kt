// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.InputHandling
import org.mvel2.templates.TemplateRuntime
import org.slf4j.LoggerFactory

/**
 * The rule argument model.
 *
 * @author Island of TeX
 */
@Serializable
class RuleArgument : RuleArgument<Any?> {
    /**
     * The logger of the rule argument evaluation.
     */
    @Transient
    private val logger = LoggerFactory.getLogger(RuleArgument::class.java)

    /**
     * The identifier directives use to pass this argument.
     */
    override var identifier: String = ""

    /**
     * Whether the argument must be present in every directive.
     */
    @SerialName("required")
    override var isRequired: Boolean = false

    /**
     * The flag the argument renders when the value is
     * present, or null when the default applies.
     */
    private var flag: String? = null
        get() = field?.trim()

    /**
     * The value the argument takes when the directive omits it.
     */
    override val defaultValue: Any?
        get() = default?.trim()

    /**
     * The default value the argument falls back to when
     * the directive provides none, or null when the
     * argument is required.
     */
    private val default: String? = null

    /**
     * Evaluates the MVEL flag expression of the argument against the
     * input value, returning the resulting list of flags.
     */
    @Transient
    // user-provided MVEL flag expressions may throw anything, so the
    // catch has to stay generic to wrap the failure into an arara error
    @Suppress("TooGenericExceptionCaught")
    override val processor: (Any?, Map<String, Any>) -> List<String> = { input, context ->
        try {
            val resolvedContext = context
                .plus(
                    "parameters" to (
                        context
                            .getValue("parameters") as Map<*, *>
                        )
                        .plus(identifier to input!!)
                )
                // the MVEL flag expressions call the rule helpers, so
                // the registry joins the context here: the shared call
                // site passes only what every engine needs, and each
                // engine brings its own machinery
                .plus(MvelState.ruleMethods)
            flag?.let { fn ->
                when (val output = TemplateRuntime.eval(fn, resolvedContext)) {
                    is String -> listOf(output)
                    is List<*> -> InputHandling.flatten(output).map { it.toString() }
                    else -> {
                        logger.warn(
                            "You are using an unsupported return type " +
                                "which may be deprecated in future versions of " +
                                "arara. Please use String or List<String> instead."
                        )
                        listOf(output.toString())
                    }
                }
            } ?: listOf(input.toString())
        } catch (exception: RuntimeException) {
            throw AraraExceptionWithHeader(
                LanguageController.messages
                    .errorInterpreterFlagRuntimeException,
                exception
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
                    .messages.errorValidateBodyMissingKeys
            )
        }
    } else {
        throw AraraException(
            ruleErrorHeader + LanguageController
                .messages.errorValidateBodyNullArgumentId
        )
    }

    /**
     * A human-readable representation of the argument, for logs and
     * error reports.
     */
    override fun toString(): String {
        return "RuleArgument(identifier='$identifier', isRequired=$isRequired, " +
            "flag=$flag, defaultValue=$defaultValue)"
    }
}
