package org.hildan.boundvariable.um

import java.io.*
import kotlin.io.path.*
import kotlin.test.*

class UniversalMachineTest {

    @Test
    fun testSandmark() {
        val sandmarkProgram = Path("../material/sandmark.umz").readBytes()
        val sandmarkExpectedOutput = Path("../material/sandmark-expected-output.txt").readText()

        val outputStream = ByteArrayOutputStream()
        UniversalMachine.run(program = sandmarkProgram, stdout = outputStream)

        assertEquals(sandmarkExpectedOutput, outputStream.toString(Charsets.US_ASCII))
    }
}
