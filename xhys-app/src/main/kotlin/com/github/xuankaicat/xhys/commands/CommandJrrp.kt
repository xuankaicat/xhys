package com.github.xuankaicat.xhys.commands

import com.github.xuankaicat.xhys.managers.CommandMgr
import com.github.xuankaicat.xhys.mysql.model.User
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.max

object CommandJrrp {
    private val extraStringList = arrayOf(
        "今天也是平常的一天呢。",
        "最近读到运算结果总是偏小。",
        "今日事宜：都可以都行。",
        "建议多喝热水。",
        "妈妈永远爱你！",
        "建议拿吃的贿赂我(bushi",
        "其他的无可奉告！",
        "等额的钱已经打到你的账户上咯~",
        "神奇猫咪就知道这么多~",
        "要不要补个觉？",
        "热知识：你的一点rp被我摸走了~",
        "")

    private val highRpExtraStringList = arrayOf(
        "运势不错哦！",
        "今天摸到了双倍的钱呢。",
        "你还希望听到什么好消息？"
    )

    fun get(data: CommandMgr.CommandResult) : Message {
        val msg = data.msg
        val args = data.args
        if(args.isNotEmpty()) return PlainText("jrrp不存在参数！")
        msg.apply {
            val randoms = getRpValue(source.fromId)
            addJrrpMoney(data.user, randoms)
            val extraString: String
            val calendar = Calendar.getInstance()
            //calendar.timeInMillis = (time * 1000).toLong()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            if(hour !in 4..22){
                //val userInfo = source.sender.getMyUserInfo()
//                if(source.sender.id == 2366937214L && LocalDate.now().month.value == 6
//                        && LocalDate.now().dayOfMonth == 29) {
//                        extraString = "热知识：今天是一只红色围巾狐狸的生日。宝贝生日快乐！"
//                } else{
                    extraString = "建议早点睡。"
                //}
            }else{
                extraString = when (randoms) {
                    1 -> "事到如今已经没什么好担心的了不是嘛？"
                    in 2..10 -> "别担心，我会暗中支持你的。"
                    in 90..99 -> highRpExtraStringList.random()
                    100 -> "满上!"
                    else -> extraStringList.random()
                }
            }
            val name = data.name
            return PlainText("${name},你今天的人品值是${randoms}。${extraString}")
        }
    }

    private fun getRpValue(qqId: Long): Int {
        val time = ZonedDateTime.now()
        val rpValue: Long = qqId * 100000 + time.year * 10 + time.dayOfYear

        val rd = Random(rpValue)
        val r1 = rd.nextInt(100) + 1
        val r2 = if (time.dayOfYear >= 118) rd.nextInt(100) + 1 else Random(rpValue).nextInt(100) + 1

        return if (r1 == 1 && r2 < 90 || r2 == 1 && r1 < 90) 1 else max(r1, r2)
    }

    private fun addJrrpMoney(user: User, rpvalue: Int){
        val today : LocalDate = LocalDate.now()
        //val user = User.find(qqId)
        if(today != user.lastjrrp){
            user.money += when (rpvalue) {
                1 -> 100
                in 2..10 -> rpvalue * 5
                in 90..100 -> rpvalue * 2
                else -> rpvalue
            }
            user.lastjrrp = today
            user.usedMoney -= 100
            user.update()
        }
    }
}