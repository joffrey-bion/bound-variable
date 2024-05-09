package org.hildan.boundvariable.um

import kotlin.io.path.*

typealias Registers = IntArray

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("no programs to run")
        return
    }
    val programs = args.map { Path(it).readBytes() }
    val uberProgram = programs.reduce { a, b -> a + b }
    UniversalMachine(uberProgram).run()
}

/**
 * The Universal Machine.
 */
class UniversalMachine(program: IntArray) {

    private val registers: Registers = IntArray(N_REGISTERS)
    private val memory: Memory = Memory(program)
    private var finger: Int = 0

    constructor(legacyProgram: ByteArray) : this(legacyProgram.concatBytesToIntsBE())

    fun run() {
        try {
            while (true) {
                val instruction = memory[0][finger]
                val operator = Operator.decode(instruction)
                finger = operator.execute(registers, memory, finger)
            }
        } catch (e: HaltProgram) {
            return
        }
    }

    companion object {
        const val N_REGISTERS: Int = 8
    }
}

/**
 * Creates an [IntArray] by concatenating bytes from this [ByteArray] in groups of 4.
 * The bytes are read in big-endian order: the MSB first.
 */
private fun ByteArray.concatBytesToIntsBE() = IntArray(size / 4) {
    val bytePos = it * 4
    intOf(
        a = this[bytePos],
        b = this[bytePos + 1],
        c = this[bytePos + 2],
        d = this[bytePos + 3],
    )
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
