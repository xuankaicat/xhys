package app.xuankai.xhys.behaviours

import app.xuankai.xhys.XhysMiraiBot
import app.xuankai.xhys.mysql.model.Group
import net.mamoe.mirai.event.events.BotJoinGroupEvent

fun XhysMiraiBot.initExistingGroup(){
    apply {
        //启动时初始化已有群组
        groupList = Group.getAll()

        miraiBot.groups.forEach {
            if(!Group.isGroupExisted(it.id)) {
                val newGroup = Group.addInitGroup(it.id)
                groupList.add(newGroup)
            }
        }

        miraiBot.eventChannel.subscribeAlways<BotJoinGroupEvent> {
            if(!Group.isGroupExisted(groupId)) {
                val newGroup = Group.addInitGroup(groupId)
                groupList.add(newGroup)
            }
        }
    }
}