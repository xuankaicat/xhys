package com.github.xuankaicat.xhys.model

import com.github.xuankaicat.xhys.core.mysql.DataMysql
import com.github.xuankaicat.xhys.core.mysql.IObjectMysql

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