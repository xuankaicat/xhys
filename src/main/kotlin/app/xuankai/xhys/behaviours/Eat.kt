package app.xuankai.xhys.behaviours

import app.xuankai.xhys.XhysMiraiBot
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.model.User
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo

object Eat {
    fun XhysMiraiBot.eat(){
        apply {
            miraiBot.eventChannel.subscribeMessages {
                always {
                    val id: Long
                    if (subject is Group) {
                        id = source.targetId
                        if(!groupList.first{it.groupId == source.targetId}.ruleObj.responseEatKeyword) return@always
                    } else {
                        id = source.fromId
                    }
                    val msg = message[1].toString()
                    if (msg.startsWith("吃")) {
                        if(msg == "吃什么"){
                            subject.sendMessage("不许复制我！")
                        }else if (eatTimer > 0 && msg.length > 1) {
                            val eaten = msg.replaceFirst("吃", "")
                            subject.sendMessage(eatenReturn(this, eaten))
                            lastMsg.remove(id)
                            eatTimer = 0
                        }
                    }
                    if (eatTimer > 0) {
                        eatTimer -= 1
                    }
                }
                contains("吃") {
                    val id: Long
                    if (subject is Group) {
                        id = source.targetId
                        if(!groupList.first{it.groupId == source.targetId}.ruleObj.responseEatKeyword) return@contains
                    } else {
                        id = source.fromId
                    }
                    if (!message[1].toString().startsWith("吃")) {
                        when((1..5).random()){
                            1->this.javaClass.getResourceAsStream("/吃啥呢.jpg")!!.sendAsImageTo(subject)
                            else-> subject.sendMessage("吃什么")
                        }
                        eatTimer = (3..5).random()
                        lastMsg.remove(id)
                    }
                }
            }
        }
    }

    private val blackFoodStringList = arrayOf(
        "%s?狗都不吃！",
        "你的口味好独特哦。",
        "那是人能吃的东西嘛？",
        "呕呕呕，可别恶心我了。",
        "那还不如吃我呢。",
        "我觉得你肯定没吃过%s",
        "？小黄勇士并不想吃%s"
    )

    private val eatString = arrayOf(
        "小黄勇士也想吃%s！",
        "是%s呀，下次记得叫上小黄勇士！",
        "%s？，小黄勇士好想吃！",
        "吃吃吃就知道吃，都不叫上我！",
        "你说的那个%s，他好吃吗？"
    )

    private val eatHeiBaiString = arrayOf(
        "你说的黑白是什喵黑白？",
        "好喝好喝"
    )

    private val eatYongShiString = arrayOf(
        "是那个很多人都想吃的勇士嘛？",
        "勇士卖得太好都断货啦！",
        "你们怎么整天只知道吃勇士呀？",
        "勇士好像在看屏幕，快躲起来！",
        "好主意，我相信勇士也会很享受的！"
    )

    private fun eatenReturn(msg : MessageEvent, eaten : String) : String{
        msg.apply {
            if(XhysMiraiBot.foodBlackList.contains(eaten)){
                return String.format(blackFoodStringList.random(), eaten)
            }

            val result = User.where("beAteText is not null")
            result.forEach {
                if(it.displayName == eaten){
                    return it.beAteText!!
                }
            }
            return when (eaten) {
                "黑白" -> eatHeiBaiString.random()
                "勇士" -> eatYongShiString.random()
                else -> String.format(eatString.random(), eaten)
            }
        }
    }

}