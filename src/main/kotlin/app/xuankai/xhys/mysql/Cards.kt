package app.xuankai.xhys.mysql

import app.xuankai.xhys.mysql.enums.CardRarity

open class Cards : ObjectMysql {
    var id : Int = 0
    var name : String = ""
    var rarity : CardRarity = CardRarity.N;
    var pic : String? = null;

    override fun add(varName: String, value: Any?) {
        when(varName){
            "id"->id = value as Int
            "name"->name = value as String
            "rarity"->rarity = value as CardRarity
            "pic"->pic = value as String?
        }
    }
}

