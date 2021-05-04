package app.xuankai.xhys

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.Users
import net.mamoe.mirai.message.data.PlainText

object Vault {
    val canNotEffortText = "%s,你连10枚硬币都出不起！"

    fun cost(qqId : Long, cost : Long) : Boolean{
        val result = DataMysql.query<Users>("select * from users where qqId=${qqId}")
        val money = result[0].money!!
        if(money < cost){
            return false
        }
        DataMysql.executeSql("update users set money=money-${cost} where qqId=${qqId}")
        return true
    }

    fun add(qqId: Long, value:Long){
        DataMysql.executeSql("update users set money=money+${value} where qqId=${qqId}")
    }
}