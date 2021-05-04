package app.xuankai.xhys

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages

fun XhysMiraiBot.baseReply(){
    apply {
        miraiBot.eventChannel.subscribeMessages {
            "小黄勇士power！" reply { powerReply(this) }
            "小黄勇士power!" reply { powerReply(this) }
        }
    }
}

fun powerReply(message : MessageEvent): String{
    message.apply {
        when(subject){
            is Group-> return " ${(sender as Member).nameCard}power!"
            else-> return " ${sender.nick}power!"
        }
    }
}