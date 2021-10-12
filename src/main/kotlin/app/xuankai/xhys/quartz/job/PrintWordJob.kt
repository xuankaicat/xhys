package app.xuankai.xhys.quartz.job

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

class PrintJobRunner {
    companion object {
        @DelicateCoroutinesApi
        fun runCoroutine() {
            GlobalScope.launch {
                while (true) {
                    val time = LocalTime.now()
                    println("[quartz job] at $time")
                    delay(5000L)
                }
            }
        }
    }
}