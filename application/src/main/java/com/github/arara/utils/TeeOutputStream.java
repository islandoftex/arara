package com.github.arara.utils;

// needed imports
import java.io.IOException;
import java.io.OutputStream;

/**
 * Implements an output stream that handles other streams, so the flow is
 * propagated to all of them in an easy way.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class TeeOutputStream extends OutputStream {

    // the list of the output streams
    private final OutputStream[] streams;

    /**
     * Constructor.
     *
     * @param outputStreams An array containing the output streams.
     */
    public TeeOutputStream(OutputStream... outputStreams) {

        // set the array
        streams = outputStreams;
    }

    /**
     * {@inheritDoc}
     *
     * Writes the byte to all the output streams.
     */
    @Override
    public void write(int b) throws IOException {

        // for every stream
        for (OutputStream ostream : streams) {

            // write the byte
            ostream.write(b);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Writes an array of bytes to all the output streams.
     */
    @Override
    public void write(byte[] b, int offset, int length) throws IOException {

        // for every stream
        for (OutputStream ostream : streams) {

            // write the array
            ostream.write(b, offset, length);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Flushes the stream.
     */
    @Override
    public void flush() throws IOException {

        // for every stream
        for (OutputStream ostream : streams) {

            // flush the stream
            ostream.flush();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Closes the stream.
     */
    @Override
    public void close() throws IOException {

        // for every stream
        for (OutputStream ostream : streams) {

            // try to close the stream
            forceClose(ostream);
        }
    }

    /**
     * Forces the stream to close without raising exceptions.
     *
     * @param ostream The stream.
     */
    private void forceClose(OutputStream ostream) {

        // try to close
        try {

            // close it
            ostream.close();

        } catch (IOException exception) {
            // do nothing
        }
    }
}
