package app.xuankai.xhys.commands

import net.mamoe.mirai.message.data.Dice
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

object CommandBase {
    fun log() : Message =
        PlainText("""
        |版本号：2021m09r3
        |修复bug
        """.trimMargin())

    fun pool() : Message =
        PlainText("""
        |每次十连会消耗100枚硬币
        |目前可以抽取的卡池如下：
        |默认卡池(77%R,20%SR,2.94%SSR,0.06%UR)
        |.drawcard或.十连
        |活动卡池
        |抽到SSR时60%是猎食者、方阵刹那、无邪、阿尔马达中的一个
        |抽到SR时20%是2星角色单手剑专武
        |.drawcard A或.十连 A或.活动十连
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
        |.rule 设置群规则
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