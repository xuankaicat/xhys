package com.github.xuankaicat.xhys.model

import com.github.xuankaicat.xhys.core.mysql.DataMysql
import com.github.xuankaicat.xhys.core.mysql.IObjectMysql

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