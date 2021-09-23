package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.IObjectMysql

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