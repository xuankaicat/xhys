package app.xuankai.xhys

import net.mamoe.mirai.Bot

object XhysMiraiBot {
    var id : Long = 0L
    var repeatValue = 100
    val lastMsg = HashMap<Long, String>()
    var eatTimer = 0

    val registeredqqId = ArrayList<Long>()
    var foodBlackList = ArrayList<String>()

    lateinit var miraiBot : Bot
}