package kweb.memory

import com.ibm.icu.text.DecimalFormat
import kotlinx.coroutines.*
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.plugins.fomanticUI.fomanticUIPlugin
import kweb.state.KVar
import kweb.state.render
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MemoryTestSever(private val port: Int = 8080) : AutoCloseable {

    private val scope = CoroutineScope(context = Dispatchers.IO)
    private lateinit var server: Kweb
    private lateinit var webServerMemoryLoadJob: Job
    private lateinit var randomDataJob: Job

    suspend fun start() {
        val runtime = Runtime.getRuntime()

        var counter = 0
        val randomDataList = (0..100).map { KVar(initialValue = "") }
        randomDataJob = scope.launch {
            while (isActive) {
                randomDataList.forEach { randomData ->
                    randomData.value = Random.nextDouble(
                        from = -Double.MAX_VALUE,
                        until = Double.MAX_VALUE
                    ).toString()
                }
                if (counter % 10 == 0)
                    MemoryLoad(
                        max = runtime.maxMemory(),
                        free = runtime.freeMemory(),
                        total = runtime.totalMemory()
                    ).printMemoryLoad()

                System.gc()
                delay(duration = 100.milliseconds)
            }
        }

        server = Kweb(
            port = port,
            debug = false,
            plugins = listOf(fomanticUIPlugin)
        ) {
            doc.body {
                route {
                    path(template = "/") {
                        div(attributes = fomantic.field) {
                            label().addText(value = "Random Double")
                            randomDataList.forEach { randomData ->
                                div {
                                    val memoryLeak = true
                                    render(randomData) { randomData: String ->

                                        if(memoryLeak) {
                                            /* With memory Leak */
                                            input(type = InputType.text) { element ->
                                                element.setReadOnly(true)
                                            }
                                        } else {
                                            /* without memory leak */
                                            label().text(randomData)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun close() {
        webServerMemoryLoadJob.cancel()
        randomDataJob.cancel()
        scope.cancel()
        server.close()
    }
}


data class MemoryLoad(
    val max: Long = 0L,
    val free: Long = 0L,
    val total: Long = 0L
) {
    companion object {
        val format = DecimalFormat("0.00")
        private val MEGA = (1024 * 1024).toLong()
    }

    val used: Long by lazy { total - free }

    fun printMemoryLoad() {
        println(
            "Memory Load: ${format.format(max / MEGA)}MB max, ${format.format(free / MEGA)}MB free, ${
                format.format(
                    total / MEGA
                )
            }MB total, ${format.format(used / MEGA)}MB used"
        )
    }
}


fun main() = runBlocking {
    MemoryTestSever().use { server ->
        server.start()
        delay(duration = Int.MAX_VALUE.seconds)
    }
}