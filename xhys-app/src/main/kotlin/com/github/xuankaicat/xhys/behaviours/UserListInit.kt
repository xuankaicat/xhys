package com.github.xuankaicat.xhys.behaviours

import com.github.xuankaicat.xhys.XhysMiraiBot
import com.github.xuankaicat.xhys.core.IXhysBot
import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import com.github.xuankaicat.xhys.model.User
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.event.EventPriority

@Behaviour
fun IXhysBot.userListInit(){
    this as XhysMiraiBot
    miraiBot.eventChannel.subscribeMessages(priority = EventPriority.HIGHEST) {
        (startsWith(".") or startsWith("。")) {
            val qqId = source.fromId
            if(!registeredQQid.contains(qqId)){
                if(!User.exist(qqId))
                    User.insert(qqId)
                registeredQQid.add(qqId)
            }
        }
    }
}