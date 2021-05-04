package app.xuankai.xhys

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.Users
import net.mamoe.mirai.message.data.PlainText

object Vault {
    const val canNotEffortText = "%s,你硬币不够了！"

    fun cost(qqId : Long, cost : Long) : Boolean{
        val result = DataMysql.query<Users>("select * from users where qqId=${qqId}")
        val money = result[0].money!! - result[0].usedMoney
        if(money < cost){
            return false
        }
        DataMysql.executeSql("update users set usedMoney=usedMoney+${cost} where qqId=${qqId}")
        return true
    }

    fun add(qqId: Long, value:Long){
        DataMysql.executeSql("update users set money=money+${value} where qqId=${qqId}")
    }
}