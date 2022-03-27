package com.github.xuankaicat.xhys.mysql.model

import com.github.xuankaicat.xhys.mysql.DataMysql
import com.github.xuankaicat.xhys.mysql.IObjectMysql
import java.sql.Date
import java.time.LocalDate

open class User : IObjectMysql {
    var id: Int = 0
    var qqId: Long = 0
    var nick:String? = null
    var money:Long = 0
    var lastjrrp:LocalDate? = null
    var beAteText:String? = null
    var usedMoney:Long = 0
    var material:Long = 0

    val displayName: String
        get() = nick ?: qqId.toString()

    override fun add(varName:String, value:Any?) {
        when(varName){
            "id"->id= value as Int
            "qqId"->qqId = value as Long
            "nick"->nick = value as String?
            "money"->money = value as Long
            "lastjrrp"->{
                val date : Date? = value as Date?
                if(date == null){
                    lastjrrp = null
                    return
                }
                lastjrrp = date.toLocalDate()
            }
            "beAteText"->beAteText = value as String?
            "usedMoney" -> usedMoney = value as Long
            "material" -> material = value as Long
        }
    }

    fun update() {
        DataMysql.executeSql("update user set " +
                "nick=${if(nick != null) "'${nick}'" else "null"}," +
                "money='${money}'," +
                "lastjrrp=${if(lastjrrp != null) "'${lastjrrp}'" else "null"}," +
                "beAteText=${if(beAteText != null) "'${beAteText}'" else "null"}," +
                "usedMoney='${usedMoney}'," +
                "material='${material}' where id=${id}")
    }

    companion object{
        fun insert(qqId: Long) = DataMysql.executeSql("insert into user(qqId) values(${qqId})")

        fun where(str: String) = DataMysql.query<User>("select * from user where ${str})")

        fun exist(qqId: Long) = DataMysql.getValue<Int>("select id from user where qqId = $qqId") != null

        fun find(qqId : Long) = DataMysql.query<User>("select * from user where qqId=${qqId}")[0]

        fun findOrNull(qqId : Long) : User? {
            val list = DataMysql.query<User>("select * from user where qqId=${qqId}")
            if(list.isEmpty()) return null
            return list[0]
        }
    }
}