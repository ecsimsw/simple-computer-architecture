package computer.architecture.cpu.cu

import computer.architecture.component.And.Companion.and
import computer.architecture.component.Latches
import computer.architecture.component.Memory
import computer.architecture.component.Mux.Companion.mux
import computer.architecture.component.Or.Companion.or
import computer.architecture.cpu.*
import computer.architecture.cpu.register.Registers
import computer.architecture.utils.Logger

class ControlUnit(
    private val memory: Memory,
    private val logger: Logger
) : ControlUnitInterface {
    private val registers = Registers(32)
    private val decodeUnit = DecodeUnit()
    private val alu = ALUnit()
    private val stallUnit = StallUnit()
    private val forwardingUnit = ForwardingUnit()
    private val latches = Latches()

    override fun process(): Int {
        var cycle = 0
        var validCycle = 0

        var cycleResult = CycleResult()
        var isEnd = false

        while (true) {
            logger.printCycle(cycleResult.valid, validCycle)

            isEnd = or(isEnd, cycleResult.lastInstruction)
            val pc = mux(stallUnit.isMelt, stallUnit.freezePc, cycleResult.nextPc)
            val valid = stallUnit.valid && !isEnd

            cycleResult = cycleExecution(valid, pc)

            if (cycleResult.lastCycle) {
                return cycleResult.value
            }

            if (cycleResult.valid) {
                validCycle++
            }

            latches.flushAll()
            stallUnit.next()
            cycle++
        }
    }

    private fun cycleExecution(valid: Boolean, pc: Int): CycleResult {
        val prevIfId = latches.ifId()
        val prevIdEx = latches.idEx()
        val prevExMa = latches.exMa()
        val prevMaWb = latches.maWb()

        val wbResult = writeBack(prevMaWb)
        val nextMaWb = memoryAccess(prevExMa)

        forwardingUnit.execute(prevIdEx, prevExMa, prevMaWb)
        val nextExMa = execute(prevIdEx)

        val nextIdEx = decode(prevIfId)
        val nextIfId = fetch(valid, pc)

        var isEnd = false
        if (nextExMa.valid && nextExMa.branch) {
            nextIfId.valid = false
            nextIdEx.valid = false
            if (nextExMa.nextPc == -1) {
                nextExMa.controlSignal.isEnd = true
                isEnd = true
            }
        }

        if (nextIdEx.valid && nextIdEx.jump) {
            nextIfId.valid = false
            if (nextIdEx.nextPc == -1) {
                nextIdEx.controlSignal.isEnd = true
                isEnd = true
            }
        }

        var nextPc = mux(nextExMa.branch, nextExMa.nextPc, pc + 4)
        nextPc = mux(nextIdEx.jump, nextIdEx.nextPc, nextPc)

        latches.store(nextIfId)
        latches.store(nextIdEx)
        latches.store(nextExMa)
        latches.store(nextMaWb)
        logger.log(nextIfId, nextIdEx, nextExMa, nextMaWb, wbResult)

        return CycleResult(
            nextPc = nextPc,
            value = registers[2],
            valid = wbResult.valid,
            lastInstruction = isEnd,
            lastCycle = wbResult.controlSignal.isEnd
        )
    }

    private fun fetch(valid: Boolean, pc: Int): FetchResult {
        if (!valid) {
            return FetchResult(valid, 0, 0)
        }
        val instruction = memory.read(pc)
        return FetchResult(
            valid = valid && (instruction != 0),
            pc = pc,
            instruction = instruction
        )
    }

    private fun decode(ifResult: FetchResult): DecodeResult {
        if (!ifResult.valid) {
            return DecodeResult()
        }

        val instruction = decodeUnit.parse(ifResult.pc + 4, ifResult.instruction)
        val controlSignal = decodeUnit.controlSignal(ifResult.valid, instruction.opcode)

        val readData1 = registers[instruction.rs]
        val readData2 = registers[instruction.rt]

        var writeRegister = mux(controlSignal.regDest, instruction.rd, instruction.rt)
        writeRegister = mux(controlSignal.jal, 31, writeRegister)

        var nextPc = mux(controlSignal.jump, instruction.address, ifResult.pc)
        nextPc = mux(controlSignal.jr, readData1, nextPc)

        return DecodeResult(
            valid = ifResult.valid,
            pc = ifResult.pc,
            shiftAmt = instruction.shiftAmt,
            immediate = instruction.immediate,
            address = instruction.address,
            readReg1 = instruction.rs,
            readReg2 = instruction.rt,
            readData1 = readData1,
            readData2 = readData2,
            writeReg = writeRegister,
            jump = controlSignal.jump || controlSignal.jr,
            nextPc = nextPc,
            controlSignal = controlSignal
        )
    }

    private fun execute(idResult: DecodeResult): ExecutionResult {
        if (!idResult.valid) {
            return ExecutionResult()
        }

        val controlSignal = idResult.controlSignal
        val aluValue = alu.execute(idResult)

        val branchCondition = and(aluValue == 1, controlSignal.branch)
        val nextPc = mux(branchCondition, idResult.immediate, idResult.pc)

        return ExecutionResult(
            valid = idResult.valid,
            pc = idResult.pc, // TODO :: only for logging
            readData2 = idResult.readData2,
            writeReg = idResult.writeReg,
            aluValue = aluValue,
            nextPc = nextPc,
            branch = branchCondition,
            controlSignal = controlSignal
        )
    }

    private fun memoryAccess(exResult: ExecutionResult): MemoryAccessResult {
        if (!exResult.valid) {
            return MemoryAccessResult()
        }

        val controlSignal = exResult.controlSignal
        val memReadValue = memory.read(
            memRead = controlSignal.memRead,
            address = exResult.aluValue,
        )

        memory.write(
            memWrite = controlSignal.memWrite,
            address = exResult.aluValue,
            value = exResult.readData2
        )

        val regWriteValue = mux(controlSignal.memToReg, memReadValue, exResult.aluValue)

        return MemoryAccessResult(
            valid = exResult.valid,
            pc = exResult.pc, // TODO :: only for logging
            regWriteValue = regWriteValue,
            writeReg = exResult.writeReg,
            controlSignal = controlSignal
        )
    }

    private fun writeBack(maResult: MemoryAccessResult): WriteBackResult {
        if (!maResult.valid) {
            return WriteBackResult()
        }

        if (maResult.controlSignal.regWrite) {
            registers.write(
                register = maResult.writeReg,
                data = maResult.regWriteValue,
            )
        }

        return WriteBackResult(
            valid = maResult.valid,
            pc = maResult.pc, // TODO :: only for logging
            regWriteValue = maResult.regWriteValue,
            writeReg = maResult.writeReg,
            controlSignal = maResult.controlSignal
        )
    }
}
