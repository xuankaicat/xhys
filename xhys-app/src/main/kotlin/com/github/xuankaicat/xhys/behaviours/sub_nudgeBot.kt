package com.github.xuankaicat.xhys.behaviours

import com.github.xuankaicat.xhys.XhysMiraiBot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.NudgeEvent

fun XhysMiraiBot.nudgeBot(){
    apply {
        miraiBot.eventChannel.subscribeAlways<NudgeEvent> {
            if(from != bot && target == bot){
                if(from is Member){
                    from.nudge().sendTo((from as Member).group)
                }else{
                    from.nudge().sendTo(from as Contact)
                }
            }
        }
    }
}