package app.xuankai.xhys.managers

import app.xuankai.xhys.Vault
import app.xuankai.xhys.XhysMiraiBot
import app.xuankai.xhys.behaviours.Repeat
import app.xuankai.xhys.commands.CommandBase
import app.xuankai.xhys.commands.CommandJrrp
import app.xuankai.xhys.commands.CommandRule
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.enums.CardRarity.*
import app.xuankai.xhys.mysql.model.CardBackpack
import app.xuankai.xhys.mysql.model.Cards
import app.xuankai.xhys.mysql.model.FoodBlackList
import app.xuankai.xhys.mysql.model.Users
import app.xuankai.xhys.utils.CommandUtils
import app.xuankai.xhys.utils.format
import app.xuankai.xhys.utils.toInputStream
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.lang.StringBuilder
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KSuspendFunction2

object CommandMgr {
    private var cmdStrPattern = ""
    //不需要任何参数的指令
    private val baseCmd = HashMap<String, () -> Message>()
    //需要当前环境信息的指令，形如 (MessageEvent, List<String>) -> Message
    private val cmd = HashMap<String, Any>()

    init {
        baseCmd.apply {
            register(CommandBase::log, "log")
            register(CommandBase::help, "help", "帮助")
            register(CommandBase::psHelp, "pshelp")
            register(CommandBase::dice, "dice", "骰子")
            register(CommandBase::pool, "pool", "卡池")
        }

        cmd.apply {
            register(CommandJrrp::get, "jrrp")
            register(Repeat::commandRp, "rp")
            register(::commandMoney, "money", "coin")
            register(::commandDrawCard, "drawcard", "十连")
            register(::commandActivityDrawCard, "活动十连")
            register(::commandBackpack, "bag","backpack", "背包")
            register(::commandItem, "item", "物品")
            register(::commandAtetext, "atetext")
            register(::commandBlackfood, "blackfood")
            register(::commandUnblackfood, "unblackfood")
            register(::commandNn, "nn")
            register(::commandPay, "pay")
            register(::commandSend, "send")
            register(::commandDisenchant, "disenchant", "分解", "材料")
            register(::commandMake, "make", "制造", "合成")
            register(CommandRule::get, "rule")

            register(::commandUpdateVersionControl, "uvc")
        }
    }

    private fun commandUpdateVersionControl(msg : MessageEvent, args: List<String>) : Message {
        if(args.isNotEmpty()) return PlainText("")
        if(msg.source.sender.id != 1277961681L) return PlainText("权限不足，操作失败！")
        val qqIdList = DataMysql.query<Users>("select qqId from cardbackpack GROUP BY qqId HAVING count(*) >= 80")
        val stringBuilder = StringBuilder("insert into cardbackpack(qqId, cardId, amount) values")
        var first = true
        for(id in qqIdList.map { it.qqId }) {
            if(first) first = false else stringBuilder.append(',')
            stringBuilder.append("($id, 91, 1)")
        }
        DataMysql.executeSql(stringBuilder.toString())
        return PlainText("升级成功，语句为$stringBuilder")
    }

    private fun HashMap<String, () -> Message>.register(
        f: () -> Message,
        vararg cmdNames: String) {

        for (name in cmdNames) {
            this[name] = f
            cmdStrPattern += "|${name}"
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

    private val pattern = Pattern.compile("^[.|。]\\s?(?<name>\\S+)\\s*?(?<params>.*)$",
        Pattern.CASE_INSENSITIVE)
    private fun tryGetCmdFun(cmdText: String): Any? {
        return cmd[cmdText] ?: baseCmd[cmdText]
    }

    private suspend fun getCmdFunResult(f: Any?, args: List<String>, messageEvent: MessageEvent): Message {
        @Suppress("UNCHECKED_CAST")
        return (f as? (MessageEvent, List<String>) -> Message)?.invoke(messageEvent, args)
            ?: (f as? () -> Message)?.let { it() }
            ?: (f as? KSuspendFunction2<MessageEvent, List<String>, Message>)?.invoke(messageEvent, args)
            ?: PlainText("")
    }

    private suspend fun tryParse(messageEvent: MessageEvent): Message? {
        val matcher = pattern.matcher(messageEvent.message[1].toString())
        return if (matcher.find()) {
            //解析指令参数
            val params = matcher.group("params")
            val args = params?.trim()?.split(' ')?.toList()?.filter { it.trim() != "" } ?: listOf()
            //运行指令函数
            val cmdText = matcher.group("name").lowercase(Locale.getDefault())
            val c = cmd[cmdText]
            if (c == null) {
                //不在指令注册列表中则先去基础指令中寻找
                val baseCmd = baseCmd[cmdText]
                if (baseCmd != null) return baseCmd()
                //试图修复指令
                val fixedCmdText = CommandUtils.tryCheck(cmdText)
                return if (fixedCmdText != null) {
                    PlainText("小黄勇士猜你想用的指令是$fixedCmdText,\n")
                        .plus(getCmdFunResult(tryGetCmdFun(fixedCmdText), args, messageEvent))
                } else {
                    PlainText("小黄勇士听不懂你的指令！输入.help来看看有什么可以用的！")
                }
            }
            getCmdFunResult(c, args, messageEvent)
        } else {
            null
        }
    }

    fun XhysMiraiBot.initCommandSystem(){
        apply {
            miraiBot.eventChannel.subscribeMessages(priority = EventPriority.HIGHEST) {
                (startsWith(".") or startsWith("。")){
                    val result = tryParse(this)
                    if(result != null) {
                        if(source.subject is Group) lastMsg.remove(source.subject.id)
                        if(result.toString().trim().isNotEmpty()) subject.sendMessage(result)
                        intercept()
                    }
                }
            }
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
        val pool = if(args.isEmpty()) null else args[0]
        if(pool != null && pool !in CardMgr.cardPoolList) return PlainText("没有这个卡池！输入.pool查看有哪些卡池存在！")
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.subCoin(result.qqId, 100)) return PlainText.format(Vault.canNotEffortText, name)
        val stream = CardMgr.getTenCards(name, result.qqId, msg.source.sender.avatarUrl, pool).toInputStream()
        return messageChainOf(PlainText("${name},你成功花费100枚硬币在${pool ?: "默认"}卡池进行了一次十连！"),
            stream.uploadAsImage(msg.subject))
    }

    private suspend fun commandActivityDrawCard(msg : MessageEvent, args: List<String>) : Message {
        if(args.isNotEmpty()) return PlainText("")
        return commandDrawCard(msg, listOf("A"))
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

    private suspend fun commandItem(msg : MessageEvent, args: List<String>) : Message {
        if(args.size > 1) return PlainText("参数不正确，应该使用.item <page>！")
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName

        val page = if(args.isEmpty()) 1 else args[0].toIntOrNull() ?: return PlainText("奇怪的页数！")
        val imgResult = CardMgr.getBackpack(name, result.qqId, msg.source.sender.avatarUrl, page, false)
            ?: return PlainText("${name},你根本没有这么多东西！")
        val stream = imgResult.toInputStream()
        return messageChainOf(PlainText("${name},你的物品第 $page 页是"),
            stream.uploadAsImage(msg.subject))
    }

    private fun commandPay(msg : MessageEvent, args: List<String>) : Message {
        if(args.size != 2) return PlainText("参数不对喔，先写要付给谁再写要付多少枚硬币！中间用空格分开！")
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(!Vault.userSendCoin(result.qqId, args[0].toLong(), args[1].toLong())) return PlainText.format(Vault.canNotEffortText, name)
        if(args[0].toLong() == result.qqId) return PlainText("${name},你成功把${args[1]}枚硬币从左手放到了右手！")
        return PlainText("${name},你成功支付给${args[0]} ${args[1]}枚硬币！")
    }

    private fun commandSend(msg : MessageEvent, args: List<String>) : Message {
        if(args.size != 2 && args.size != 3) return PlainText("参数不对喔，先写要付给谁再写卡牌的ID再写数量！中间用空格分开！")
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(args[0].toLong() == result.qqId) return PlainText("${name},你自己操作吧XD")
        val amount = if(args.size == 2) 1 else args[2].toInt()
        val cardId = args[1].toInt()
        if(!CardBackpack.userSendCard(result.qqId, args[0].toLong(), cardId, amount)) return PlainText("发送失败了，请检查参数！")
        val cardName = Cards.findById(cardId)?.name ?: return PlainText("ID为$cardId 的物品不存在！")
        return PlainText("${name},你成功把 $amount 个 $cardName 交给了${args[0]}!")
    }

    private fun commandDisenchant(msg : MessageEvent, args: List<String>) : Message {
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        if(args.isEmpty()) {
            //获取分解列表的情况
            val rAmount = CardBackpack.userGetBackpackRepeatItemAmount(result.qqId, R)
            val srAmount = CardBackpack.userGetBackpackRepeatItemAmount(result.qqId, SR)
            val ssrAmount = CardBackpack.userGetBackpackRepeatItemAmount(result.qqId, SSR)
            val stringBuilder = StringBuilder("$name,你一共有${result.material}个材料。")
            stringBuilder.apply {
                if(rAmount != 0L || srAmount != 0L || ssrAmount != 0L) {
                    appendLine("这些是你的分解结果和方法：")
                    if(rAmount != 0L) appendLine("一共有 $rAmount 个重复的R品质物品，分解可以得到 $rAmount 个材料(使用.disenchant R分解所有重复R品质物品)")
                    if(srAmount != 0L) appendLine("一共有 $srAmount 个重复的SR品质物品，分解可以得到 ${srAmount * 5} 个材料(使用.disenchant SR分解所有重复SR品质物品)")
                    if(ssrAmount != 0L) {
                        appendLine("一共有 $ssrAmount 个重复的SSR品质物品(使用.disenchant SSR <ID> <数量=1>分解SSR)")
                    }
                } else {
                    appendLine("目前没有任何东西可以分解！")
                }
            }
            return PlainText(stringBuilder.toString())
        } else {
            //执行分解的情况
            val rarity = valueOf(args[0].uppercase(Locale.getDefault()))

            if(rarity == UR) return PlainText("UR品质的物品目前不支持分解！")

            if(rarity == SSR) {
                //SSR不能一次全部分解
                if(args.size < 2) return PlainText("SSR不支持一键分解！请在后面跟上要分解的物品ID！")
                val amount = if(args.size >= 3) args[2].toInt() else 1
                if(amount == 0) return PlainText("好，分解结束（？）")

                val card = Cards.findById(args[1].toInt()) ?: return PlainText("这根本不是个东西！")
                if(card.rarity != SSR) {
                    return PlainText("$name, ${card.name}根本不是SSR！不能通过这个方法制作！")
                }

                val mateAmount = CardBackpack.userDisenchantSSRItem(result.qqId, args[1], amount)
                    ?: return PlainText("$name,你根本没有${card.name}！")
                if(mateAmount == 0L) return PlainText("$name,你没有这么多${card.name}！")
                return PlainText("$name,你成功分解了$amount 个${card.name}，获得了${mateAmount} 个材料！")
            }

            val mateAmount = CardBackpack.userClearBackpackRepeatItemAmount(result.qqId, rarity)
            return if(mateAmount == 0L){
                PlainText("$name,你根本没有多余的${args[0].uppercase(Locale.getDefault())}品质物品！")
            } else {
                PlainText("$name,多余的${args[0].uppercase(Locale.getDefault())}品质物品分解成功！获得了 $mateAmount 个材料！")
            }
        }
    }

    private fun commandMake(msg : MessageEvent, args: List<String>) : Message {
        if(args.size != 1 && args.size != 2) return PlainText("参数不对喔，应该使用.make <Id> <数量>！")
        val result = Users.findByQQId(msg.source.fromId)
        val name = result.nick ?: msg.senderName
        val cardId = args[0].toInt()
        val card = Cards.findById(cardId)
        val amount = if (args.size == 2) args[1].toLong() else 1
        val metaCost: Long
        val byCard = ArrayList<Cards>()
        when (card?.rarity) {
            R -> {
                metaCost = amount * 80
                if(Users.findByQQId(result.qqId).material < metaCost) return PlainText("$name,你根本没有那么多材料！每个R制造需要80个材料！")

                for (i in 0 until amount) {
                    if((1..10).random() == 1) {
                        val rdCard = CardMgr.getRandomR()
                        byCard.add(rdCard)
                        CardBackpack.userGetNewCard(result.qqId, rdCard.id)
                    }
                }
            }
            SR -> {
                metaCost = amount * 100
                if(Users.findByQQId(result.qqId).material < metaCost) return PlainText("$name,你根本没有那么多材料！每个SR制造需要100个材料！")

                for (i in 0 until amount) {
                    if((1..10).random() == 1) {
                        val rdCard = CardMgr.getRandomSR()
                        byCard.add(rdCard)
                        CardBackpack.userGetNewCard(result.qqId, rdCard.id)
                    }
                }
            }
            SSR -> {
                metaCost = amount * 300
                if(Users.findByQQId(result.qqId).material < metaCost) return PlainText("$name,你根本没有那么多材料！每个SSR制造需要300个材料！")

                for (i in 0..amount) {
                    if((1..10).random() == 1) {
                        val rdCard = CardMgr.getRandomSSR()
                        byCard.add(rdCard)
                        CardBackpack.userGetNewCard(result.qqId, rdCard.id)
                    }
                }
            }
            UR -> {
                metaCost = amount * 3000
                if(Users.findByQQId(result.qqId).material < metaCost) return PlainText("$name,你根本没有那么多材料！每个UR制造需要3000个材料！")

                for (i in 0..amount) {
                    if((1..4).random() == 1) {
                        val rdCard = CardMgr.getRandomSSR()
                        byCard.add(rdCard)
                        CardBackpack.userGetNewCard(result.qqId, rdCard.id)
                    }
                }
            }
            null -> return PlainText("$name,我根本不知道你在说什么！")
        }
        CardBackpack.userGetNewCard(result.qqId, card.id, amount)
        Vault.subMaterial(result.qqId, metaCost)
        var text: Message = PlainText("$name,你成功使用 $metaCost 个材料制造了 $amount 个 ${card.name}！")

        if(byCard.isNotEmpty()) {
            text += "\n运气不错，在制造的时候还获得了一些副产物："
            byCard.forEach {
                text += "\n${it.name} x 1"
            }
        }
        return text
    }
}