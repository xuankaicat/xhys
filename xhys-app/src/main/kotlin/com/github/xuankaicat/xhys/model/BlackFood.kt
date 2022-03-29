package com.github.xuankaicat.xhys.model

import com.github.xuankaicat.xhys.core.mysql.DataMysql
import com.github.xuankaicat.xhys.core.mysql.IObjectMysql

class BlackFood : IObjectMysql {
    var id: Int = 0
    var eatStr : String = ""

    override fun add(varName: String, value: Any?) {
        when(varName){
            "id"->id= value as Int
            "eatStr"->eatStr = value as String
        }
    }

    companion object {
        fun insert(value: String) = DataMysql.executeSql("insert into blackfood(eatStr) values('${value}')")

        fun delete(value: String) = DataMysql.executeSql("delete from blackfood where eatStr='${value}'")

        fun all() = DataMysql.query<BlackFood>("select * from blackfood")
        fun where(str: String) = DataMysql.query<BlackFood>("select * from blackfood where ${str})")
    }
}