package app.xuankai.xhys

import app.xuankai.xhys.commands.CommandBase
import app.xuankai.xhys.commands.CommandJrrp
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.FoodBlackList
import app.xuankai.xhys.mysql.Users
import app.xuankai.xhys.utils.CommandUtils
import app.xuankai.xhys.utils.format
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

object CommandMgr {
    fun XhysMiraiBot.baseCommand(){
        apply {
            miraiBot.eventChannel.subscribeMessages {
                (startsWith(".") or startsWith("。")){
                    id = if (subject is Group) source.targetId else source.fromId
                    val msg = message[1].toString()
                    val startIndex = if (msg[1] != ' ') 1 else 2
                    val command = message[1].toString().substring(startIndex).toLowerCase()
                    if(command.trim('.','。').isNotEmpty()){
                        val text : Message = when{
                            command == "jrrp"-> CommandJrrp.get(this)
                            command.startsWith("rp")-> command_rp(this)
                            command == "money" -> commandMoney(this)
                            command.startsWith("atetext") -> commandAtetext(this)
                            command.startsWith("nn") -> commandNn(this)
                            command.startsWith("blackfood") -> commandBlackfood(this)
                            command.startsWith("unblackfood") -> commandUnblackfood(this)

                            else-> {
                                val baseCmdResult = CommandBase.getCommand(command)
                                if(baseCmdResult != null){
                                    baseCmdResult
                                }else{
                                    val cmd = CommandUtils.tryCheck(command)
                                    if(cmd != null){
                                        PlainText("小黄勇士猜你想用的指令是${cmd},\n").plus(useCommand(cmd, this))
                                    } else {
                                        PlainText("小黄勇士听不懂你的指令。。。")
                                    }
                                }
                            }
                        }
                        if(text.toString().trim().isNotEmpty()){
                            subject.sendMessage(text)
                        }
                        lastMsg.remove(id)
                    }
                }
            }
        }
    }

    private fun useCommand(command : String, event: MessageEvent) : Message {
        return when(command){
            "jrrp" -> CommandJrrp.get(event)
            "money" -> commandMoney(event)
            else -> CommandBase.getCommand(command) ?: PlainText("Error")
        }
    }

    private fun commandMoney(msg : MessageEvent) : Message{
        val result = Users.findByQQId(msg.source.fromId)
        val money = result.money
        val name = result.nick ?: msg.senderName
        return PlainText("${name},你已经存了${money}枚硬币啦!")
    }

    private fun commandAtetext(msg : MessageEvent) : Message{
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.cost(result.qqId, 10)) return PlainText.format(Vault.canNotEffortText, name)
        val msgstr = msg.message[1].toString()
        val value : String? = if(msgstr.trim() == ".atetext") null else msgstr.substring(8).trim()

        if(value == null){
            DataMysql.executeSql("update users set beAteText=null where qqId=${result.qqId}")
            return PlainText("${name},你成功花费10枚硬币取消了被吃文字功能！")
        }
        DataMysql.executeSql("update users set beAteText='${value}' where qqId=${result.qqId}")
        return PlainText("${name},你成功花费10枚硬币把被吃文字改成了${value}")
    }

    private fun commandNn(msg : MessageEvent) : Message{
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.cost(result.qqId, 10)) return PlainText.format(Vault.canNotEffortText, name)
        val msgstr = msg.message[1].toString()
        val value : String? = if(msgstr.trim() == ".nn") null else msgstr.substring(3).trim()
        if(value == null){
            DataMysql.executeSql("update users set nick=null where qqId=${result.qqId}")
            return PlainText("${name},你成功花费10枚硬币取消了昵称！")
        }
        DataMysql.executeSql("update users set nick='${value}' where qqId=${result.qqId}")
        return PlainText("${name},你成功花费10枚硬币把昵称改成了${value}！")
    }

    private fun commandBlackfood(msg : MessageEvent) : Message{
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.cost(result.qqId, 10)) return PlainText.format(Vault.canNotEffortText, name)
        val msgstr = msg.message[1].toString()
        val value : String = (if(msgstr.trim() == ".blackfood") null else msgstr.trim().substring(10).trim())
            ?: return PlainText("所以小黄勇士应该把什么加入食物黑名单呢？")
        val fresult = DataMysql.query<FoodBlackList>("select * from foodblacklist where eatStr='${value}'")
        if(fresult.isNotEmpty()){
            Vault.add(result.qqId, 10)
            return PlainText("食物黑名单上已经有${value}了！")
        }
        DataMysql.executeSql("insert into foodblacklist(eatStr) values('${value}')")
        XhysMiraiBot.foodBlackList.add(value)
        return PlainText("${name},你成功花费10枚硬币把${value}添加到了食物黑名单！")
    }

    private fun commandUnblackfood(msg : MessageEvent) : Message{
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.cost(result.qqId, 10)) return PlainText.format(Vault.canNotEffortText, name)
        val msgstr = msg.message[1].toString()
        val value : String = (if(msgstr.trim() == ".unblackfood") null else msgstr.substring(12).trim())
            ?: return PlainText("所以小黄勇士应该把什么从食物黑名单上划掉呢？")
        val fresult = DataMysql.query<FoodBlackList>("select * from foodblacklist where eatStr='${value}'")
        if(fresult.isEmpty()){
            return PlainText("小黄勇士没有在食物黑名单上找到${value}，但是硬币还是收走了！")
        }
        DataMysql.executeSql("delete from foodblacklist where eatStr='${value}'")
        XhysMiraiBot.foodBlackList.remove(value)
        return PlainText("${name},你成功花费10枚硬币把${value}从食物黑名单去掉了！")
    }
}