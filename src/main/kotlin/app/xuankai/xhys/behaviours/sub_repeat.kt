package app.xuankai.xhys.behaviours

import app.xuankai.xhys.XhysMiraiBot
import app.xuankai.xhys.managers.CommandMgr
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

object Repeat {
    fun XhysMiraiBot.repeat(){
        apply {
            miraiBot.eventChannel.subscribeAlways<GroupMessageEvent>(priority = EventPriority.LOW) {
                val droppedMessage = message.drop(1).toString()
                val groupId = source.subject.id
                if(droppedMessage == lastMsg[groupId]
                    && groupList.first { it.groupId == groupId }.repeat >= (1..100).random()){
                    subject.sendMessage(message)
                    lastMsg.remove(groupId)
                }else{
                    lastMsg[groupId] = droppedMessage
                }
            }
        }
    }

    /**调整复读功率*/
    fun commandRp() : Message {
        val msg = CommandMgr.msg
        val args = CommandMgr.args
        if(msg.subject !is Group) return PlainText("目前只支持设置群聊复读功率！请在你的群中使用rp指令")
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
            val group = XhysMiraiBot.groupList.first { it.groupId == source.subject.id }
            return if(value != group.repeat){
                group.repeat = value
                app.xuankai.xhys.mysql.model.Group.updateRepeat(source.subject.id, value)
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
