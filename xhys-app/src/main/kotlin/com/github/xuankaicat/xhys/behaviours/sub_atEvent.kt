package com.github.xuankaicat.xhys.behaviours

import com.github.xuankaicat.xhys.XhysMiraiBot
import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo

@Behaviour
fun XhysMiraiBot.atEvent(){
    apply {
        miraiBot.eventChannel.subscribeMessages {
            atBot {
                val id: Long
                if (subject is Group) {
                    id = source.targetId
                    if(!groupList.first{it.groupId == source.targetId}.ruleObj.responseAtEvent) return@atBot
                } else {
                    id = source.fromId
                }
                when(message[2].toString().trim()){
                    "小黄勇士power！","小黄勇士power!"-> subject.sendMessage(At(sender as Member) + powerReply(this))
                    "滚","爬","sb","爪巴"->{
                        if((subject as Group).botPermission == MemberPermission.ADMINISTRATOR){
                            if((sender as Member).isAdministrator() || (sender as Member).isOwner()){
                                when((1..2).random()){
                                    1->this.javaClass.getResourceAsStream("/是你蝶.jpg")!!.sendAsImageTo(subject)
                                    else->this.javaClass.getResourceAsStream("/哭哭猫.jpg")!!.sendAsImageTo(subject)
                                }
                            }else{
                                (sender as Member).mute(60)
                                subject.sendMessage(At(sender as Member) + " ${message[2]}")
                            }
                        }else{
                            subject.sendMessage(At(sender as Member) + " ${message[2]}")
                        }
                    }
                    else-> subject.sendMessage(At(sender as Member) + " 爬")
                }
                lastMsg.remove(id)
            }
        }
    }
}