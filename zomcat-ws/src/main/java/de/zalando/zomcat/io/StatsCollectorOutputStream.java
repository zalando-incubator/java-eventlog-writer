package de.zalando.zomcat.io;

import java.io.IOException;
import java.io.OutputStream;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The class implements an output stream, which collects the number of bytes written to the stream. Whenever an
 * application writes to this stream, an internal counter tracks the total number of bytes written.
 *
 * @author  rreis
 */
public class StatsCollectorOutputStream extends OutputStream {

    /**
     * The underlying output stream to be used.
     */
    protected OutputStream out;

    /**
     * The current number of bytes written to the stream.
     */
    private long bytesWritten = 0;

    /**
     * A list of objects interested in relevant events from this stream.
     */
    private List<StatsCollectorOutputStreamCallback> callbacks;

    /**
     * Creates a new instance to write data to the specified output stream.
     *
     * @param  out  the underlying output stream.
     */
    public StatsCollectorOutputStream(final OutputStream out) {
        super();
        if (out == null) {
            throw new IllegalArgumentException("Stream may not be null");
        }

        this.out = out;
    }

    /**
     * Registers the specified object as a callback for relevant events from this stream.
     *
     * <p>The specified object is notified whenever a relevant event happens.
     *
     * @param  callback  the object to register.
     *
     * @see    StatsCollectorOutputStreamCallback
     */
    public void registerCallback(final StatsCollectorOutputStreamCallback callback) {

        // Creates the list of callbacks, in case it doesn't exist.
        if (null == callbacks) {
            callbacks = new LinkedList<>();
        }

        callbacks.add(callback);
    }

    /**
     * Unregisters the specified callback object for relevant events from this stream.
     *
     * @param  callback  the object to unregister.
     *
     * @see    StatsCollectorOutputStreamCallback
     */
    public void unregisterCallback(final StatsCollectorOutputStreamCallback callback) {

        // If the callback list doesn't exist, doesn't do anything.
        if (null != callbacks) {
            callbacks.remove(callback);
        }
    }

    /**
     * Returns a list of all registered callback objects.
     *
     * <p>The returned list is an unmodifiable view of the actual list. Attemps to modify it wil result in an <code>
     * UnsupportedOperationException</code>.
     *
     * @return  the list of registered callback objects, or <code>null</code> if it wasn't initialized.
     *
     * @see     StatsCollectorOutputStreamCallback
     */
    public List<StatsCollectorOutputStreamCallback> getCallbacks() {

        // returns null if no object has registered yet.
        return callbacks == null ? null : Collections.unmodifiableList(callbacks);
    }

    /**
     * Returns number of bytes written to the stream.
     *
     * @return  the current count of bytes.
     */
    public long getBytesWritten() {
        return bytesWritten;
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * <p>The write method of this output stream calls the write method of its underlying output stream, that is, it
     * performs <code>out.write(b)</code>. Also increments the total number of bytes written to the stream.
     *
     * @param      b  the <code>byte</code>.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void write(final int b) throws IOException {
        out.write(b);

        // Increment the total number of bytes written
        bytesWritten++;
    }

    /**
     * Writes <code>b.length</code> bytes to this output stream.
     *
     * <p>The write method of this output stream calls the write method of its underlying output stream, that is, it
     * performs <code>out.write(b)</code>. Also increments the total number of bytes written to the stream.
     *
     * @param      b  the data to be written.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void write(final byte[] b) throws IOException {
        out.write(b);

        bytesWritten += b.length;
    }

    /**
     * Writes <code>len</code> bytes from the specified <code>byte</code> array stating at offset <code>off</code> to
     * this output stream.
     *
     * <p>The write method of this output stream calls the write method of its underlying output stream, that is, it
     * performs <code>out.write(b, off, len)</code>. Also adds <code>len</code> to the total number of bytes written to
     * the stream.
     *
     * @param      b    the data.
     * @param      off  the start offset in the data.
     * @param      len  the number of bytes to write.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        out.write(b, off, len);

        bytesWritten += len;
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out to the stream.
     *
     * <p>The <code>flush</code> method of this output stream calls the <code>flush</code> method of its underlying
     * output stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Closes this output stream and releases any system resources associated with the stream.
     *
     * <p>The <code>close</code> method of this output stream calls the <code>flush</code> and the <code>close</code>
     * method of its underlying output stream.
     *
     * <p>Registered callback object will be notified, through their <code>onClose</code> method.
     *
     * @exception  IOException  if an I/O error occurs.
     *
     * @see        StatsCollectorOutputStreamCallback
     */
    @Override
    public void close() throws IOException {
        out.flush();
        out.close();

        // Notify all callbacks that the stream is closed.
        if (null != callbacks) {
            for (StatsCollectorOutputStreamCallback cb : callbacks) {
                cb.onClose(this);
            }
        }
    }
}
