// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.lua.rules

import org.islandoftex.arara.api.AraraException
import org.luaj.vm2.LuaError

internal object LuaFailures {
    fun diagnostic(error: LuaError): String {
        val text = error.getLuaMessage() ?: error.message ?: error.toString()
        val location = error.fileline
        return if (location.isNullOrBlank() || text.contains(location)) text else "$location: $text"
    }

    /** Luak keeps wrapped callback failures in luaCause rather than Throwable.cause. */
    fun <T> withLuaErrorHandling(message: String, block: () -> T): T = try {
        block()
    } catch (exception: LuaHelperException) {
        throw AraraException(message, exception)
    } catch (exception: LuaError) {
        val araraCause = generateSequence<Throwable>(exception) {
            if (it is LuaError) it.luaCause ?: it.cause else it.cause
        }.filterIsInstance<AraraException>().firstOrNull()
        throw when (araraCause) {
            is LuaHelperException -> AraraException(message, araraCause)
            null -> AraraException(message, Exception(diagnostic(exception), exception))
            else -> araraCause
        }
    }}
