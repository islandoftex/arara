// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.utils

import java.io.IOException
import java.io.OutputStream

/**
 * Implements a stream splitter.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class TeeOutputStream(
    /**
     * The array of output streams holds every output stream that will be
     * written to.
     */
    vararg outputStreams: OutputStream
) : OutputStream() {
    /**
     * An array of streams in which an object of this class will split data.
     */
    private val streams: List<OutputStream> = outputStreams.toList()

    /**
     * Writes the provided integer to each stream.
     *
     * @param b The provided integer
     * @throws IOException An IO exception.
     */
    @Throws(IOException::class)
    override fun write(b: Int) = streams.forEach { it.write(b) }

    /**
     * Writes the provided byte array to each stream, with the provided offset
     * and length.
     *
     * @param b The byte array.
     * @param offset The offset.
     * @param length The length.
     * @throws IOException An IO exception.
     */
    @Throws(IOException::class)
    override fun write(b: ByteArray, offset: Int, length: Int) =
            streams.forEach { it.write(b, offset, length) }

    /**
     * Flushes every stream.
     *
     * @throws IOException An IO exception.
     */
    @Throws(IOException::class)
    override fun flush() = streams.forEach { it.flush() }

    /**
     * Closes every stream silently.
     */
    override fun close() = streams.forEach {
        try {
            it.close()
        } catch (ignored: IOException) {
            // do nothing on purpose
        }
    }
}
