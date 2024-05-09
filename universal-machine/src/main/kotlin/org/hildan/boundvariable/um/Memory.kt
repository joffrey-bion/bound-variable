package org.hildan.boundvariable.um

class Memory(initialProgram: IntArray) {

    private val data = mutableListOf<IntArray?>(initialProgram)
    private val freeAddresses = ArrayDeque<Int>()

    operator fun get(address: Int): IntArray = data[address] ?: error("accessing unallocated address $address")

    fun alloc(capacity: Int): Int {
        val newArray = IntArray(capacity)
        val address = freeAddresses.removeFirstOrNull()
        if (address == null) {
            data.add(newArray)
            return data.lastIndex
        } else {
            data[address] = newArray
            return address
        }
    }

    fun free(address: Int) {
        if (address == 0) {
            error("cannot free special address 0")
        }
        if (data[address] == null || address > data.lastIndex) {
            error("address $address is already unallocated")
        }
        freeAddresses.add(address)
        data[address] = null
    }

    fun loadProgramFrom(address: Int) {
        data[0] = get(address).copyOf()
    }
}
