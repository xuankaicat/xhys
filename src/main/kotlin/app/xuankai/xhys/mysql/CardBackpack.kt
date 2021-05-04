package app.xuankai.xhys.mysql

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
            val exist = DataMysql.query<CardBackpack>("select * from cardbackpack where qqId = $qqId and cardId = $cardId")
            return if(exist.isEmpty()){
                //从未获得的情况
                DataMysql.executeSql("insert into cardbackpack(qqId, cardId, amount) " +
                        "values ($qqId, $cardId, 1)")
                true
            }else{
                //已经有该卡牌的情况
                DataMysql.executeSql("update cardbackpack set amount=amount+1 " +
                        "where qqId = $qqId and cardId = $cardId")
                false
            }
        }
    }
}