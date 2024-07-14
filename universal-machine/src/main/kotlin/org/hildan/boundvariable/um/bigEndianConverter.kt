package org.hildan.boundvariable.um

/**
 * Creates an [IntArray] by concatenating bytes from this [ByteArray] in groups of 4.
 * The bytes are read in big-endian order: the MSB first.
 */
fun ByteArray.concatBytesToIntsBE(): IntArray {
    require(size % 4 == 0) { "number of bytes should be a multiple of 4" }
    return IntArray(size / 4) {
        val bytePos = it * 4
        intOf(
            a = this[bytePos],
            b = this[bytePos + 1],
            c = this[bytePos + 2],
            d = this[bytePos + 3],
        )
    }
}

/**
 * Concatenates the given 4 bytes into an integer, where [a] is the MSB and [d] the LSB.
 */
private fun intOf(a: Byte, b: Byte, c: Byte, d: Byte): Int {
    val aa = a.toInt() and 0xFF shl 24
    val bb = b.toInt() and 0xFF shl 16
    val cc = c.toInt() and 0xFF shl 8
    val dd = d.toInt() and 0xFF
    return aa or bb or cc or dd
}
