package org.hildan.boundvariable.asm

import kotlin.test.*

class AssemblyTest {

    private val R0 = Register(0)
    private val R1 = Register(1)
    private val R2 = Register(2)
    private val R3 = Register(3)
    private val R4 = Register(4)
    private val R5 = Register(5)
    private val R6 = Register(6)
    private val R7 = Register(7)

    @Test
    fun decode_conditionalMove() {
        val op = Operator.decode(0b0000_0000000000000000000_010_100_110u.toInt())
        assertEquals(ConditionalMove(dest = R2, src = R4, condition = R6), op)
    }

    @Test
    fun decode_ArrayIndex() {
        val op = Operator.decode(0b0001_0000000000000000000_001_010_011u.toInt())
        assertEquals(ArrayIndex(dest = R1, arr = R2, offset = R3), op)
    }

    @Test
    fun decode_ArrayAmendment() {
        val op = Operator.decode(0b0010_0000000000000000000_010_011_100u.toInt())
        assertEquals(ArrayAmendment(arr = R2, offset = R3, value = R4), op)
    }

    @Test
    fun decode_Addition() {
        val op = Operator.decode(0b0011_0000000000000000000_011_100_101u.toInt())
        assertEquals(Addition(dest = R3, op1 = R4, op2 = R5), op)
    }

    @Test
    fun decode_Multiplication() {
        val op = Operator.decode(0b0100_0000000000000000000_100_101_110u.toInt())
        assertEquals(Multiplication(dest = R4, op1 = R5, op2 = R6), op)
    }

    @Test
    fun decode_Division() {
        val op = Operator.decode(0b0101_0000000000000000000_101_110_111u.toInt())
        assertEquals(Division(dest = R5, op1 = R6, op2 = R7), op)
    }

    @Test
    fun decode_NotAnd() {
        val op = Operator.decode(0b0110_0000000000000000000_110_111_000u.toInt())
        assertEquals(NotAnd(dest = R6, op1 = R7, op2 = R0), op)
    }

    @Test
    fun decode_Halt() {
        val op = Operator.decode(0b0111_0000000000000000000000000000u.toInt())
        assertEquals(Halt, op)
    }

    @Test
    fun decode_Allocation() {
        val op = Operator.decode(0b1000_0000000000000000000000_011_001u.toInt())
        assertEquals(Allocation(addrDest = R3, capacity = R1), op)
    }

    @Test
    fun decode_Abandonment() {
        val op = Operator.decode(0b1001_0000000000000000000000000_110u.toInt())
        assertEquals(Abandonment(arr = R6), op)
    }

    @Test
    fun decode_Output() {
        val op = Operator.decode(0b1010_0000000000000000000000000_100u.toInt())
        assertEquals(Output(value = R4), op)
    }

    @Test
    fun decode_Input() {
        val op = Operator.decode(0b1011_0000000000000000000000000_111u.toInt())
        assertEquals(Input(dest = R7), op)
    }

    @Test
    fun decode_LoadProgram() {
        val op = Operator.decode(0b1100_0000000000000000000000_001_010u.toInt())
        assertEquals(LoadProgram(arr = R1, newFinger = R2), op)
    }

    @Test
    fun decode_Orthography() {
        val op = Operator.decode(0b1101_101_1000001000110001011100010u.toInt())
        assertEquals(Orthography(dest = R5, value = 0b1000001000110001011100010u.toInt()), op)
    }
}