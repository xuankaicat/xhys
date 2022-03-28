package com.github.xuankaicat.xhys.behaviours

import com.github.xuankaicat.xhys.XhysMiraiBot
import com.github.xuankaicat.xhys.behaviours.Eat.eat
import com.github.xuankaicat.xhys.core.IXhysBot
import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import com.github.xuankaicat.xhys.mysql.model.User
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.event.EventPriority

@Behaviour
fun IXhysBot.initExistingUsers(){
    this as XhysMiraiBot
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