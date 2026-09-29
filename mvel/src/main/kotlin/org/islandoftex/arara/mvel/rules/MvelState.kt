// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import org.islandoftex.arara.core.rules.ConditionalMethods
import org.islandoftex.arara.core.rules.RuleMethods

/**
 * Implements some auxiliary methods for runtime evaluation.
 *
 * @author Island of TeX
 */
object MvelState {
    /**
     * A map of rule method names to method pointers. The pointers are
     * reflective methods invoked without an instance, so the bound
     * helpers have to be true statics - the wiring below checks that
     * a method exists by name, but only the helper objects themselves
     * carry the annotations that make the invocation work.
     */
    val ruleMethods: Map<String, Any> by lazy {
        val map = conditionalMethods.toMutableMap()
        // a method that cannot be wired has to fail loudly: swallowing
        // the failure would silently drop every method listed after it
        val methodsKotlin = RuleMethods::class.java.methods
        listOf(
            "halt", "getOriginalFile", "getOriginalReference",
            "trimSpaces", "getBasename", "getFiletype", "replicatePattern",
            "throwError", "getSession", "buildString", "getCommand",
            "getCommandWithWorkingDirectory", "isVerboseMode",
            "showMessage", "isOnPath", "unsafelyExecuteSystemCommand",
            "listFilesByExtensions", "listFilesByPatterns",
            "writeToFile", "readFromFile", "isSubdirectory",
            "isEmpty", "isNotEmpty", "isTrue", "isFalse",
            "isWindows", "isLinux", "isMac", "isUnix", "isCygwin",
            "checkClass", "isString", "isList", "isMap", "isBoolean",
            "getOrNull"
        ).forEach { name: String ->
            map[name] = methodsKotlin.firstOrNull { it.name == name }
                ?: throw IllegalStateException(
                    "The rule method '$name' cannot be found; the " +
                        "registry in MvelState is out of sync with " +
                        "RuleMethods."
                )
        }
        map
    }

    /**
     * A map of conditional method names to method pointers.
     */
    val conditionalMethods: Map<String, Any> by lazy {
        val map = mutableMapOf<String, Any>()
        val methodsKotlin = ConditionalMethods::class.java.methods
        listOf(
            "exists", "missing", "changed", "unchanged",
            "found", "toFile", "showDropdown", "showInput",
            "showOptions", "currentFile", "loadClass", "loadObject"
        ).forEach { name: String ->
            map[name] = methodsKotlin.firstOrNull { it.name == name }
                ?: throw IllegalStateException(
                    "The conditional method '$name' cannot be found; the " +
                        "registry in MvelState is out of sync with " +
                        "ConditionalMethods."
                )
        }
        val ruleMethodsKotlin = RuleMethods::class.java.methods
        listOf("getOriginalReference", "getBasename", "getSession")
            .forEach { name: String ->
                map[name] = ruleMethodsKotlin.firstOrNull { it.name == name }
                    ?: throw IllegalStateException(
                        "The conditional method '$name' cannot be found; " +
                            "the registry in MvelState is out of sync " +
                            "with RuleMethods."
                    )
            }
        map
    }

    /**
     * A map of directive method names to method pointers.
     *
     * Currently a wrapper around [conditionalMethods] but introduced for
     * easier extensibility.
     */
    val directiveMethods: Map<String, Any>
        get() = conditionalMethods

    /**
     * Save the available preambles from the configuration file.
     */
    val preambles: MutableMap<String, String> = mutableMapOf()

    /**
     * Specify the default preamble to pick if there is neither a preamble
     * nor directives specified.
     */
    var defaultPreamble: String? = null

    /**
     * Whether to prepend preamble directives even if there are directives
     * in the document. Useful for enabling preambles only for documents
     * without preambles.
     *
     * Set to `true` for reasons of backward compatibility.
     */
    var prependPreambleIfDirectivesGiven: Boolean = true
}
