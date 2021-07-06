package app.xuankai.xhys.behaviours

import app.xuankai.xhys.XhysMiraiBot
import app.xuankai.xhys.mysql.model.Group
import net.mamoe.mirai.event.events.BotJoinGroupEvent

fun XhysMiraiBot.initExistingGroup(){
    apply {
        miraiBot.eventChannel.subscribeAlways<BotJoinGroupEvent> {
            //检查数据库中是否已经有该群
            if(!Group.isGroupExisted(groupId)) {
                //没有则初始化群信息
                Group.addInitGroup(groupId)
            }
        }
    }
}