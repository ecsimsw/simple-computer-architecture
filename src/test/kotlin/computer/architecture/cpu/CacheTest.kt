package computer.architecture.cpu

import computer.architecture.component.Memory
import computer.architecture.cpu.cache.*
import computer.architecture.cpu.cache.replacement.LruReplacementStrategy
import computer.architecture.cpu.cache.replacement.LruSecondChanceReplacementStrategy
import computer.architecture.cpu.cache.replacement.RandomReplacementStrategy
import computer.architecture.cpu.cu.ForwardingPipelineControlUnit
import computer.architecture.cpu.pc.TwoLevelLocalHistoryPredictionPcUnit
import computer.architecture.cpu.utils.Utils.Companion.checkProcessResult
import computer.architecture.utils.Logger
import computer.architecture.utils.LoggingSignal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CacheTest {

    @BeforeEach
    fun initLogger() {
        Logger.loggingSignal = LoggingSignal(result = true)
        Logger.init()
    }

    private val pcUnit = TwoLevelLocalHistoryPredictionPcUnit()
    private val replacementStrategy = LruReplacementStrategy()

    @DisplayName("FullyAssociativeMapped, SetAssociativeMapped, DirectMapped cache 성능을 비교한다.")
    @Nested
    inner class Cache {

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBackFullyAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackFullyAssociativeMappedCache(memory, 4, 8, replacementStrategy)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBack2waySetAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 7, 1, replacementStrategy)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBackDirectMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackDirectMappedCache(memory, 4, 8, replacementStrategy)
            testResult(cache, expected)
        }
    }

    @DisplayName("WriteThrough와 WriteBack의 MemoryWrite 횟수를 비교한다.")
    @Nested
    inner class WritePolicyTest {

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeThrough2waySetAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteThroughSetAssociativeMappedCache(memory, 4, 7, 1, replacementStrategy)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBack2waySetAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 7, 1, replacementStrategy)
            testResult(cache, expected)
        }
    }

    @DisplayName("Set 수에 변화에 따른 성능 변화를 확인한다. (2,4,16,32,128,256)")
    @Nested
    inner class SetWayTest {

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBack2WaySetAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 7, 1, replacementStrategy)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBack4WaySetAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 6, 2, replacementStrategy)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBack16WaySetAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 4, 4, replacementStrategy)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBack32WaySetAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 3, 5, replacementStrategy)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBack128WaySetAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 3, 5, replacementStrategy)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun writeBack256WaySetAssociativeMappedCache(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 2, 6, replacementStrategy)
            testResult(cache, expected)
        }
    }

    @DisplayName("교체 알고리즘 성능을 비교한다. (LRU, LRU_SecondChance, Random)")
    @Nested
    inner class ReplacementTest {

        private val random = RandomReplacementStrategy()
        private val lru = LruReplacementStrategy()
        private val lruSecondChance = LruSecondChanceReplacementStrategy()

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun random4way(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 2, 6, random)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun lru4way(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 2, 6, lru)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun lru_secondChance_4way(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 2, 6, lruSecondChance)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun random256way(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 2, 6, random)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun lru256way(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 2, 6, lru)
            testResult(cache, expected)
        }

        @ParameterizedTest
        @CsvSource(
            "sample/simple.bin,0",
            "sample/simple2.bin,100",
            "sample/simple3.bin,5050",
            "sample/simple4.bin,55",
            "sample/gcd.bin,1",
            "sample/fib.bin,55",
            "sample/input4.bin,85"
        )
        fun lru_secondChance_256way(path: String, expected: Int) {
            val memory = Memory.load(20000000, path)
            val cache = WriteBackSetAssociativeMappedCache(memory, 4, 2, 6, lruSecondChance)
            testResult(cache, expected)
        }
    }

    private fun testResult(
        cache: ICache,
        expected: Int
    ) {
        val controlUnit = ForwardingPipelineControlUnit(cache, pcUnit)
        val processResult = controlUnit.process()
        checkProcessResult(processResult[0], expected)
    }
}
