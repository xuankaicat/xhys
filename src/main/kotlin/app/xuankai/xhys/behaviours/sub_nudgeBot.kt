package app.xuankai.xhys

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.MiraiExperimentalApi

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
        //miraiBot.subscribeAlways<MemberNudgedEvent> {
        //    PlainText("${from.nameCard}戳了戳${member.nameCard}").sendTo((from as Member).group)
        //}
    }
}