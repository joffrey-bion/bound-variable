package org.hildan.boundvariable.um

import java.io.*
import kotlin.io.path.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("no programs to run")
        return
    }
    val programs = args.map { Path(it).readBytes() }
    val uberProgram = programs.reduce { a, b -> a + b }
    UniversalMachine.run(uberProgram)
}

/**
 * The Universal Machine.
 */
class UniversalMachine(
    initialProgram: IntArray,
    private val stdin: InputStream = System.`in`,
    private val stdout: OutputStream = System.out,
) {
    private val registers: IntArray = IntArray(8)
    private val memory: Memory = Memory(initialProgram)
    private var finger: Int = 0

    @OptIn(ExperimentalStdlibApi::class)
    private fun run() {
        while (true) {
            val instruction = memory[0][finger++]

            val a = (instruction ushr 6) and 0x7
            val b = (instruction ushr 3) and 0x7
            val c = instruction and 0x7

            when (val code = instruction ushr 28) {
                0x0 -> conditionalMove(a, b, c)
                0x1 -> arrayIndex(a, b, c)
                0x2 -> arrayAmendment(a, b, c)
                0x3 -> addition(a, b, c)
                0x4 -> multiplication(a, b, c)
                0x5 -> division(a, b, c)
                0x6 -> notAnd(a, b, c)
                0x7 -> break
                0x8 -> allocation(b = b, c = c)
                0x9 -> abandonment(c = c)
                0xA -> output(c = c)
                0xB -> input(c = c)
                0xC -> loadProgram(b = b, c = c)
                0xD -> orthography(a = (instruction shr 25) and 0x7, data = instruction and 0x1FFFFFF)
                else -> error("invalid instruction code $code (instruction 0x${instruction.toHexString()})")
            }
        }
        stdout.flush()
    }

    private fun conditionalMove(a: Int, b: Int, c: Int) {
        if (registers[c] != 0) {
            registers[a] = registers[b]
        }
    }

    private fun arrayIndex(a: Int, b: Int, c: Int) {
        val address = registers[b]
        val offset = registers[c]
        registers[a] = memory[address][offset]
    }

    private fun arrayAmendment(a: Int, b: Int, c: Int) {
        val address = registers[a]
        val offset = registers[b]
        memory[address][offset] = registers[c]
    }

    private fun addition(a: Int, b: Int, c: Int) {
        registers[a] = registers[b] + registers[c]
    }

    private fun multiplication(a: Int, b: Int, c: Int) {
        registers[a] = registers[b] * registers[c]
    }

    private fun division(a: Int, b: Int, c: Int) {
        val divisor = registers[c]
        if (divisor == 0) {
            error("division by 0")
        }
        registers[a] = (registers[b].toUInt() / divisor.toUInt()).toInt() // TODO check sign
    }

    private fun notAnd(a: Int, b: Int, c: Int) {
        registers[a] = (registers[b] and registers[c]).inv()
    }

    private fun allocation(b: Int, c: Int) {
        registers[b] = memory.alloc(capacity = registers[c])
    }

    private fun abandonment(c: Int) {
        memory.free(address = registers[c])
    }

    private fun output(c: Int) {
        val output = registers[c]
        if (output > 255) {
            error("invalid output value $output")
        }
        // Writes the 8 lowest-order bits of the integer, which is ok even for 255 (0x000000FF -> 0xFF).
        // In that case it will result in the byte -1 (or, considered unsigned, still 255) which is the correct value.
        // Converting to Char and using print() is incorrect.
        stdout.write(output)
        stdout.flush() // even though print streams auto-flush after \n, we want to flush after prompts too
    }

    private fun input(c: Int) {
        val inputByte = stdin.read()
        if (inputByte < 0) {
            error("unexpected end of input")
        }
        registers[c] = inputByte
    }

    private fun loadProgram(b: Int, c: Int) {
        val address = registers[b]
        if (address != 0) { // no need for a copy in that case, it's just for the finger move
            memory.loadProgramFrom(address = address)
        }
        finger = registers[c]
    }

    private fun orthography(a: Int, data: Int) {
        registers[a] = data
    }

    companion object {
        fun run(program: ByteArray, stdin: InputStream = System.`in`, stdout: OutputStream = System.out) {
            UniversalMachine(program.concatBytesToIntsBE(), stdin, stdout).run()
        }
    }
}
