package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.IObjectMysql
import app.xuankai.xhys.mysql.enums.CardRarity

open class Cards : IObjectMysql {
    var id : Int = 0
    var name : String = ""
    var rarity : CardRarity = CardRarity.R
    var pic : String = ""
    var group : String = ""
    var existingAmount : Int = 0
    var inPool : Boolean = false

    override fun add(varName: String, value: Any?) {
        when(varName){
            "id"->id = value as Int
            "name"->name = value as String
            "rarity"->rarity = CardRarity.valueOf(value as String)
            "pic"->pic = value as String
            "group"->group = value as String
            "existingAmount"->existingAmount = value as Int
            "inPool"->inPool = value as Int == 1
        }
    }

    fun addExistingAmount(){
        DataMysql.executeSql("update cards set existingAmount = existingAmount+1 where id=${id}")
    }
}

