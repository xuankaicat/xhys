package app.xuankai.xhys

import app.xuankai.xhys.behaviours.*
import app.xuankai.xhys.managers.CommandMgr.initCommandSystem
import app.xuankai.xhys.behaviours.Eat.eat
import app.xuankai.xhys.behaviours.Repeat.repeat
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.model.FoodBlackList
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.utils.BotConfiguration
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {
    if(args.size != 2){
        println("程序启动参数应为账号 密码")
        exitProcess(0)
    }
    val qqId : Long = args[0].toLong()
    val password : String = args[1]

    XhysMiraiBot.apply {
        miraiBot = BotFactory.newBot(qqId, password) {
            fileBasedDeviceInfo("device.json") // 使用 device.json 存储设备信息
            protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE // 切换协议
        }.alsoLogin()

        DataMysql.query<FoodBlackList>("select * from foodblacklist").forEach {
            foodBlackList.add(it.eatStr)
        }

        initExistingUsers()//初始化新用户

        addNew()//添加新朋友
        atEvent()//at
        initCommandSystem()//指令系统
        baseReply()//基本回复
        repeat()//复读机
        eat()//吃
        sendimg()//发送固定图片
        nudgeBot()//戳一戳

        miraiBot.join() // 等待 Bot 离线, 避免主线程退出
    }
}