package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.IObjectMysql
import app.xuankai.xhys.mysql.viewModel.UserCardBackpackItem

open class CardBackpack : IObjectMysql {
    var qqId: Long = 0L
    var cardId: Int = 0
    var amount: Int = 0

    override fun add(varName: String, value: Any?) {
        when(varName){
            "qqId" -> qqId = value as Long
            "cardId" -> cardId = value as Int
            "amount" -> amount = value as Int
        }
    }

    companion object{
        fun userGetNewCard(qqId: Long, cardId: Int): Boolean{
            val exist = DataMysql.query<CardBackpack>("select * from cardbackpack" +
                    " where qqId = $qqId and cardId = $cardId")
            return if(exist.isEmpty()){
                //从未获得的情况
                DataMysql.executeSql(
                    "insert into cardbackpack(qqId, cardId, amount) " +
                            "values ($qqId, $cardId, 1)"
                )
                true
            }else{
                //已经有该卡牌的情况
                DataMysql.executeSql(
                    "update cardbackpack set amount=amount+1 " +
                            "where qqId = $qqId and cardId = $cardId"
                )
                false
            }
        }

        fun userGetBackpackItems(qqId: Long, startIndex: Int, indexAmount: Int): ArrayList<UserCardBackpackItem> {
            val cardArray = DataMysql.query<CardBackpack>("select cardId,amount from cardbackpack" +
                    " where qqId = $qqId limit $startIndex,$indexAmount")
            val result = ArrayList<UserCardBackpackItem>()
            cardArray.forEach {
                val card = DataMysql.query<Cards>("select * from cards where id=${it.cardId}")[0]
                result.add(UserCardBackpackItem(card, it.amount))
            }
            return result
        }

        fun userGetItemAmount(qqId: Long): Long = DataMysql.getValue("select count(*) from cardbackpack where qqId = $qqId")
    }
}