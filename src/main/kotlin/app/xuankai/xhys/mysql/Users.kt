package app.xuankai.xhys.mysql

import java.sql.Date
import java.time.LocalDate

open class Users() : ObjectMysql {
    var id: Int = 0
    var qqId: Long = 0
    var nick:String? = null
    var money:Long? = null
    var lastjrrp:LocalDate? = null
    var beAteText:String? = null

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
        }
    }

    companion object{
        fun findByQQId(qqId : Long) = DataMysql.query<Users>("select * from users where qqId=${qqId}")[0]
    }
}