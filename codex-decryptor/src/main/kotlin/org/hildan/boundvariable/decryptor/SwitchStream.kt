package org.hildan.boundvariable.decryptor

import java.io.*

/**
 * Writes the first [nBytesBeforeSwitch] bytes to [outputStream1], and write subsequent bytes to [outputStream2].
 *
 * [outputStream1] is flushed once all [nBytesBeforeSwitch] have been written to it.
 */
internal class SwitchStream(
    private var nBytesBeforeSwitch: Int,
    private val outputStream1: OutputStream,
    private val outputStream2: OutputStream,
) : OutputStream() {

    override fun write(b: Int) {
        if (nBytesBeforeSwitch == 0) {
            outputStream2.write(b)
        } else {
            outputStream1.write(b)
            nBytesBeforeSwitch--
            if (nBytesBeforeSwitch == 0) {
                outputStream1.flush()
            }
        }
    }

    override fun write(buf: ByteArray, off: Int, len: Int) {
        val len2 = (len - nBytesBeforeSwitch).coerceAtLeast(0)
        val len1 = len - len2
        outputStream1.write(buf, off, len1)
        outputStream2.write(buf, off + len1, len2)
        nBytesBeforeSwitch -= len1
        if (nBytesBeforeSwitch == 0) {
            outputStream1.flush()
        }
    }
}
