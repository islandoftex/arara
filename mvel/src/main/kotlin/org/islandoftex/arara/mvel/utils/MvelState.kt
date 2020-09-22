// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.utils

/**
 * Implements some auxiliary methods for runtime evaluation.
 *
 * @author Island of TeX
 * @version 6.0
 * @since 6.0
 */
object MvelState {
    /**
     * A map of rule method names to method pointers.
     */
    @JvmStatic
    val ruleMethods: Map<String, Any> by lazy {
        val map = conditionalMethods.toMutableMap()
        kotlin.runCatching {
            val methodsKotlin = RuleMethods::class.java.methods
            listOf("halt", "getOriginalFile", "getOriginalReference",
                    "trimSpaces", "getBasename", "getFiletype", "replicatePattern",
                    "throwError", "getSession", "buildString", "getCommand",
                    "getCommandWithWorkingDirectory", "isVerboseMode",
                    "showMessage", "isOnPath", "unsafelyExecuteSystemCommand",
                    "listFilesByExtensions", "listFilesByPatterns",
                    "writeToFile", "readFromFile", "isSubdirectory",
                    "isEmpty", "isNotEmpty", "isTrue", "isFalse",
                    "isWindows", "isLinux", "isMac", "isUnix", "isCygwin",
                    "checkClass", "isString", "isList", "isMap", "isBoolean"
            ).forEach { name: String ->
                map[name] = methodsKotlin.first { it.name == name }
            }
        }
        map
    }

    /**
     * A map of conditional method names to method pointers.
     */
    @JvmStatic
    val conditionalMethods: Map<String, Any> by lazy {
        val map = mutableMapOf<String, Any>()
        kotlin.runCatching {
            val methodsKotlin = ConditionalMethods::class.java.methods
            listOf("exists", "missing", "changed", "unchanged",
                    "found", "toFile", "showDropdown", "showInput",
                    "showOptions", "currentFile", "loadClass", "loadObject"
            ).forEach { name: String ->
                map[name] = methodsKotlin.first { it.name == name }
            }
        }
        map
    }

    /**
     * A map of directive method names to method pointers.
     */
    @JvmStatic
    val directiveMethods: Map<String, Any> by lazy {
        val map = conditionalMethods.toMutableMap()
        kotlin.runCatching {
            val methodsKotlin = RuleMethods::class.java.methods
            listOf("getOriginalReference", "getBasename", "getSession")
                    .forEach { name: String ->
                        map[name] = methodsKotlin.first { it.name == name }
                    }
        }
        map
    }

    /**
     * Save the available preambles from the configuration file.
     */
    val preambles: MutableMap<String, String> = mutableMapOf()
}
