package org.hildan.boundvariable.asm

import org.hildan.boundvariable.um.*
import kotlin.io.path.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("no programs to disassemble")
        return
    }
    args.forEach { filename ->
        val programPath = Path(filename)
        println("Disassembling program: $programPath")
        val programBytes = programPath.readBytes()

        val assembly = Program.disassembleFrom(programBytes.concatBytesToIntsBE())
        val asmPath = programPath.resolveSibling(programPath.nameWithoutExtension + ".asm")
        val lines = assembly.instructions.mapIndexed { i, instruction ->
            "${i.toString().padStart(4, ' ')}\t${instruction.encodeAsHex()}\t${instruction.pretty()}"
        }
        asmPath.writeLines(lines)
    }
}
