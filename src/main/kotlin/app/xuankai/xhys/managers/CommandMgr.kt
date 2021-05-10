package app.xuankai.xhys.managers

import app.xuankai.xhys.Vault
import app.xuankai.xhys.XhysMiraiBot
import app.xuankai.xhys.command_rp
import app.xuankai.xhys.commands.CommandBase
import app.xuankai.xhys.commands.CommandJrrp
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.model.CardBackpack
import app.xuankai.xhys.mysql.model.Cards
import app.xuankai.xhys.mysql.model.FoodBlackList
import app.xuankai.xhys.mysql.model.Users
import app.xuankai.xhys.utils.CommandUtils
import app.xuankai.xhys.utils.format
import app.xuankai.xhys.utils.toInputStream
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.util.regex.Pattern

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
                            command == "money" || command == "coin" -> commandMoney(this)
                            command == "drawcard" || command == "十连" -> commandDrawCard(this)
                            command.startsWith("item") || command.startsWith("背包") -> commandBackpack(this)
                            command.startsWith("atetext") -> commandAtetext(this)
                            command.startsWith("nn") -> commandNn(this)
                            command.startsWith("pay")-> commandPay(this)
                            command.startsWith("send")-> commandSend(this)
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
            "coin" -> commandMoney(event)
            else -> CommandBase.getCommand(command) ?: PlainText("Error")
        }
    }

    private fun commandMoney(msg : MessageEvent) : Message{
        val result = Users.findByQQId(msg.source.fromId)
        val money = result.money!! - result.usedMoney
        val name = result.nick ?: msg.senderName
        return PlainText("${name},你一共获得过${result.money}枚硬币，还存着${money}枚可以用!")
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

    private suspend fun commandDrawCard(msg : MessageEvent): Message{
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.cost(result.qqId, 100)) return PlainText.format(Vault.canNotEffortText, name)
        val stream = CardMgr.getTenCards(name, result.qqId, msg.source.sender.avatarUrl).toInputStream()
        return messageChainOf(PlainText("${name},你成功花费100枚硬币进行了一次十连！"),
            stream.uploadAsImage(msg.subject))
    }

    private suspend fun commandBackpack(msg: MessageEvent): Message {
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        val msgstr = msg.message[1].toString().substring(1)
        val pattern: Pattern = Pattern.compile("\\d+$")
        val page : Int = if(msgstr.trim() == "item" || msgstr.trim() == "背包") 1 else {
            val res = pattern.matcher(msgstr)
            if(res.find()){
                res.group().toInt()
            }else{
                return PlainText("奇怪的页数！")
            }
        }
        val imgResult = CardMgr.getBackpack(name, result.qqId, msg.source.sender.avatarUrl, page)
            ?: return PlainText("${name},你根本没有这么多东西！")
        val stream = imgResult.toInputStream()
        return messageChainOf(PlainText("${name},你的背包第 $page 页是"),
            stream.uploadAsImage(msg.subject))
    }

    private fun commandPay(msg: MessageEvent): Message {
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        val msgstr = msg.message[1].toString()
        val value : String = (if(msgstr.trim() == ".pay") null else msgstr.substring(4).trim())
            ?: return PlainText("参数不对喔，先写要付给谁再写要付多少枚硬币！中间用空格分开！")
        val params = getParams(value)
        if(params.size != 2) return PlainText("参数不对喔，先写要付给谁再写要付多少枚硬币！中间用空格分开！")
        if(!Vault.userSendCoin(result.qqId, params[0].toLong(), params[1].toLong())) return PlainText.format(Vault.canNotEffortText, name)
        return PlainText("${name},你成功支付给${params[0]} ${params[1]}枚硬币！")
    }

    private fun commandSend(msg: MessageEvent): Message {
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        val msgstr = msg.message[1].toString()
        val value : String = (if(msgstr.trim() == ".send") null else msgstr.substring(5).trim())
            ?: return PlainText("参数不对喔，先写要付给谁再写卡牌的ID再写数量！中间用空格分开！")
        val params = getParams(value)
        if(!(params.size in 2..3)) return PlainText("参数不对喔，先写要付给谁再写卡牌的ID再写数量！中间用空格分开！")
        val amount = if(params.size == 2) 1 else params[2].toInt()
        val cardId = params[1].toInt()
        if(!CardBackpack.userSendCard(result.qqId, params[0].toLong(), cardId, amount)) return PlainText("发送失败了，请检查参数！")
        val cardName = Cards.findById(cardId).name
        return PlainText("${name},你成功把$amount 个$cardName 交给了${params[0]}!")
    }

    private val regex = Regex("\\S+")
    private fun getParams(str: String): List<String> {
        val matched = regex.findAll(str)
        val result = ArrayList<String>()
        for(m in matched.iterator()){
            result.add(m.value)
        }
        return result
    }
}