package org.hildan.boundvariable.um

import kotlin.test.*

class MemoryTest {

    @Test
    fun canReadInitialProgram_zeroes() {
        val initialProgram = IntArray(0)
        val memory = Memory(initialProgram)
        assertContentEquals(initialProgram, memory[0])
    }

    @Test
    fun canReadInitialProgram_dataIsIndices() {
        val initialProgram = IntArray(0) {it: Int -> it }
        val memory = Memory(initialProgram)
        assertContentEquals(initialProgram, memory[0])
    }

    @Test
    fun alloc_empty() {
        val memory = Memory(IntArray(0))
        val address = memory.alloc(0)
        assertContentEquals(IntArray(0), memory[address])
    }

    @Test
    fun alloc_nonEmpty() {
        val memory = Memory(IntArray(0))
        val address = memory.alloc(4)
        assertContentEquals(IntArray(4), memory[address])

        memory[address][0] = 42
        memory[address][1] = 43
        memory[address][2] = 44
        memory[address][3] = 45
        assertEquals(42, memory[address][0])
        assertEquals(43, memory[address][1])
        assertEquals(44, memory[address][2])
        assertEquals(45, memory[address][3])

        assertFails { memory[address][4] }
    }

    @Test
    fun allocAndFree_single() {
        val memory = Memory(IntArray(0))
        val address = memory.alloc(4)
        assertNotEquals(illegal = 0, address)
        assertContentEquals(IntArray(4), memory[address])

        memory.free(address)
        assertFails { memory[address] }
        assertFails { memory.free(address) }
    }

    @Test
    fun allocAndFree_multiple() {
        val memory = Memory(IntArray(0))
        val address1 = memory.alloc(1)
        val address2 = memory.alloc(1)
        val address3 = memory.alloc(1)
        assertNotEquals(address1, address2)
        assertNotEquals(address1, address3)
        assertNotEquals(address2, address3)
        assertContentEquals(IntArray(1), memory[address1])
        assertContentEquals(IntArray(1), memory[address2])
        assertContentEquals(IntArray(1), memory[address3])

        memory[address1][0] = 42
        memory[address2][0] = 2048
        assertContentEquals(IntArray(1) { 42 }, memory[address1])
        assertContentEquals(IntArray(1) { 2048 }, memory[address2])
        assertContentEquals(IntArray(1) { 0 }, memory[address3])

        memory.free(address2)
        assertFails { memory[address2] }
        assertFails { memory.free(address2) }
        assertContentEquals(IntArray(1) { 42 }, memory[address1])
        assertContentEquals(IntArray(1) { 0 }, memory[address3])
    }

    @Test
    fun freeZero_fails() {
        val memory = Memory(IntArray(0))
        assertFails { memory.free(0) }
    }

    @Test
    fun freeUnknownAddress_fails() {
        val memory = Memory(IntArray(0))
        assertFails { memory.free(0) }
    }

    @Test
    fun loadProgramFrom() {
        val memory = Memory(IntArray(0))
        val address = memory.alloc(4)

        for (i in 0..<4) {
            memory[address][i] = i * i
        }
        assertContentEquals(IntArray(0), memory[0])
        assertContentEquals(IntArray(4) { it * it }, memory[address])

        memory.loadProgramFrom(address)
        assertContentEquals(IntArray(4) { it * it }, memory[0])
        assertContentEquals(IntArray(4) { it * it }, memory[address])

        memory[address][0] = 42
        memory[address][1] = 2048
        assertContentEquals(IntArray(4) { it * it }, memory[0])
        assertContentEquals(intArrayOf(42, 2048, 4, 9), memory[address])
    }
}
