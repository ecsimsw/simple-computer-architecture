package computer.architecture.cpu.cu

import computer.architecture.cpu.ControlSignal
import computer.architecture.cpu.DataDependencyUnit
import computer.architecture.cpu.cache.ICache
import computer.architecture.cpu.dto.CycleResult
import computer.architecture.cpu.pc.IProgramCounterUnit
import computer.architecture.cpu.pc.NonePredictionPcUnit
import computer.architecture.utils.Logger

class StallingPipelineControlUnit(
    cache: ICache,
    private val pcUnit: IProgramCounterUnit = NonePredictionPcUnit()
) : SingleProcessingPipelineControlUnit(cache) {
    private val dataDependencyUnit = DataDependencyUnit(registers.size)

    override fun cycleExecution(valid: Boolean, pc: Int): CycleResult {
        val nextIfId = fetch(valid, pc)
        val nextIdEx = decode(latches.ifId())

        if (nextIdEx.valid && dataDependencyUnit.hasHazard(nextIdEx.readReg1, nextIdEx.readReg2)) {
            nextIfId.valid = false
            nextIdEx.valid = false
            nextIdEx.controlSignal = ControlSignal.NONE
            stallUnit.sleep(2, nextIdEx.pc)
        }

        if (nextIdEx.valid && nextIdEx.controlSignal.regWrite) {
            dataDependencyUnit.book(nextIdEx)
        }

        val nextExMa = execute(latches.idEx())
        val nextMaWb = memoryAccess(latches.exMa())
        val wbResult = writeBack(latches.maWb())
        dataDependencyUnit.release(wbResult)

        val nextPc = pcUnit.findNext(pc, nextIfId, nextIdEx, nextExMa)

        latches.store(nextIfId)
        latches.store(nextIdEx)
        latches.store(nextExMa)
        latches.store(nextMaWb)
        latches.flushAll()
        Logger.log(nextIfId, nextIdEx, nextExMa, nextMaWb, wbResult)

        return CycleResult(
            nextPc = nextPc,
            value = registers[2],
            valid = wbResult.valid,
            isEnd = nextPc == -1,
            lastCycle = wbResult.controlSignal.isEnd
        )
    }
}
