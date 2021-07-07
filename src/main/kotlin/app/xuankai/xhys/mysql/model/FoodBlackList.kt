package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.IObjectMysql

class FoodBlackList : IObjectMysql {
    var id: Int = 0
    var eatStr : String = ""

    override fun add(varName: String, value: Any?) {
        when(varName){
            "id"->id= value as Int
            "eatStr"->eatStr = value as String
        }
    }

    companion object {
        fun getAll() =
            DataMysql.query<FoodBlackList>("select * from foodblacklist")
    }
}