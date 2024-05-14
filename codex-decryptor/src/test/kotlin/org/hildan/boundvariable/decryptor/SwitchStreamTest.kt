package org.hildan.boundvariable.decryptor

import java.io.*
import kotlin.test.*

class SwitchStreamTest {

    @Test
    fun writeSingle_immediateSwitch() {
        val stream1 = ByteArrayOutputStream()
        val stream2 = ByteArrayOutputStream()
        val switchStream = SwitchStream(nBytesBeforeSwitch = 0, stream1, stream2)
        repeat(6) {
            switchStream.write(it)
        }
        assertContentEquals(byteArrayOf(), stream1.toByteArray())
        assertContentEquals(byteArrayOf(0, 1, 2, 3, 4, 5), stream2.toByteArray())
    }

    @Test
    fun writeSingle_switchMiddle() {
        val stream1 = ByteArrayOutputStream()
        val stream2 = ByteArrayOutputStream()
        val switchStream = SwitchStream(nBytesBeforeSwitch = 4, stream1, stream2)
        repeat(6) {
            switchStream.write(it)
        }
        assertContentEquals(byteArrayOf(0, 1, 2, 3), stream1.toByteArray())
        assertContentEquals(byteArrayOf(4, 5), stream2.toByteArray())
    }

    @Test
    fun writeBatch_immediateSwitch() {
        val stream2 = ByteArrayOutputStream()
        val stream1 = ByteArrayOutputStream()
        val switchStream = SwitchStream(nBytesBeforeSwitch = 0, stream1, stream2)

        switchStream.write(byteArrayOf(1, 2, 3, 4))
        assertContentEquals(byteArrayOf(), stream1.toByteArray())
        assertContentEquals(byteArrayOf(1, 2, 3, 4), stream2.toByteArray())

        switchStream.write(byteArrayOf(5, 6))
        assertContentEquals(byteArrayOf(), stream1.toByteArray())
        assertContentEquals(byteArrayOf(1, 2, 3, 4, 5, 6), stream2.toByteArray())
    }

    @Test
    fun writeBatch_switchExactlyBetweenBatches() {
        val stream2 = ByteArrayOutputStream()
        val stream1 = ByteArrayOutputStream()
        val switchStream = SwitchStream(nBytesBeforeSwitch = 4, stream1, stream2)

        switchStream.write(byteArrayOf(1, 2, 3, 4))
        assertContentEquals(byteArrayOf(1, 2, 3, 4), stream1.toByteArray())
        assertContentEquals(byteArrayOf(), stream2.toByteArray())

        switchStream.write(byteArrayOf(5, 6, 7, 8))
        assertContentEquals(byteArrayOf(1, 2, 3, 4), stream1.toByteArray())
        assertContentEquals(byteArrayOf(5, 6, 7, 8), stream2.toByteArray())
    }

    @Test
    fun writeBatch_switchDuringBatch_onFirstBatch() {
        val stream2 = ByteArrayOutputStream()
        val stream1 = ByteArrayOutputStream()
        val switchStream = SwitchStream(nBytesBeforeSwitch = 4, stream1, stream2)

        switchStream.write(byteArrayOf(1, 2, 3, 4, 5, 6))
        assertContentEquals(byteArrayOf(1, 2, 3, 4), stream1.toByteArray())
        assertContentEquals(byteArrayOf(5, 6), stream2.toByteArray())
    }

    @Test
    fun writeBatch_switchDuringBatch_onLaterBatch() {
        val stream2 = ByteArrayOutputStream()
        val stream1 = ByteArrayOutputStream()
        val switchStream = SwitchStream(nBytesBeforeSwitch = 6, stream1, stream2)

        switchStream.write(byteArrayOf(1, 2, 3, 4))
        assertContentEquals(byteArrayOf(1, 2, 3, 4), stream1.toByteArray())
        assertContentEquals(byteArrayOf(), stream2.toByteArray())

        switchStream.write(byteArrayOf(5, 6, 7, 8))
        assertContentEquals(byteArrayOf(1, 2, 3, 4, 5, 6), stream1.toByteArray())
        assertContentEquals(byteArrayOf(7, 8), stream2.toByteArray())
    }

    @Test
    fun writeBatchWithIndices() {
        val stream2 = ByteArrayOutputStream()
        val stream1 = ByteArrayOutputStream()
        val switchStream = SwitchStream(nBytesBeforeSwitch = 3, stream1, stream2)

        switchStream.write(byteArrayOf(1, 2, 3, 4), 1, 2)
        assertContentEquals(byteArrayOf(2, 3), stream1.toByteArray())
        assertContentEquals(byteArrayOf(), stream2.toByteArray())

        switchStream.write(byteArrayOf(4, 5, 6, 7, 8, 9), off = 2, len = 3)
        assertContentEquals(byteArrayOf(2, 3, 6), stream1.toByteArray())
        assertContentEquals(byteArrayOf(7, 8), stream2.toByteArray())
    }
}
