// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.watch

import java.io.Closeable
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_DELETE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.concurrent.TimeUnit
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.WatchMode
import org.islandoftex.arara.api.files.toJVMPath

/** Native notifications are hints; snapshots remain the authority for deciding whether inputs changed. */
class WatchMonitor(private val mode: WatchMode) : Closeable {
    private var service: WatchService? = if (mode == WatchMode.POLLING) null else try {
        FileSystems.getDefault().newWatchService()
    } catch (exception: IOException) {
        if (mode == WatchMode.NATIVE) throw exception
        null
    } catch (exception: UnsupportedOperationException) {
        if (mode == WatchMode.NATIVE) throw exception
        null
    }
    private val registered = mutableMapOf<MPPPath, WatchKey>()

    /** Register newly discovered directories and remove obsolete registrations after graph reloads. */
    fun update(directories: Set<MPPPath>) {
        val watcher = service ?: return
        registered.keys.filter { it !in directories || registered[it]?.isValid != true }.forEach {
            registered.remove(it)?.cancel()
        }
        try {
            directories.filter { it !in registered }.forEach {
                registered[it] = it.toJVMPath().register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
            }
        } catch (exception: IOException) {
            if (mode == WatchMode.NATIVE) throw exception
            close()
        } catch (exception: UnsupportedOperationException) {
            if (mode == WatchMode.NATIVE) throw exception
            close()
        }
    }

    /** Wait for a notification or reconciliation deadline. Never treats an event as a content change itself. */
    fun await(timeoutMillis: Long) {
        val watcher = service
        if (watcher == null) {
            Thread.sleep(timeoutMillis)
        } else {
            var key = watcher.poll(timeoutMillis, TimeUnit.MILLISECONDS)
            while (key != null) {
                key.pollEvents()
                key.reset()
                key = watcher.poll()
            }
        }
    }

    /** Release operating-system watch registrations. */
    override fun close() {
        service?.close()
        service = null
        registered.clear()
    }
}
