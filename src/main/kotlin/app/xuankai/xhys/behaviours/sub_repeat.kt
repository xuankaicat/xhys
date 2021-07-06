package app.xuankai.xhys.behaviours

import app.xuankai.xhys.XhysMiraiBot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

object Repeat {
    fun XhysMiraiBot.repeat(){
        apply {
            miraiBot.eventChannel.subscribeMessages {
                always {
                    id = if (subject is Group) source.targetId else source.fromId
                    val droppedMessage = message.drop(1).toString()
                    if(droppedMessage == lastMsg[id] && repeatValue >= (1..100).random()){
                        subject.sendMessage(message)
                        lastMsg.remove(id)
                    }else{
                        lastMsg[id] = droppedMessage
                    }
                }
            }
        }
    }

    /**调整复读功率*/
    fun commandRp(msg : MessageEvent, args: List<String>) : Message {
        if(args.size != 1) return PlainText("参数不正确，应该使用.rp <value>！")
        msg.apply {
            val value : Int
            val rpValue : Int
            try {
                rpValue = args[0].toInt()
            } catch (ignored: NumberFormatException) {
                return PlainText("不要塞乱七八糟的东西给小黄勇士！功率的调整范围是0-100！")
            }
            value = rpValue

            return if(value != XhysMiraiBot.repeatValue){
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
}
