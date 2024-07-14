@file:OptIn(ExperimentalStdlibApi::class)

package org.hildan.boundvariable.asm

data class Program(val instructions: List<Operator>) {

    companion object {
        fun disassembleFrom(program: IntArray): Program = Program(instructions = program.map { Operator.decode(it) })
    }
}

@JvmInline
value class Register(val id: Int) {
    init {
        require(id in 0..7) { "a register ID must fit in 3 bits (max 7), got $id" }
    }

    override fun toString(): String = "R$id"

    companion object {
        val ZERO = Register(0)
    }
}

sealed interface Operator {
    fun encode(): Int

    fun encodeAsHex(): String = "0x" + encode().toHexString().padStart(8, '0')

    fun pretty(): String

    companion object {
        internal fun decode(instruction: Int): Operator {
            val a = Register((instruction ushr 6) and 0x7)
            val b = Register((instruction ushr 3) and 0x7)
            val c = Register(instruction and 0x7)
            val code = instruction ushr 28
            return when (code) {
                0x0 -> ConditionalMove(dest = a, src = b, condition = c)
                0x1 -> ArrayIndex(dest = a, arr = b, offset = c)
                0x2 -> ArrayAmendment(arr = a, offset = b, value = c)
                0x3 -> Addition(dest = a, op1 = b, op2 = c)
                0x4 -> Multiplication(dest = a, op1 = b, op2 = c)
                0x5 -> Division(dest = a, op1 = b, op2 = c)
                0x6 -> NotAnd(dest = a, op1 = b, op2 = c)
                0x7 -> Halt
                0x8 -> Allocation(addrDest = b, capacity = c)
                0x9 -> Abandonment(arr = c)
                0xA -> Output(value = c)
                0xB -> Input(dest = c)
                0xC -> LoadProgram(arr = b, newFinger = c)
                0xD -> Orthography(dest = Register((instruction shr 25) and 0x7), value = instruction and 0x1FFFFFF)
                else -> JustData(data = instruction) // might never be executed, so it might not be a problem
            }
        }
    }
}

data class ConditionalMove(val dest: Register, val src: Register, val condition: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0x0, a = dest, b = src, c = condition)

    override fun pretty(): String = if (dest == src) "NOOP" else "if ($condition != 0) $dest <- $src"
}

data class ArrayIndex(val dest: Register, val arr: Register, val offset: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0x1, a = dest, b = arr, c = offset)

    override fun pretty(): String = "$dest <- mem[$arr][$offset]"
}

data class ArrayAmendment(val arr: Register, val offset: Register, val value: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0x2, a = arr, b = offset, c = value)

    override fun pretty(): String = "mem[$arr][$offset] <- $value"
}

data class Addition(val dest: Register, val op1: Register, val op2: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0x3, a = dest, b = op1, c = op2)

    override fun pretty(): String = "$dest <- $op1 + $op2"
}

data class Multiplication(val dest: Register, val op1: Register, val op2: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0x4, a = dest, b = op1, c = op2)

    override fun pretty(): String = "$dest <- $op1 * $op2"
}

data class Division(val dest: Register, val op1: Register, val op2: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0x5, a = dest, b = op1, c = op2)

    override fun pretty(): String = "$dest <- $op1 / $op2"
}

data class NotAnd(val dest: Register, val op1: Register, val op2: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0x6, a = dest, b = op1, c = op2)

    override fun pretty(): String = "$dest <- ~($op1 & $op2)"
}

data object Halt : Operator {
    override fun encode(): Int = encodeInstruction(code = 0x7, data = 0)

    override fun pretty(): String = "=== HALT ==="
}

data class Allocation(val addrDest: Register, val capacity: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0x8, a = Register.ZERO, b = addrDest, c = capacity)

    override fun pretty(): String = "$addrDest <- @allocate(size = $capacity)"
}

data class Abandonment(val arr: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0x9, a = Register.ZERO, b = Register.ZERO, c = arr)

    override fun pretty(): String = "free(mem[$arr])"
}

data class Output(val value: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0xA, a = Register.ZERO, b = Register.ZERO, c = value)

    override fun pretty(): String = "print($value)"
}

data class Input(val dest: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0xB, a = Register.ZERO, b = Register.ZERO, c = dest)

    override fun pretty(): String = "$dest <- read()"
}

data class LoadProgram(val arr: Register, val newFinger: Register) : Operator {
    override fun encode(): Int = encodeStdInstruction(code = 0xC, a = Register.ZERO, b = arr, c = newFinger)

    override fun pretty(): String = "loadProgram(mem[$arr]); finger <- $newFinger"
}

data class Orthography(val dest: Register, val value: Int) : Operator {
    init {
        require(value in 0..0x1FFFFFF) { "value must fit in 25bit (max 0x1FFFFFF), got 0x${value.toHexString()}" }
    }

    override fun encode(): Int = encodeInstruction(code = 0xD, data = (dest.id shl 25) + value)

    override fun pretty(): String {
        val prettyCharValue = when (value) {
            in 0..255 -> when (val char = value.toChar()) {
                '\n' -> " '\\n'"
                '\t' -> " '\\t'"
                '\r' -> " '\\r'"
                '\b' -> " '\\b'"
                in '\u0020'..'\u00FF' -> " '$char'"
                else -> ""
            }
            else -> ""
        }
        return "$dest <- $value$prettyCharValue"
    }
}

/**
 * This is used when disassembling invalid instruction codes, which generally means this piece of the program is never
 * actually run, and is probably just used as data for something else (like directly reading from the program array).
 *
 * When such instruction is actually run on the UM, it obviously fails because of the invalid code, but this is just for
 * the disassembler.
 */
internal data class JustData(val data: Int) : Operator {
    override fun encode(): Int = data

    override fun pretty(): String = "DATA? 0x${data.toHexString()}"
}

private fun encodeInstruction(code: Int, data: Int): Int {
    require(code in 0..13) { "'code' must be in the 0..13 range, got $code" }
    require(data in 0..0xFFFFFFF) { "'data' must fit in 28 bits (max 0xFFFFFFF), got 0x${data.toHexString()}" }
    return (code shl 28) + data
}

private fun encodeStdInstruction(code: Int, a: Register, b: Register, c: Register): Int =
    encodeInstruction(code, (a.id shl 6) + (b.id shl 3) + c.id)
