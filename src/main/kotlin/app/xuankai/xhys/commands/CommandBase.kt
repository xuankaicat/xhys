package app.xuankai.xhys.commands

import net.mamoe.mirai.message.data.Dice
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

object CommandBase {
    fun log() : Message =
        PlainText("""
        |版本号：2021m06r4-2
        |尝试修复超时导致的BUG
        """.trimMargin())

    fun pool() : Message =
        PlainText("""
        |每次十连会消耗100枚硬币
        |目前可以抽取的卡池如下：
        |默认卡池(78%R,20%SR,2%SSR)
        |.drawcard或.十连
        |活动卡池
        |抽到SSR时40%是时风的匕首、叮咚中的一个
        |抽到SR时20%是发光浆果、紫水晶碎片、望远镜中的一个
        |.drawcardA或.十连A或.活动十连
        """.trimMargin())

    fun help() : Message =
        PlainText("""
        |这些是小黄勇士听得懂的话：
        |.log 获取最近的更新内容
        |.rp <value> 调整复读功率
        |.jrrp 获取今日人品
        |.dice 丢骰子
        |.coin 查看硬币数量
        |.bag <page> 查看背包
        |.item <page> 以ID顺序查看背包
        |.pool 查看卡池
        |.pay <qq号> <硬币数量> 交给另一个玩家硬币
        |.send <qq号> <物品ID> <物品数量=1> 交给另一个玩家物品
        |.disenchant 分解物品
        |.make <物品ID> <数量=1> 制造物品
        |.pshelp 查询付费项目帮助
        """.trimMargin())

    fun psHelp() : Message =
        PlainText("""
        |这些是小黄勇士的付费项目：
        |【10枚硬币可以】
        |.nn <str> 设置昵称
        |.atetext <str> 设置被吃文字
        |.blackfood <str> 将食物添加到黑名单
        |.unblackfood <str> 将食物从黑名单移除
        |【100枚硬币可以】
        |.drawcard 十连抽卡
        |.活动十连 活动期间十连抽活动卡池
        """.trimMargin())

    fun dice(): Message = Dice.random()
}