import app.xuankai.xhys.quartz.job.PrintJobRunner
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.util.*

@DelicateCoroutinesApi
fun main() = runBlocking {
    //PrintJobRunner.run()
//    PrintJobRunner.runCoroutine()
//    launch {
//        while (true) {
//            println("[quartz job] another job")
//            delay(5000L)
//        }
//    }
//    delay(26000L)
    println("hello,now is ${LocalTime.now()}")
    repeat(1000) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, 1)
        val time = calendar.time
        val timer = Timer()
        timer.schedule(MyTask(), time)
    }
}

class MyTask : TimerTask() {
    override fun run() {
        println("hello,now is ${LocalTime.now()}")
        cancel()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, 1)
        val time = calendar.time
        val timer = Timer()
        timer.schedule(MyTask(), time)
    }

}