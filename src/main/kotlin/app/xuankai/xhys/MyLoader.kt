package app.xuankai.xhys

import app.xuankai.xhys.CommandMgr.baseCommand
import app.xuankai.xhys.behaviours.Eat.eat
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.FoodBlackList
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.utils.BotConfiguration

suspend fun main(args: Array<String>) {
    var test = false
    if(args.isNotEmpty()){
        test = args[0] == "test"
    }
    val qqId : Long
    val password : String
    if(test){
        qqId = 2011132136L//Bot的QQ号，需为Long类型，在结尾处添加大写L
        password = "windowswzp"//Bot的密码
    }else{
        qqId = 1586056857L//Bot的QQ号，需为Long类型，在结尾处添加大写L
        password = "qscvb123mmqq"//Bot的密码
    }

    XhysMiraiBot.apply {
        miraiBot = BotFactory.newBot(qqId, password) {
            fileBasedDeviceInfo("device.json") // 使用 device.json 存储设备信息
            protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE // 切换协议
        }.alsoLogin()

        DataMysql.query<FoodBlackList>("select * from foodblacklist").forEach {
            foodBlackList.add(it.eatStr)
        }

        userInit()//初始化新用户

        addNew()//添加新朋友
        atEvent()//at
        baseCommand()//基本指令
        baseReply()//基本回复
        repeat()//复读机
        eat()//吃
        sendimg()//发送固定图片
        nudgeBot()//戳一戳

        miraiBot.join() // 等待 Bot 离线, 避免主线程退出
    }
}