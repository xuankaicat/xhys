package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.IObjectMysql
import java.sql.Date
import java.time.LocalDate

open class Users() : IObjectMysql {
    var id: Int = 0
    var qqId: Long = 0
    var nick:String? = null
    var money:Long? = null
    var lastjrrp:LocalDate? = null
    var beAteText:String? = null
    var usedMoney:Long = 0

    val displayName: String
        get() = nick ?: qqId.toString()

    override fun add(varName:String, value:Any?){
        when(varName){
            "id"->id= value as Int
            "qqId"->qqId = value as Long
            "nick"->nick = value as String?
            "money"->money = value as Long?
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
        }
    }

    companion object{
        fun isUserExist(qqId: Long) = DataMysql.getValue<Int>("select id from users " +
                "where qqId = $qqId") != null

        fun findByQQId(qqId : Long) = DataMysql.query<Users>("select * from users where qqId=${qqId}")[0]
    }
}