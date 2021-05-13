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
        |版本号：202105r5
        |更新内容：
        |每日打卡额外100个不计数的硬币
        """.trimMargin())

    private fun help() : Message =
        PlainText("""
        |这些是小黄勇士听得懂的话：
        |.log 获取最近的更新内容
        |.rp <value> 调整复读功率
        |.jrrp 获取今日人品
        |.dice 丢骰子
        |.item <page> 查看背包
        |.pay <qq号> <硬币数量> 交给另一个玩家硬币
        |.send <qq号> <物品ID> <物品数量=1> 交给另一个玩家物品
        |.pshelp 查询付费项目帮助
        """.trimMargin())

    private fun psHelp() : Message =
        PlainText("""
        |这些是小黄勇士的付费项目：
        |【10枚硬币可以】
        |.nn <str> 设置昵称
        |.atetext <str> 设置被吃文字
        |.blackfood <str> 将食物添加到黑名单
        |.unblackfood <str> 将食物从黑名单移除
        |【100枚硬币可以】
        |.drawcard 十连抽卡
        """.trimMargin())

    private fun dice(): Message = Dice.random()
}