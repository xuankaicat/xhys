package app.xuankai.xhys

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.model.Users

object Vault {
    const val canNotEffortText = "%s,你硬币不够了！"

    /**
     * 用户qqId将amount枚硬币交给用户orderQQId，硬币不够则返回false
     * @param qqId Long
     * @param orderQQId Long
     * @param amount Long
     * @return Boolean
     */
    fun userSendCoin(qqId: Long, orderQQId: Long, amount: Long): Boolean {
        //判断orderQQId用户是否存在
        if(!Users.isUserExist(orderQQId)) return false
        if(!costBaseCoin(qqId, amount)) return false
        addCoin(orderQQId, amount)
        return true
    }

    /**
     * 用户花费硬币，硬币不够则返回false
     * @param qqId Long
     * @param cost Long
     * @return Boolean
     */
    fun costCoin(qqId : Long, cost : Long): Boolean {
        val result = DataMysql.query<Users>("select money,usedMoney from users where qqId=${qqId}")
        val money = result[0].money!! - result[0].usedMoney
        if(money < cost) return false
        DataMysql.executeSql("update users set usedMoney=usedMoney+${cost} where qqId=${qqId}")
        return true
    }

    /**
     * 用户花费硬币总数，硬币不够则返回false
     * @param qqId Long
     * @param cost Long
     * @return Boolean
     */
    fun costBaseCoin(qqId: Long, cost: Long): Boolean {
        val result = DataMysql.query<Users>("select money,usedMoney from users where qqId=${qqId}")
        val money = result[0].money!! - result[0].usedMoney
        if(money < cost) return false
        DataMysql.executeSql("update users set money=money-${cost} where qqId=${qqId}")
        return true
    }

    /**
     * 用户获取硬币
     * @param qqId Long
     * @param value Long
     */
    fun addCoin(qqId: Long, value:Long) {
        DataMysql.executeSql("update users set money=money+${value} where qqId=${qqId}")
    }

    /**
     * 减少用户用掉的硬币
     * @param qqId Long
     * @param value Long
     */
    fun subUsedCoin(qqId: Long, value: Long) {
        DataMysql.executeSql("update users set usedMoney=usedMoney-${value} where qqId=${qqId}")
    }
}