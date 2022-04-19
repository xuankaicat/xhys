package com.github.xuankaicat.xhys.managers

import com.github.xuankaicat.xhys.Vault
import com.github.xuankaicat.xhys.XhysMiraiBot
import com.github.xuankaicat.xhys.behaviours.Repeat
import com.github.xuankaicat.xhys.commands.CommandBase
import com.github.xuankaicat.xhys.commands.CommandJrrp
import com.github.xuankaicat.xhys.commands.CommandRule
import com.github.xuankaicat.xhys.core.IXhysBot
import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import com.github.xuankaicat.xhys.ksp.annotation.Command
import com.github.xuankaicat.xhys.core.mysql.DataMysql
import com.github.xuankaicat.xhys.enums.CardRarity
import com.github.xuankaicat.xhys.enums.CardRarity.*
import com.github.xuankaicat.xhys.managers.CardMgr.randomCard
import com.github.xuankaicat.xhys.model.BlackFood
import com.github.xuankaicat.xhys.model.Card
import com.github.xuankaicat.xhys.model.CardBackpack
import com.github.xuankaicat.xhys.model.User
import com.github.xuankaicat.xhys.utils.CommandUtils
import com.github.xuankaicat.xhys.utils.format
import com.github.xuankaicat.xhys.utils.toInputStream
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.util.*
import java.util.regex.Pattern

object CommandMgr {
    private const val COMMAND_ADMIN = 1277961681L

    private var cmdStrPattern = ""
    //不需要任何参数的指令
    private val baseCmd = HashMap<String, ()->Message>()
    //需要当前环境信息的指令，形如 (MessageEvent, List<String>) -> Message
    private val cmd = HashMap<String, Any>()

    private const val poolSize = 20
    val resultPool = LinkedList<CommandResult>()

    fun initResultPool() {
        resultPool.clear()
        for(i in 0 until poolSize) {
            resultPool.add(CommandResult())
        }
    }

    fun getresultObject(): CommandResult {
        if(resultPool.size == 0) return CommandResult()
        return resultPool.removeFirst()
    }

    init {
        initResultPool()

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
            register(CommandResult::commandSb, "sb")
            register(CommandResult::commandMoney, "money", "coin")
            register(CommandResult::commandDrawCard, "drawcard", "十连")
            register(CommandResult::commandSoHa, "sohacard", "梭哈")
            register(CommandResult::commandActivityDrawCard, "活动十连")
            register(CommandResult::commandActivitySoHa, "活动梭哈")
            register(CommandResult::commandBackpack, "bag","backpack", "背包")
            register(CommandResult::commandItem, "item", "物品")
            register(CommandResult::commandAtetext, "atetext")
            register(CommandResult::commandBlackfood, "blackfood")
            register(CommandResult::commandUnblackfood, "unblackfood")
            register(CommandResult::commandNn, "nn")
            register(CommandResult::commandPay, "pay")
            register(CommandResult::commandSend, "send")
            register(CommandResult::commandDisenchant, "disenchant", "分解", "材料")
            register(CommandResult::commandDisenchantR, "disenchantr", "分解r")
            register(CommandResult::commandDisenchantSR, "disenchantsr", "分解sr")
            register(CommandResult::commandDisenchantSSR, "disenchantssr", "分解ssr")
            register(CommandResult::commandMake, "make", "制造", "合成")
            register(CommandRule::get, "rule")

            register(CommandResult::commandUpdateVersionControl, "uvc")
            register(CommandResult::commandUpdate, "update")
        }
    }

    @JvmName("registerStringMessage")
    private fun HashMap<String, ()->Message>.register(
        f: ()->Message,
        vararg cmdNames: String) {

        for (name in cmdNames) {
            this[name] = f
            cmdStrPattern += "|${name}"
        }
    }

    @JvmName("registerStringCommandResultMessage")
    private fun HashMap<String, Any>.register(
        f: (CommandResult) -> Message,
        vararg cmdNames: String) {

        for (name in cmdNames) {
            this[name] = f
            cmdStrPattern += "|${name}"
        }
    }
    //kSuspendFunction1: KSuspendFunction1<CommandResult, Message>

    @JvmName("registerStringAny")
    private fun HashMap<String, Any>.register(
        f: suspend (CommandResult) -> Message,
        vararg cmdNames: String) {

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

    private suspend fun getCmdFunResult(f: Any?, data: CommandResult): Message {
        @Suppress("UNCHECKED_CAST")
        return (f as? (CommandResult) -> Message)?.invoke(data)
            ?: (f as? suspend (CommandResult) -> Message)?.invoke(data)
            ?: PlainText("")
    }

    private suspend fun tryParse(messageEvent: MessageEvent): Message? {
        val matcher = pattern.matcher(messageEvent.message[1].toString())
        val resultObject = getresultObject()

        val result = if (matcher.find()) {
            //解析指令参数
            val params = matcher.group("params")

            resultObject.init(messageEvent,
                params?.trim()?.split(' ')?.toList()?.filter { it.trim() != "" } ?: listOf())
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
                    resultObject.initUser()
                    PlainText("小黄勇士猜你想用的指令是$fixedCmdText,\n")
                        .plus(getCmdFunResult(tryGetCmdFun(fixedCmdText), resultObject))
                } else {
                    PlainText("小黄勇士听不懂你的指令！输入.help来看看有什么可以用的！")
                }
            }
            resultObject.initUser()
            getCmdFunResult(c, resultObject)
        } else null

        resultObject.close()

        return result
    }

    /**
     * 指令入口
     * @receiver XhysMiraiBot
     */
    @Behaviour
    fun IXhysBot.initCommandSystem(){
        this as XhysMiraiBot
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

    class CommandResult {
        lateinit var msg: MessageEvent
        lateinit var args: List<String>
        lateinit var user: User
        val name: String
            get() = user.nick ?: msg.senderName

        fun init(msg: MessageEvent, args: List<String>) {
            this.msg = msg
            this.args = args
        }
        fun initUser() { user = User.find(msg.source.fromId) }
        fun close() = resultPool.add(this)

        @Command("uvc")
        suspend fun commandUpdateVersionControl() : Message {
            if(msg.source.sender.id != COMMAND_ADMIN) {
                val img = this.javaClass.getResourceAsStream("/摆烂.jpg")!!
                return img.uploadAsImage(msg.subject)
            }
            if(args.size != 2) return PlainText("[Error]param1: Count, param2: cardId")
            val qqIdList = DataMysql.query<User>("select qqId from cardbackpack GROUP BY qqId HAVING count(*) >= ${args[0]}")
            val stringBuilder = StringBuilder("insert into cardbackpack(qqId, cardId, amount) values")
            var first = true
            for(id in qqIdList.map { it.qqId }) {
                if(first) first = false else stringBuilder.append(",")
                stringBuilder.append("($id, ${args[1]}, 1)")
            }
            DataMysql.executeSql(stringBuilder.toString())
            return PlainText("操作成功，影响数量：${qqIdList.size}")
        }

        @Command("update")
        suspend fun commandUpdate() : Message {
            if(args.isNotEmpty() || msg.source.sender.id != COMMAND_ADMIN) {
                val img = this.javaClass.getResourceAsStream("/摆烂.jpg")!!
                return img.uploadAsImage(msg.subject)
            }
            CardMgr.imgPoolInit()
            CardMgr.poolInit()
            return PlainText("卡池更新成功！")
        }

        @Command("sb")
        fun commandSb() : Message {
            if(msg.subject !is Group) return PlainText(msg.senderName)
            return At(user.qqId)
        }

        @Command("money", "coin")
        fun commandMoney() : Message {
            if(args.isNotEmpty()) return PlainText("参数不正确，应该使用.money！")
            val money = user.money - user.usedMoney
            return PlainText("${name},你一共获得过${user.money}枚硬币，还存着${money}枚可以用!")
        }

        @Command("atetext")
        fun commandAtetext() : Message {
            if(!Vault.subCoin(user.qqId, 10)) return PlainText.format(Vault.canNotEffortText,
                name
            )
            val value : String? = if(args.isEmpty()) null else args.joinToString()

            if(value == null){
                user.beAteText = null
                user.update()
                return PlainText("${name},你成功花费10枚硬币取消了被吃文字功能！")
            }
            user.beAteText = value
            user.update()
            return PlainText("${name},你成功花费10枚硬币把被吃文字改成了${value}")
        }

        @Command("nn")
        fun commandNn() : Message {
            if(!Vault.subCoin(user.qqId, 10)) return PlainText.format(Vault.canNotEffortText,
                name
            )
            val value : String? = if(args.isEmpty()) null else args.joinToString("")

            if(value == null){
                user.nick = null
                user.update()
                return PlainText("${name},你成功花费10枚硬币取消了昵称！")
            }
            user.nick = value
            user.update()
            return PlainText("${name},你成功花费10枚硬币把昵称改成了${value}！")
        }

        @Command("blackfood")
        fun commandBlackfood() : Message {
            if(!Vault.subCoin(user.qqId, 10)) return PlainText.format(Vault.canNotEffortText,
                name
            )
            val value : String = (if(args.isEmpty()) null else args.joinToString(""))
                ?: return PlainText("所以小黄勇士应该把什么加入食物黑名单呢？")
            val fresult = BlackFood.where("eatStr='${value}'")
            if(fresult.isNotEmpty()){
                Vault.addCoin(user.qqId, 10)
                return PlainText("食物黑名单上已经有${value}了！")
            }
            BlackFood.insert(value)
            XhysMiraiBot.foodBlackList.add(value)
            return PlainText("${name},你成功花费10枚硬币把${value}添加到了食物黑名单！")
        }

        @Command("unblackfood")
        fun commandUnblackfood() : Message {
            if(!Vault.subCoin(user.qqId, 10)) return PlainText.format(Vault.canNotEffortText,
                name
            )
            val value : String = (if(args.isEmpty()) null else args.joinToString(""))
                ?: return PlainText("所以小黄勇士应该把什么从食物黑名单上划掉呢？")
            val fresult = BlackFood.where("eatStr='${value}'")
            if(fresult.isEmpty()){
                return PlainText("小黄勇士没有在食物黑名单上找到${value}，但是硬币还是收走了！")
            }
            BlackFood.delete(value)
            XhysMiraiBot.foodBlackList.remove(value)
            return PlainText("${name},你成功花费10枚硬币把${value}从食物黑名单去掉了！")
        }

        @Command("drawcard", "十连")
        suspend fun commandDrawCard() : Message {
            val pool = if(args.isEmpty()) null else args[0]
            if(pool != null && pool[0] !in CardMgr.cardPoolList) return PlainText("没有这个卡池！输入.pool查看有哪些卡池存在！")

            if(!Vault.subCoin(user.qqId, 100)) return PlainText.format(Vault.canNotEffortText,
                name
            )
            val stream = CardMgr.getTenCards(name, user.qqId, msg.source.sender.avatarUrl, pool).toInputStream()
            return messageChainOf(PlainText("${name},你成功花费100枚硬币在${pool ?: "默认"}卡池进行了一次十连！"),
                stream.uploadAsImage(msg.subject))
        }

        @Command("sohacard", "梭哈")
        suspend fun commandSoHa() : Message {
            val pool = if(args.isEmpty()) null else args[0]
            if(pool != null && pool[0] !in CardMgr.cardPoolList) return PlainText("没有这个卡池！输入.pool查看有哪些卡池存在！")

            if(user.money - user.usedMoney < 100) return PlainText.format(Vault.canNotEffortText,
                name
            )
            val stream = CardMgr.getPokerCards(
                name,
                user, msg.source.sender.avatarUrl, pool).toInputStream()
            return messageChainOf(PlainText("${name},你成功在${pool ?: "默认"}卡池进行了一次梭哈！"),
                stream.uploadAsImage(msg.subject))
        }

        @Command("活动十连")
        suspend fun commandActivityDrawCard() : Message {
            if(args.isNotEmpty()) return PlainText("")
            args = listOf("A")
            return commandDrawCard()
        }

        @Command("活动梭哈")
        suspend fun commandActivitySoHa() : Message {
            if(args.isNotEmpty()) return PlainText("")
            args = listOf("A")
            return commandSoHa()
        }

        @Command("bag", "backpack", "背包")
        suspend fun commandBackpack() : Message {
            if(args.size > 1) return PlainText("参数不正确，应该使用.backpack <page>！")

            val page = if(args.isEmpty()) 1 else args[0].toIntOrNull() ?: return PlainText("奇怪的页数！")
            val imgResult = CardMgr.getBackpack(name, user.qqId, msg.source.sender.avatarUrl, page)
                ?: return PlainText("${name},你根本没有这么多东西！")
            val stream = imgResult.toInputStream()
            return messageChainOf(PlainText("${name},你的背包第 $page 页是"),
                stream.uploadAsImage(msg.subject))
        }

        @Command("item", "物品")
        suspend fun commandItem() : Message {
            if(args.size > 1) return PlainText("参数不正确，应该使用.item <page>！")

            val page = if(args.isEmpty()) 1 else args[0].toIntOrNull() ?: return PlainText("奇怪的页数！")
            val imgResult = CardMgr.getBackpack(name, user.qqId, msg.source.sender.avatarUrl, page, false)
                ?: return PlainText("${name},你根本没有这么多东西！")
            val stream = imgResult.toInputStream()
            return messageChainOf(PlainText("${name},你的物品第 $page 页是"),
                stream.uploadAsImage(msg.subject))
        }

        @Command("pay")
        fun commandPay() : Message {
            if(args.size != 2) return PlainText("参数不对喔，先写要付给谁再写要付多少枚硬币！中间用空格分开！")

            val qqId = args[0].toLong()
            val count = args[1].toLong()

            if(count <= 0) return PlainText("必须支付大于0的整数个硬币！")

            if(!Vault.userSendCoin(user.qqId, qqId, count)) return PlainText.format(Vault.canNotEffortText,
                name
            )
            if(qqId == user.qqId) return PlainText("${name},你成功把${args[1]}枚硬币从左手放到了右手！")
            return PlainText("${name},你成功支付给${args[0]} ${args[1]}枚硬币！")
        }

        @Command("send")
        fun commandSend() : Message {
            if(args.size != 2 && args.size != 3) return PlainText("参数不对喔，先写要付给谁再写卡牌的ID再写数量！中间用空格分开！")

            if(args[0].toLong() == user.qqId) return PlainText("${name},你自己操作吧XD")
            val amount = if(args.size == 2) 1 else args[2].toInt()
            val cardId = args[1].toInt()
            if(!CardBackpack.userSendCard(user.qqId, args[0].toLong(), cardId, amount)) return PlainText("发送失败了，请检查参数！")
            val cardName = Card.find(cardId)?.name ?: return PlainText("ID为$cardId 的物品不存在！")
            return PlainText("${name},你成功把 $amount 个 $cardName 交给了${args[0]}!")
        }

        @Command("disenchantr", "分解r")
        fun commandDisenchantR() : Message {
            args = listOf("R")
            return commandDisenchant()
        }

        @Command("disenchantsr", "分解sr")
        fun commandDisenchantSR() : Message {
            args = listOf("SR")
            return commandDisenchant()
        }

        @Command("disenchantssr", "分解ssr")
        fun commandDisenchantSSR() : Message {
            args = listOf("SSR").plus(args)
            return commandDisenchant()
        }

        @Command("disenchant", "分解", "材料")
        fun commandDisenchant() : Message {
            if(args.isEmpty()) {
                //获取分解列表的情况
                val rAmount = CardBackpack.userGetBackpackRepeatItemAmount(user.qqId, R)
                val srAmount = CardBackpack.userGetBackpackRepeatItemAmount(user.qqId, SR)
                val ssrAmount = CardBackpack.userGetBackpackRepeatItemAmount(user.qqId, SSR)
                val stringBuilder = StringBuilder("${name},你一共有${user.material}个材料。")
                stringBuilder.apply {
                    if(rAmount != 0L || srAmount != 0L || ssrAmount != 0L) {
                        appendLine("这些是你的分解结果和方法：")
                        if(rAmount != 0L) appendLine("一共有 $rAmount 个重复的R品质物品，分解可以得到 $rAmount 个材料(使用.disenchantR分解所有重复R品质物品)")
                        if(srAmount != 0L) appendLine("一共有 $srAmount 个重复的SR品质物品，分解可以得到 ${srAmount * 5} 个材料(使用.disenchantSR分解所有重复SR品质物品)")
                        if(ssrAmount != 0L) {
                            appendLine("一共有 $ssrAmount 个重复的SSR品质物品(使用.disenchantSSR <ID> <数量=1>分解SSR)")
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
                    if(args.size < 2) return PlainText("分解SSR请在后面跟上要分解的物品ID！如果要全部分解请使用.disenchantSSR all")
                    if(args[1].lowercase(Locale.getDefault()) != "all") {
                        //指定分解的情况
                        val amount = if(args.size >= 3) args[2].toInt() else 1
                        if(amount == 0) return PlainText("好，分解结束（？）")

                        val card = Card.find(args[1].toInt()) ?: return PlainText("这根本不是个东西！")
                        if(card.rarity != SSR) {
                            return PlainText("${name}, ${card.name}根本不是SSR！不能通过这个方法制作！")
                        }

                        val mateAmount = CardBackpack.userDisenchantSSRItem(user.qqId, args[1], amount)
                            ?: return PlainText("${name},你根本没有${card.name}！")
                        if(mateAmount == 0L) return PlainText("${name},你没有这么多${card.name}！")
                        return PlainText("${name},你成功分解了$amount 个${card.name}，获得了${mateAmount} 个材料！")
                    }
                }

                val mateAmount = CardBackpack.userClearBackpackRepeatItemAmount(user.qqId, rarity)
                return if(mateAmount == 0L){
                    PlainText("${name},你根本没有多余的${args[0].uppercase(Locale.getDefault())}品质物品！")
                } else {
                    PlainText("${name},多余的${args[0].uppercase(Locale.getDefault())}品质物品分解成功！获得了 $mateAmount 个材料！")
                }
            }
        }

        @Command("make", "制造")
        fun commandMake() : Message {
            if(args.size != 1 && args.size != 2) return PlainText("参数不对喔，应该使用.make <Id> <数量>！")
            val cardId = args[0].toInt()
            val card = Card.find(cardId)
            val amount = if (args.size == 2) args[1].toLong() else 1
            var metaCost: Long = -1
            val byCard = ArrayList<Card>()

            val makeFunc = makeFunc@{ rarity: CardRarity, cost: Long ->
                metaCost = amount * 80
                if(User.find(user.qqId).material < metaCost)
                    return@makeFunc PlainText("${name},你根本没有那么多材料！每个${rarity.type}制造需要${cost}个材料！")

                for (i in 0 until amount) {
                    if((1..10).random() == 1) {
                        val rdCard = rarity.randomCard()
                        byCard.add(rdCard)
                        CardBackpack.userGetNewCard(user.qqId, rdCard.id)
                    }
                }
                return@makeFunc null
            }

            when (card?.rarity) {
                R -> makeFunc(R, 80)
                SR -> makeFunc(SR, 100)
                SSR -> makeFunc(SSR, 300)
                UR -> makeFunc(SSR, 3000)
                null -> return PlainText("${name},我根本不知道你在说什么！")
            }?.let {
                return it
            }

            CardBackpack.userGetNewCard(user.qqId, card.id, amount)
            Vault.subMaterial(user.qqId, metaCost)
            var text: Message = PlainText("${name},你成功使用 $metaCost 个材料制造了 $amount 个 ${card.name}！")

            if(byCard.isNotEmpty()) {
                text += "\n运气不错，在制造的时候还获得了一些副产物："
                byCard.forEach {
                    text += "\n${it.name} x 1"
                }
            }
            return text
        }
    }
}