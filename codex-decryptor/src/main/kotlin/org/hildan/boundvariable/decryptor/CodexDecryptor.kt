package org.hildan.boundvariable.decryptor

import org.hildan.boundvariable.um.*
import java.io.*
import kotlin.io.path.*

/**
 * This runs the UM on the encrypted codex with the encryption key, to dump the codex contents to `codex-dump.um`.
 */
fun main() {
    val codexProgram = Path("material/codex.umz").readBytes()
    val codexKey = Path("material/codex-decryption-key.txt").readText()

    val codexDump = Path("solution/umix.um").createParentDirectories()
    val introTextLength = 195

    println("Running the codex on the Universal Machine...")
    codexDump.outputStream().use { codexDumpFileStream ->
        UniversalMachine.run(
            program = codexProgram,
            stdin = ByteArrayInputStream("$codexKey\np".encodeToByteArray()), // 'p' to choose "dump UM data"
            stdout = SwitchStream(
                nBytesBeforeSwitch = introTextLength,
                outputStream1 = System.out, // we print the introduction to the console
                outputStream2 = codexDumpFileStream,
            ),
        )
    }
    println("<redirected to '$codexDump'>")
}
