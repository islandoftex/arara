// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.rules

import java.lang.reflect.Modifier
import org.islandoftex.arara.core.rules.ConditionalMethods
import org.islandoftex.arara.core.rules.RuleMethods

/**
 * The helper registries of the MVEL dialect: the reflective
 * method pointers the template evaluation interpolates against.
 * MVEL evaluates strings, so its helpers arrive as a pre-wired
 * map; the Lua dialect binds the very same helpers directly as
 * functions and keeps no registry of its own.
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
            // the reflective lookup demands the static mirror: a
            // companion carries the same name as an instance method
            // too, and the array's order is the JVM's choice
            map[name] = methodsKotlin.firstOrNull {
                it.name == name && Modifier.isStatic(it.modifiers)
            }
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
            // the static mirror again: the lookup must not land on
            // the instance twin of the same name
            map[name] = methodsKotlin.firstOrNull {
                it.name == name && Modifier.isStatic(it.modifiers)
            }
                ?: throw IllegalStateException(
                    "The conditional method '$name' cannot be found; the " +
                        "registry in MvelState is out of sync with " +
                        "ConditionalMethods."
                )
        }
        val ruleMethodsKotlin = RuleMethods::class.java.methods
        listOf("getOriginalReference", "getBasename", "getSession")
            .forEach { name: String ->
                map[name] = ruleMethodsKotlin.firstOrNull {
                    it.name == name && Modifier.isStatic(it.modifiers)
                }
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
}
