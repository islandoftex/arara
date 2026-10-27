// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.MapMessage

internal actual object EventLog {
    private val logger = LogManager.getLogger(RunLog::class.java)

    actual val isTraceEnabled: Boolean get() = logger.isTraceEnabled
    actual val isInfoEnabled: Boolean get() = logger.isInfoEnabled
    actual val isErrorEnabled: Boolean get() = logger.isErrorEnabled

    actual fun trace(event: String, message: String, fields: Map<String, Any>) {
        if (logger.isTraceEnabled) logger.trace(EventMessage(event, message, fields))
    }

    actual fun debug(event: String, message: String, fields: Map<String, Any>) {
        if (logger.isDebugEnabled) logger.debug(EventMessage(event, message, fields))
    }

    actual fun info(event: String, message: String, fields: Map<String, Any>) {
        if (logger.isInfoEnabled) logger.info(EventMessage(event, message, fields))
    }

    actual fun warn(event: String, message: String, fields: Map<String, Any>) {
        if (logger.isWarnEnabled) logger.warn(EventMessage(event, message, fields))
    }

    actual fun error(event: String, message: String, fields: Map<String, Any>, cause: Throwable?) {
        if (logger.isErrorEnabled) {
            logger.error(EventMessage(event, message, fields), if (LoggingUtils.isJson) cause else null)
        }
    }

    private class EventMessage(event: String, private val text: String, fields: Map<String, Any>) :
        MapMessage<EventMessage, Any>(fields + ("event" to event)) {
        override fun getFormattedMessage(): String = text
        override fun getFormattedMessage(formats: Array<out String>?): String = text
        override fun formatTo(buffer: StringBuilder) { buffer.append(text) }
        override fun formatTo(formats: Array<out String>?, buffer: StringBuilder) { buffer.append(text) }
        override fun toString(): String = text
    }
}
