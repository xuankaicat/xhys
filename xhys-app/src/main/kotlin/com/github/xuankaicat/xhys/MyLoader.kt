package com.github.xuankaicat.xhys

import com.github.xuankaicat.xhys.ksp.generated.initBehaviours
import com.github.xuankaicat.xhys.model.BlackFood
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

        foodBlackList = BlackFood.all().map { it.eatStr } as ArrayList<String>

        initBehaviours()

        miraiBot.join() // 等待 Bot 离线, 避免主线程退出
    }
}