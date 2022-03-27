package com.github.xuankaicat.xhys.mysql.model

import com.github.xuankaicat.xhys.mysql.DataMysql
import com.github.xuankaicat.xhys.mysql.IObjectMysql

class CardGroup : IObjectMysql {
    var name: String = ""
    var displayName: String? = null

    override fun add(varName: String, value: Any?) {
        when(varName){
            "name" -> name = value as String
            "displayName" -> displayName = value as String?
        }
    }

    companion object {
        fun all(): List<CardGroup> = DataMysql.query("select * from cardgroup")
    }
}