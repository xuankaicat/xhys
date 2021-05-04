package app.xuankai.xhys

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content

fun XhysMiraiBot.repeat(){
    apply {
        miraiBot.eventChannel.subscribeMessages {
            always {
                id = if (subject is Group) source.targetId else source.fromId
                val dropedMessage = message.drop(1).toString()
                if(dropedMessage == lastMsg[id] && repeatValue >= (1..100).random()){
                    subject.sendMessage(message)
                    lastMsg.remove(id)
                }else{
                    lastMsg[id] = dropedMessage
                }
            }
        }
    }
}

/**调整复读功率*/
fun command_rp(msg : MessageEvent) : Message{
    msg.apply {
        val value : Int
        val changed : Boolean
        val msgstr = message[1].toString()
        if(msgstr.length == 3){
            value = 100
            changed = value != XhysMiraiBot.repeatValue
        }else{
            try {
                value = msgstr.substring(3).toInt()
                changed = value != XhysMiraiBot.repeatValue
            }catch (e : NumberFormatException){
                return PlainText("不要塞乱七八糟的东西给小黄勇士！功率的调整范围是0-100！")
            }
        }
        return if(changed){
            XhysMiraiBot.repeatValue = value
            PlainText((when(value){
                0->"小黄..小黄闭嘴就是了。"
                100->"小黄勇士已将复读功率调整至峰值！"
                else->"小黄勇士已将复读功率调整为${value}%！"
            }))

        }else{
            PlainText("小黄勇士已经是这个功率了不需要再调整了啦！")
        }
    }
}