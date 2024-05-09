package org.hildan.boundvariable.um

import org.junit.jupiter.api.BeforeEach
import java.io.*
import kotlin.test.*

class OperatorsTest {

    private lateinit var registers: Registers
    private lateinit var memory: Memory
    private var finger: Int = 0

    @BeforeEach
    fun setup() {
        val initialProgram = IntArray(4) { it * it }
        registers = Registers(UniversalMachine.N_REGISTERS)
        memory = Memory(initialProgram)
        finger = 0
    }

    private fun Operator.executeOnCurrentState(): Int = execute(registers, memory, finger).also { finger = it }

    @Test
    fun conditionalMove() {
        val op = Operator.decode(0b0000_0000000000000000000_000_001_010u.toInt())
        registers[0] = 2048
        registers[1] = 42
        registers[2] = 0

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertEquals(2048, registers[0])

        registers[2] = 1

        op.executeOnCurrentState()
        assertEquals(2, finger)
        assertEquals(42, registers[0])
    }

    @Test
    fun arrayIndex() {
        val op = Operator.decode(0b0001_0000000000000000000_000_001_010u.toInt())
        registers[0] = 2048
        registers[1] = 0
        registers[2] = 1

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertEquals(1, registers[0])

        registers[2] = 2

        op.executeOnCurrentState()
        assertEquals(2, finger)
        assertEquals(4, registers[0])

        registers[2] = 3

        op.executeOnCurrentState()
        assertEquals(3, finger)
        assertEquals(9, registers[0])
    }

    @Test
    fun arrayAmendment() {
        val op = Operator.decode(0b0010_0000000000000000000_110_101_010u.toInt())

        val address = memory.alloc(3)

        registers[6] = address
        registers[5] = 2
        registers[2] = 42

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertEquals(42, memory[address][2])
    }

    @Test
    fun addition() {
        val op = Operator.decode(0b0011_0000000000000000000_110_101_100u.toInt())
        registers[4] = 15
        registers[5] = 27
        registers[6] = 0

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertEquals(15 + 27, registers[6])

        registers[4] = Int.MAX_VALUE
        registers[5] = 1
        op.executeOnCurrentState()

        assertEquals((Int.MAX_VALUE.toUInt() + 1u).toInt(), registers[6])
    }

    @Test
    fun multiplication() {
        val op = Operator.decode(0b0100_0000000000000000000_100_011_111u.toInt())
        registers[7] = 6
        registers[3] = 7
        registers[4] = 0

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertEquals(6 * 7, registers[4])
    }

    @Test
    fun division() {
        val op = Operator.decode(0b0101_0000000000000000000_011_111_110u.toInt())
        registers[6] = 7
        registers[7] = 49
        registers[3] = 0

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertEquals(49 / 7, registers[3])

        registers[6] = 0

        assertFails { op.executeOnCurrentState() }
    }

    @Test
    fun notAnd() {
        val op = Operator.decode(0b0110_0000000000000000000_111_110_010u.toInt())
        registers[2] = 0b1010
        registers[6] = 0b0110
        registers[7] = 0

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertEquals((0b1010 and 0b0110).inv(), registers[7])
    }

    @Test
    fun halt() {
        val op = Operator.decode(0b0111_0000000000000000000000000000u.toInt())
        assertFailsWith<HaltProgram> { op.executeOnCurrentState() }
    }

    @Test
    fun allocation() {
        val op = Operator.decode(0b1000_0000000000000000000000_101_011u.toInt())
        registers[3] = 12
        registers[5] = 0

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertNotEquals(illegal = 0, registers[5])
        assertContentEquals(IntArray(12), memory[registers[5]])

        registers[3] = 4

        op.executeOnCurrentState()
        assertEquals(2, finger)
        assertNotEquals(illegal = 0, registers[5])
        assertContentEquals(IntArray(4), memory[registers[5]])
    }

    @Test
    fun abandonment() {
        val op = Operator.decode(0b1001_0000000000000000000000000_001u.toInt())

        val address = memory.alloc(5)
        registers[1] = address

        assertNotEquals(illegal = 0, address)
        assertContentEquals(IntArray(5), memory[address])

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertFails { memory[address] }
    }

    @Test
    fun output() {
        val op = Operator.decode(0b1010_0000000000000000000000000_000u.toInt())

        registers[0] = 'A'.code

        val standardOut = System.out
        val outputStreamCaptor = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStreamCaptor))

        try {
            op.executeOnCurrentState()
            assertEquals(1, finger)
            assertEquals("A", outputStreamCaptor.toString())
        } finally {
            System.setOut(standardOut)
        }
    }

    @Test
    fun input() {
        val op = Operator.decode(0b1011_0000000000000000000000000_111u.toInt())

        registers[7] = 0

        val standardIn = System.`in`
        val inputStream = ByteArray(1) { 'A'.code.toByte() }.inputStream()
        System.setIn(inputStream)

        try {
            op.executeOnCurrentState()
            assertEquals(1, finger)
            assertEquals('A'.code, registers[7])
        } finally {
            System.setIn(standardIn)
        }
    }

    @Test
    fun loadProgram() {
        val op = Operator.decode(0b1100_0000000000000000000000_110_100u.toInt())

        val address = memory.alloc(9)
        memory[address][0] = 42
        memory[address][3] = 9
        memory[address][4] = 16
        memory[address][5] = 25

        registers[6] = address
        registers[4] = 4

        assertContentEquals(intArrayOf(42, 0, 0, 9, 16, 25, 0, 0, 0), memory[address])

        op.executeOnCurrentState()
        assertEquals(4, finger)
        assertContentEquals(intArrayOf(42, 0, 0, 9, 16, 25, 0, 0, 0), memory[0])
        assertContentEquals(intArrayOf(42, 0, 0, 9, 16, 25, 0, 0, 0), memory[address])

        memory[address][5] = 12
        assertEquals(25, memory[0][5])
        assertEquals(12, memory[address][5])
    }

    @Test
    fun othography() {
        val op = Operator.decode(0b1101_001_1000000000000000000110100u.toInt())

        registers[1] = 42

        op.executeOnCurrentState()
        assertEquals(1, finger)
        assertEquals(0b1000000000000000000110100u.toInt(), registers[1])
    }
}