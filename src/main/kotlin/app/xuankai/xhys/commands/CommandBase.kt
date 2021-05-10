package app.xuankai.xhys.commands

import net.mamoe.mirai.message.data.Dice
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

object CommandBase {
    fun getCommand(command: String) : Message? =
        when(command){
            "log" -> log()
            "help" -> help()
            "pshelp" -> psHelp()
            "dice" -> dice()
            else -> null
        }

    private fun log() : Message =
        PlainText("""
        |版本号：202105r4
        |更新内容：
        |新增交易指令
        |1.交给另一个玩家硬币：
        |.pay <qq号> <硬币数量>
        |2.交给另一个玩家物品：
        |.send <qq号> <物品ID> <物品数量>
        |（物品数量默认为1，物品ID可以在背包中查看）
        """.trimMargin())

    private fun help() : Message =
        PlainText("这些是小黄勇士听得懂的话：\n" +
                ".log 获取最近的更新内容\n" +
                ".rp <value> 调整复读功率\n" +
                ".jrrp 获取今日人品\n" +
                ".money 查询硬币数量\n" +
                ".dice 丢骰子\n" +
                ".item <page> 查看背包\n" +
                ".pay <qq号> <硬币数量> 交给另一个玩家硬币\n" +
                ".send <qq号> <物品ID> <物品数量=1> 交给另一个玩家物品\n" +
                ".pshelp 查询付费项目帮助")

    private fun psHelp() : Message =
        PlainText("这些是小黄勇士的付费项目：\n" +
                "10枚硬币可以：\n" +
                ".nn <str> 设置昵称\n" +
                ".atetext <str> 设置被吃文字\n" +
                ".blackfood <str> 将食物添加到黑名单\n" +
                ".unblackfood <str> 将食物从黑名单移除\n" +
                "100枚硬币可以：\n" +
                ".drawcard 十连抽卡")

    private fun dice(): Message = Dice.random()
}