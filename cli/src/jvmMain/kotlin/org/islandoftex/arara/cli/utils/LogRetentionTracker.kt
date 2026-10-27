// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE
import org.islandoftex.arara.api.configuration.LogRetention
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toJVMPath

/** Tracks byte ranges owned by one invocation, including destinations revisited by later projects. */
internal class LogRetentionTracker {
    private data class Range(val start: Long, val end: Long, val retention: LogRetention)
    private data class Output(val preserveEmpty: Boolean, val ranges: MutableList<Range> = mutableListOf())
    internal data class Segment(val path: MPPPath, val start: Long, val retention: LogRetention)
    private val outputs = linkedMapOf<MPPPath, Output>()

    fun hasOpened(path: MPPPath): Boolean = path in outputs

    fun open(path: MPPPath, append: Boolean, retention: LogRetention): Segment {
        val existing = path.exists
        outputs.getOrPut(path) { Output(append && existing) }
        return Segment(path, if (append && existing) size(path) else 0, retention)
    }

    fun close(segment: Segment) {
        val end = size(segment.path)
        check(end >= segment.start) { "Log file was truncated during execution: ${segment.path}" }
        outputs.getValue(segment.path).ranges.add(Range(segment.start, end, segment.retention))
    }

    fun discardSuccessfulOutput() {
        outputs.forEach { (path, output) -> discard(path, output) }
    }

    private fun discard(path: MPPPath, output: Output) {
        val discarded = output.ranges.filter { it.retention == LogRetention.ON_FAILURE }
        if (discarded.isEmpty()) return
        // MPPPath does not expose random-access writes or truncation. Compact in place,
        // keeping earlier runs and any segments configured with ALWAYS retention.
        val empty = FileChannel.open(path.toJVMPath(), READ, WRITE).use { channel ->
            discarded.asReversed().forEach { remove(channel, it) }
            channel.size() == 0L
        }
        if (empty && !output.preserveEmpty && output.ranges.none { it.retention == LogRetention.ALWAYS }) {
            Files.delete(path.toJVMPath())
        }
    }

    private fun remove(channel: FileChannel, range: Range) {
        val length = range.end - range.start
        if (length == 0L) return
        val buffer = ByteBuffer.allocate(BUFFER_SIZE)
        var readPosition = range.end
        var writePosition = range.start
        val end = channel.size()
        while (readPosition < end) {
            buffer.clear()
            buffer.limit(minOf(buffer.capacity().toLong(), end - readPosition).toInt())
            val count = channel.read(buffer, readPosition)
            check(count > 0) { "Log file changed during cleanup" }
            buffer.flip()
            while (buffer.hasRemaining()) writePosition += channel.write(buffer, writePosition)
            readPosition += count
        }
        channel.truncate(end - length)
    }

    private fun size(path: MPPPath): Long = checkNotNull(path.metadata().size)

    private companion object {
        const val BUFFER_SIZE = 8192
    }
}
