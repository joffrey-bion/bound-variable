package org.hildan.boundvariable.um

/**
 * Exception thrown to halt the program normally.
 */
class HaltProgram : Exception()

/**
 * A decoded instruction for the Universal Machine.
 */
interface Operator {
    /**
     * Executes this operator on the given [registers] and [memory], and return the new finger.
     * The current [finger] can be used to calculate the new one.
     */
    fun execute(registers: Registers, memory: Memory, finger: Int): Int

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun decode(value: Int): Operator {
            val a = (value ushr 6) and 0x7
            val b = (value ushr 3) and 0x7
            val c = value and 0x7
            return when (val code = value ushr 28) {
                0 -> ConditionalMove(a, b, c)
                1 -> ArrayIndex(a, b, c)
                2 -> ArrayAmendment(a, b, c)
                3 -> Addition(a, b, c)
                4 -> Multiplication(a, b, c)
                5 -> Division(a, b, c)
                6 -> NotAnd(a, b, c)
                7 -> Halt
                8 -> Allocation(b = b, c = c)
                9 -> Abandonment(c = c)
                10 -> Output(c = c)
                11 -> Input(c = c)
                12 -> LoadProgram(b = b, c = c)
                13 -> Orthography(
                    a = (value shr 25) and 0x7,
                    data = value and 0x1FFFFFF,
                )
                else -> error("invalid instruction code $code (instruction 0x${value.toHexString()})")
            }
        }
    }
}

private interface StandardOperator : Operator {

    override fun execute(registers: Registers, memory: Memory, finger: Int): Int {
        execute(registers, memory)
        return finger + 1
    }

    fun execute(registers: Registers, memory: Memory)
}

private data class ConditionalMove(val a: Int, val b: Int, val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        if (registers[c] != 0) {
            registers[a] = registers[b]
        }
    }
}

private data class ArrayIndex(val a: Int, val b: Int, val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        val address = registers[b]
        val offset = registers[c]
        registers[a] = memory[address][offset]
    }
}

private data class ArrayAmendment(val a: Int, val b: Int, val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        val address = registers[a]
        val offset = registers[b]
        memory[address][offset] = registers[c]
    }
}

private data class Addition(val a: Int, val b: Int, val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        registers[a] = registers[b] + registers[c]
    }
}

private data class Multiplication(val a: Int, val b: Int, val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        registers[a] = registers[b] * registers[c]
    }
}

private data class Division(val a: Int, val b: Int, val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        val divisor = registers[c]
        if (divisor == 0) {
            error("division by 0")
        }
        registers[a] = (registers[b].toUInt() / divisor.toUInt()).toInt() // TODO check sign
    }
}

private data class NotAnd(val a: Int, val b: Int, val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        registers[a] = (registers[b] and registers[c]).inv()
    }
}

private data object Halt : Operator {
    override fun execute(registers: Registers, memory: Memory, finger: Int): Int {
        throw HaltProgram()
    }
}

private data class Allocation(val b: Int, val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        registers[b] = memory.alloc(capacity = registers[c])
    }
}

private data class Abandonment(val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        memory.free(address = registers[c])
    }
}

private data class Output(val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        val output = registers[c]
        if (output > 255) {
            error("invalid output value $output")
        }
        print(output.toChar()) // flushes on EOL
    }
}

private data class Input(val c: Int) : StandardOperator {
    override fun execute(registers: Registers, memory: Memory) {
        val inputByte = System.`in`.read()
        if (inputByte < 0) {
            error("unexpected end of input")
        }
        registers[c] = inputByte
    }
}

private data class LoadProgram(val b: Int, val c: Int) : Operator {
    override fun execute(registers: Registers, memory: Memory, finger: Int): Int {
        val address = registers[b]
        if (address != 0) { // no need for a copy in that case, it's just for the finger move
            memory.loadProgramFrom(address = address)
        }
        return registers[c] // new finger
    }
}

private data class Orthography(val a: Int, val data: Int) : Operator {
    override fun execute(registers: Registers, memory: Memory, finger: Int): Int {
        registers[a] = data
        return finger + 1
    }
}
