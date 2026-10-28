// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.rules

/** Runtime type predicates shared by rule interpreters. */
object TypeRuleHelpers {
    /**
     * Checks if the object is an instance of the provided class.
     *
     * @param clazz The class.
     * @param obj The object.
     * @return A boolean value.
     */
    fun checkClass(clazz: Class<*>, obj: Any?): Boolean = clazz.isInstance(obj)

    /**
     * Checks if the object is a string.
     *
     * @param obj The object.
     * @return A boolean value.
     */
    fun isString(obj: Any?): Boolean = checkClass(String::class.java, obj)

    /**
     * Checks if the object is a list.
     *
     * @param obj The object.
     * @return A boolean value.
     */
    fun isList(obj: Any?): Boolean = checkClass(List::class.java, obj)

    /**
     * Checks if the object is a map.
     *
     * @param obj The object.
     * @return A boolean value.
     */
    fun isMap(obj: Any?): Boolean = checkClass(Map::class.java, obj)

    /**
     * Checks if the object is a boolean.
     *
     * @param obj The object.
     * @return A boolean value.
     */
    fun isBoolean(obj: Any?): Boolean = checkClass(Boolean::class.javaObjectType, obj)
}
