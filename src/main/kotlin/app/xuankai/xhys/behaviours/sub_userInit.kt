package app.xuankai.xhys

import net.mamoe.mirai.event.subscribeMessages
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.Users
import net.mamoe.mirai.event.EventPriority

fun XhysMiraiBot.userInit(){
    apply {
        miraiBot.eventChannel.subscribeMessages(priority = EventPriority.HIGHEST) {
            (startsWith(".") or startsWith("。")) {
                val qqId = source.fromId
                if(!registeredqqId.contains(qqId)){
                    val result = DataMysql.query<Users>("select * from users where qqId=${qqId}")
                    if(result.isEmpty()){
                        DataMysql.executeSql("insert into users(qqId, money) values(${qqId}, 0)")
                    }
                    registeredqqId.add((qqId))
                }
            }
        }
    }
}