package com.github.xuankaicat.xhys.mysql.model

import com.github.xuankaicat.xhys.mysql.DataMysql
import com.github.xuankaicat.xhys.mysql.IObjectMysql
import com.github.xuankaicat.xhys.mysql.enums.CardRarity

open class Card : IObjectMysql {
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
        DataMysql.executeSql("update card set existingAmount = existingAmount+1 where id=${id}")
    }

    companion object {
        fun all() = DataMysql.query<Card>("select * from card")

        fun where(str: String) = DataMysql.query<Card>("select * from card where ${str}")

        fun find(id : Int) = DataMysql.query<Card>("select * from card where id=${id}").firstOrNull()

        fun rarityIdList(rarity: CardRarity): List<Int> {
            return DataMysql.query<Card>("select id from card where rarity='${rarity.name}'").map { it.id }
        }
    }
}

