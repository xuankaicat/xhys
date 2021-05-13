package app.xuankai.xhys.managers

import app.xuankai.xhys.Vault
import app.xuankai.xhys.XhysMiraiBot
import app.xuankai.xhys.behaviours.Repeat
import app.xuankai.xhys.commands.CommandBase
import app.xuankai.xhys.commands.CommandJrrp
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.enums.CardRarity
import app.xuankai.xhys.mysql.model.CardBackpack
import app.xuankai.xhys.mysql.model.Cards
import app.xuankai.xhys.mysql.model.FoodBlackList
import app.xuankai.xhys.mysql.model.Users
import app.xuankai.xhys.utils.CommandUtils
import app.xuankai.xhys.utils.format
import app.xuankai.xhys.utils.toInputStream
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.util.regex.Pattern
import kotlin.collections.HashMap
import kotlin.reflect.KSuspendFunction2

object CommandMgr {
    private var cmdStrPattern = "|log|help|pshelp|dice"
    private val cmd = HashMap<String, Any>()

    init {
        cmd.apply {
            register(CommandJrrp::get, "jrrp")
            register(Repeat::commandRp, "rp")
            register(::commandMoney, "money", "coin")
            register(::commandDrawCard, "drawcard", "十连")
            register(::commandBackpack, "item", "背包")
            register(::commandAtetext, "atetext")
            register(::commandBlackfood, "blackfood")
            register(::commandUnblackfood, "unblackfood")
            register(::commandNn, "nn")
            register(::commandPay, "pay")
            register(::commandSend, "send")
            register(::commandDisenchant, "disenchant", "分解")
        }
    }

    private fun HashMap<String, Any>.register(
        f: (MessageEvent, List<String>) -> Message,
        vararg cmdNames: String) {

        for (name in cmdNames) {
            this[name] = f
            cmdStrPattern += "|${name}"
        }
    }

    private fun HashMap<String, Any>.register(
        f: KSuspendFunction2<MessageEvent, List<String>, Message>,
        vararg cmdNames: String
    ) {
        for (name in cmdNames) {
            this[name] = f
            cmdStrPattern += "|${name}"
        }
    }

    private val pattern = Pattern.compile("^[.|。](?<name>=$cmdStrPattern)\\s*?(?<params>.*)$",
        Pattern.CASE_INSENSITIVE)
    private suspend fun tryParse(messageEvent: MessageEvent): Message? {
        val matcher = pattern.matcher(messageEvent.message[1].toString())
        return if (matcher.find()) {
            //解析指令参数
            val params = matcher.group("params")
            val args = params?.trim()?.split(' ')?.toList()?.filter { it.trim() != "" } ?: listOf()
            //运行指令函数
            val cmdtext = matcher.group("name").toLowerCase()
            val c = cmd[cmdtext]
            if(c == null) {
                val baseCmdResult = CommandBase.getCommand(cmdtext)
                if(baseCmdResult != null) return baseCmdResult
                val fixedcmdtext = CommandUtils.tryCheck(cmdtext)
                if(fixedcmdtext != null) {
                    return PlainText("小黄勇士猜你想用的指令是${cmd},\n").plus(useCommand(fixedcmdtext, messageEvent))
                } else {
                    return PlainText("小黄勇士听不懂你的指令！输入.help来看看有什么可以用的！")
                }
            }
            @Suppress("UNCHECKED_CAST")
            (c as? (MessageEvent, List<String>) -> Message)?.invoke(messageEvent, args)
                ?: (c as? KSuspendFunction2<MessageEvent, List<String>, Message>)?.invoke(messageEvent, args)
        } else {
            null
        }
    }

    fun XhysMiraiBot.baseCommand(){
        apply {
            miraiBot.eventChannel.subscribeMessages {
                (startsWith(".") or startsWith("。")){
                    val result = tryParse(this)
                    if(result != null) {
                        lastMsg.remove(id)
                        if(result.toString().trim().isNotEmpty()){
                            subject.sendMessage(result)
                        }
                    }
                }
            }
        }
    }

    private fun useCommand(command : String, event: MessageEvent) : Message {
        return when(command){
            "jrrp" -> CommandJrrp.get(event)
            "money","coin" -> commandMoney(event)
            else -> CommandBase.getCommand(command) ?: PlainText("Error")
        }
    }

    private fun commandMoney(msg : MessageEvent, args: List<String> = listOf()) : Message {
        if(args.isNotEmpty()) return PlainText("参数不正确，应该使用.money！")
        val result = Users.findByQQId(msg.source.fromId)
        val money = result.money!! - result.usedMoney
        val name = result.nick ?: msg.senderName
        return PlainText("${name},你一共获得过${result.money}枚硬币，还存着${money}枚可以用!")
    }

    private fun commandAtetext(msg : MessageEvent, args: List<String>) : Message {
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.subCoin(result.qqId, 10)) return PlainText.format(Vault.canNotEffortText, name)
        val value : String? = if(args.isEmpty()) null else args.joinToString()

        if(value == null){
            DataMysql.executeSql("update users set beAteText=null where qqId=${result.qqId}")
            return PlainText("${name},你成功花费10枚硬币取消了被吃文字功能！")
        }
        DataMysql.executeSql("update users set beAteText='${value}' where qqId=${result.qqId}")
        return PlainText("${name},你成功花费10枚硬币把被吃文字改成了${value}")
    }

    private fun commandNn(msg : MessageEvent, args: List<String>) : Message {
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.subCoin(result.qqId, 10)) return PlainText.format(Vault.canNotEffortText, name)
        val value : String? = if(args.isEmpty()) null else args.joinToString("")

        if(value == null){
            DataMysql.executeSql("update users set nick=null where qqId=${result.qqId}")
            return PlainText("${name},你成功花费10枚硬币取消了昵称！")
        }
        DataMysql.executeSql("update users set nick='${value}' where qqId=${result.qqId}")
        return PlainText("${name},你成功花费10枚硬币把昵称改成了${value}！")
    }

    private fun commandBlackfood(msg : MessageEvent, args: List<String>) : Message {
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.subCoin(result.qqId, 10)) return PlainText.format(Vault.canNotEffortText, name)
        val value : String = (if(args.isEmpty()) null else args.joinToString(""))
            ?: return PlainText("所以小黄勇士应该把什么加入食物黑名单呢？")
        val fresult = DataMysql.query<FoodBlackList>("select * from foodblacklist where eatStr='${value}'")
        if(fresult.isNotEmpty()){
            Vault.addCoin(result.qqId, 10)
            return PlainText("食物黑名单上已经有${value}了！")
        }
        DataMysql.executeSql("insert into foodblacklist(eatStr) values('${value}')")
        XhysMiraiBot.foodBlackList.add(value)
        return PlainText("${name},你成功花费10枚硬币把${value}添加到了食物黑名单！")
    }

    private fun commandUnblackfood(msg : MessageEvent, args: List<String>) : Message {
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.subCoin(result.qqId, 10)) return PlainText.format(Vault.canNotEffortText, name)
        val value : String = (if(args.isEmpty()) null else args.joinToString(""))
            ?: return PlainText("所以小黄勇士应该把什么从食物黑名单上划掉呢？")
        val fresult = DataMysql.query<FoodBlackList>("select * from foodblacklist where eatStr='${value}'")
        if(fresult.isEmpty()){
            return PlainText("小黄勇士没有在食物黑名单上找到${value}，但是硬币还是收走了！")
        }
        DataMysql.executeSql("delete from foodblacklist where eatStr='${value}'")
        XhysMiraiBot.foodBlackList.remove(value)
        return PlainText("${name},你成功花费10枚硬币把${value}从食物黑名单去掉了！")
    }

    private suspend fun commandDrawCard(msg : MessageEvent, args: List<String>) : Message {
        if(args.isNotEmpty()) return PlainText("参数不正确，应该使用.drawcard！")
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.subCoin(result.qqId, 100)) return PlainText.format(Vault.canNotEffortText, name)
        val stream = CardMgr.getTenCards(name, result.qqId, msg.source.sender.avatarUrl).toInputStream()
        return messageChainOf(PlainText("${name},你成功花费100枚硬币进行了一次十连！"),
            stream.uploadAsImage(msg.subject))
    }

    private suspend fun commandBackpack(msg : MessageEvent, args: List<String>) : Message {
        if(args.size > 1) return PlainText("参数不正确，应该使用.backpack <page>！")
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName

        val page = if(args.isEmpty()) 1 else args[0].toIntOrNull() ?: return PlainText("奇怪的页数！")
        val imgResult = CardMgr.getBackpack(name, result.qqId, msg.source.sender.avatarUrl, page)
            ?: return PlainText("${name},你根本没有这么多东西！")
        val stream = imgResult.toInputStream()
        return messageChainOf(PlainText("${name},你的背包第 $page 页是"),
            stream.uploadAsImage(msg.subject))
    }

    private fun commandPay(msg : MessageEvent, args: List<String>) : Message {
        if(args.size != 2) return PlainText("参数不对喔，先写要付给谁再写要付多少枚硬币！中间用空格分开！")
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName

        if(!Vault.userSendCoin(result.qqId, args[0].toLong(), args[1].toLong())) return PlainText.format(Vault.canNotEffortText, name)
        return PlainText("${name},你成功支付给${args[0]} ${args[1]}枚硬币！")
    }

    private fun commandSend(msg : MessageEvent, args: List<String>) : Message {
        if(args.size != 2 && args.size != 3) return PlainText("参数不对喔，先写要付给谁再写卡牌的ID再写数量！中间用空格分开！")
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName

        val amount = if(args.size == 2) 1 else args[2].toInt()
        val cardId = args[1].toInt()
        if(!CardBackpack.userSendCard(result.qqId, args[0].toLong(), cardId, amount)) return PlainText("发送失败了，请检查参数！")
        val cardName = Cards.findById(cardId).name
        return PlainText("${name},你成功把$amount 个$cardName 交给了${args[0]}!")
    }

    private fun commandDisenchant(msg : MessageEvent, args: List<String>) : Message {
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(args.isEmpty()) {
            //获取分解列表的情况
            val rAmount = CardBackpack.userGetBackpackRepeatItemAmount(result.qqId, CardRarity.R)
            val srAmount = CardBackpack.userGetBackpackRepeatItemAmount(result.qqId, CardRarity.SR)
            val ssrAmount = CardBackpack.userGetBackpackRepeatItemAmount(result.qqId, CardRarity.SSR)
            return PlainText(
                "$name,这些是你的分解结果和方法：\n" +
                        "一共有 $rAmount 个重复的R品质物品，分解可以得到 $rAmount 个材料(使用.disenchantR分解所有重复R品质物品)\n" +
                        "一共有 $srAmount 个重复的SR品质物品，分解可以得到 ${srAmount * 5} 个材料(使用.disenchantSR分解所有重复SR品质物品)\n" +
                        "一共有 $ssrAmount 个重复的SSR品质物品，SSR品质太坚硬了分解不掉XD"
            )
        } else {
            //执行分解的情况
            val rarity = CardRarity.valueOf(args[0].toUpperCase())
            val mateAmount = CardBackpack.userClearBackpackRepeatItemAmount(result.qqId, rarity)
            return if(mateAmount == 0L){
                PlainText("$name,你根本没有多余的${args[0].toUpperCase()}品质物品！")
            } else {
                PlainText("$name,多余的${args[0].toUpperCase()}品质物品分解成功！获得了 $mateAmount 个材料！")
            }
        }
    }
}