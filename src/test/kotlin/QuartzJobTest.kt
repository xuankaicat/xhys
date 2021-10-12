import app.xuankai.xhys.quartz.job.PrintJobRunner
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@DelicateCoroutinesApi
fun main() = runBlocking {
    //PrintJobRunner.run()
    PrintJobRunner.runCoroutine()
    launch {
        while (true) {
            println("[quartz job] another job")
            delay(5000L)
        }
    }
    delay(26000L)
}