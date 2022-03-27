package computer.architecture.memory

import computer.architecture.cpu.Registers

class Results {
    private val logs: MutableList<String> = mutableListOf()

    fun saveLog(instruction: String, registers: Registers) {
        addLine("Inst : $instruction")
        addLine("PC   : ${registers.pc}")
        addLine("REGs : ${registers.r.joinToString(" ")} \n")
    }

    fun printLogs() {
        logs.forEach { println(it) }
    }

    private fun addLine(log: String) {
        logs.add(log)
    }
}
