package com.github.xuankaicat.xhys.behaviours

import com.github.xuankaicat.xhys.XhysMiraiBot
import com.github.xuankaicat.xhys.core.IXhysBot
import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import com.github.xuankaicat.xhys.model.Group
import net.mamoe.mirai.event.events.BotJoinGroupEvent

@Behaviour
fun IXhysBot.initExistingGroup(){
    this as XhysMiraiBot
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