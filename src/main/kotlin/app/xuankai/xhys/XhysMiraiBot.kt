package app.xuankai.xhys

import app.xuankai.xhys.mysql.model.Group
import net.mamoe.mirai.Bot

object XhysMiraiBot {
    val lastMsg = HashMap<Long, String>()
    var eatTimer = 0

    val registeredqqId = ArrayList<Long>()
    lateinit var groupList : ArrayList<Group>
    var foodBlackList = ArrayList<String>()

    lateinit var miraiBot : Bot
}