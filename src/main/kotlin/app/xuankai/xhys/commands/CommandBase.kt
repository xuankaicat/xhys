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
        |版本号：202105r2
        |更新内容：
        |1.新增抽卡指令(只支持十连抽)
        |(.drawcard或.十连)
        |每次十连消耗100硬币
        |2.修改硬币存储方式
        |3. .coin
        """.trimMargin())

    private fun help() : Message =
        PlainText("这些是小黄勇士听得懂的话：\n" +
                ".log 获取最近的更新内容\n" +
                ".rp <value> 调整复读功率\n" +
                ".jrrp 获取今日人品\n" +
                ".money 查询硬币数量\n" +
                ".dice 丢骰子\n" +
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