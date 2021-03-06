package com.github.xuankaicat.xhys.behaviours

import com.github.xuankaicat.xhys.core.IXhysBot
import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages

@Behaviour
fun IXhysBot.baseReply(){
    miraiBot.eventChannel.subscribeMessages {
        "小黄勇士power！" reply { powerReply(this) }
        "小黄勇士power!" reply { powerReply(this) }
    }
}

fun powerReply(message : MessageEvent): String{
    message.apply {
        return when(subject){
            is Group-> " ${(sender as Member).nameCard}power!"
            else-> " ${sender.nick}power!"
        }
    }
}