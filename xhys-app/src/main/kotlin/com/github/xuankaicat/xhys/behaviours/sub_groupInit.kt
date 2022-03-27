package com.github.xuankaicat.xhys.behaviours

import com.github.xuankaicat.xhys.XhysMiraiBot
import com.github.xuankaicat.xhys.mysql.model.Group
import net.mamoe.mirai.event.events.BotJoinGroupEvent

fun XhysMiraiBot.initExistingGroup(){
    apply {
        //启动时初始化已有群组
        groupList = Group.all()

        miraiBot.groups.forEach {
            if(!Group.exist(it.id)) {
                val newGroup = Group.insert(it.id)
                groupList.add(newGroup)
            }
        }

        miraiBot.eventChannel.subscribeAlways<BotJoinGroupEvent> {
            if(!Group.exist(group.id)) {
                val newGroup = Group.insert(group.id)
                groupList.add(newGroup)
            }
        }
    }
}