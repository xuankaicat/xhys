package app.xuankai.xhys.behaviours

import app.xuankai.xhys.XhysMiraiBot
import net.mamoe.mirai.event.subscribeMessages
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.model.User
import net.mamoe.mirai.event.EventPriority

fun XhysMiraiBot.initExistingUsers(){
    apply {
        miraiBot.eventChannel.subscribeMessages(priority = EventPriority.HIGHEST) {
            (startsWith(".") or startsWith("。")) {
                val qqId = source.fromId
                if(!registeredqqId.contains(qqId)){
                    if(!User.exist(qqId))
                        User.insert(qqId)
                    registeredqqId.add(qqId)
                }
            }
        }
    }
}