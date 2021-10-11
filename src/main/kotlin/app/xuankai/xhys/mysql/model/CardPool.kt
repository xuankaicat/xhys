package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.IObjectMysql

open class CardPool : IObjectMysql {
    var cardId: Int = 0
    var pool: Char = ' '

    override fun add(varName: String, value: Any?) {
        when(varName){
            "cardId" -> cardId = value as Int
            "pool" -> pool = (value as String)[0]
        }
    }

    companion object {
        fun all() = DataMysql.query<CardPool>("select * from cardpool")
    }
}