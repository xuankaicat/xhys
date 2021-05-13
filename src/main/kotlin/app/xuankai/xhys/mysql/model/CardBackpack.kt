package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.IObjectMysql
import app.xuankai.xhys.mysql.enums.CardRarity
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
        /**
         * qqId的用户将amount个cardId物品交给orderQQId，返回给予是否成功
         * @param qqId Long
         * @param orderQQId Long
         * @param cardId Int
         * @param amount Int
         * @return Boolean
         */
        fun userSendCard(qqId: Long, orderQQId: Long, cardId: Int, amount: Int): Boolean {
            val fromAmount = DataMysql.getValue<Int>("select amount from cardbackpack " +
                    "where qqId = $qqId and cardId = $cardId")
                ?: return false
            if(amount > fromAmount) return false

            val toAmount = DataMysql.getValue<Int>("select amount from cardbackpack " +
                    "where qqId = $orderQQId and cardId = $cardId")
            if(toAmount == null){
                //如果列表中不存在则判断用户是否存在
                if(!Users.isUserExist(orderQQId)) return false

                DataMysql.executeSql("insert into cardbackpack(qqId, cardId, amount) " +
                        "values ($orderQQId, $cardId, $amount)")
            }else{
                DataMysql.executeSql("update cardbackpack set amount = ${toAmount + amount} " +
                        "where qqId = $orderQQId and cardId = $cardId")
            }
            if(fromAmount == amount){
                DataMysql.executeSql("delete from cardbackpack " +
                        "where qqId = $qqId and cardId = $cardId")
            }else{
                DataMysql.executeSql("update cardbackpack set amount = ${fromAmount - amount} " +
                        "where qqId = $qqId and cardId = $cardId")
            }
            return true
        }

        /**
         * 用户获取卡牌
         * @param qqId Long
         * @param cardId Int
         * @return Boolean
         */
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

        /**
         * 获取用户背包内物品
         * @param qqId Long
         * @param startIndex Int
         * @param indexAmount Int
         * @return ArrayList<UserCardBackpackItem>
         */
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

        /**
         * 获取一个稀有度重复物品的数量
         * @param qqId Long
         * @param rarity CardRarity
         * @return Long
         */
        fun userGetBackpackRepeatItemAmount(qqId: Long, rarity: CardRarity) =
            DataMysql.getValue<Long>("select sum(amount) from cardbackpack" +
                    " where qqId = $qqId and amount > 1 and cardId in (" +
                    "select id from cards where rarity='${rarity.name}')")

        fun userClearBackpackRepeatItemAmount(qqId: Long, rarity: CardRarity) =
            DataMysql.getValue<Long>("select sum(amount) from cardbackpack" +
                    " where qqId = $qqId and amount > 1 and cardId in (" +
                    "select id from cards where rarity='${rarity.name}')")

        /**
         * 获取用户拥有的物品种数
         * @param qqId Long
         * @return Long
         */
        fun userGetItemAmount(qqId: Long): Long = DataMysql.getValue("select count(*) from cardbackpack where qqId = $qqId")!!
    }
}