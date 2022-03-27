package com.github.xuankaicat.xhys

import com.github.xuankaicat.xhys.mysql.DataMysql
import com.github.xuankaicat.xhys.mysql.model.User

/**
 * 用户交易操作的类
 */
object Vault {
    const val canNotEffortText = "%s,你硬币不够了！" //硬币不足以支付时的文字

    /**
     * 用户qqId将amount枚硬币交给用户orderQQId，硬币不够则返回false
     * @param qqId Long
     * @param orderQQId Long
     * @param amount Long
     * @return Boolean
     */
    fun userSendCoin(qqId: Long, orderQQId: Long, amount: Long): Boolean {
        //判断orderQQId用户是否存在
        if(!User.exist(orderQQId)) return false
        if(!subBaseCoin(qqId, amount)) return false
        addCoin(orderQQId, amount)
        return true
    }

    /**
     * 用户花费硬币，硬币不够则返回false
     * @param qqId Long
     * @param cost Long
     * @return Boolean
     */
    fun subCoin(qqId : Long, cost : Long): Boolean {
        val result : ArrayList<User> = DataMysql.query("select money,usedMoney from user where qqId=${qqId}")
        val money = result[0].money - result[0].usedMoney
        if(money < cost) return false
        DataMysql.executeSql("update user set usedMoney=usedMoney+${cost} where qqId=${qqId}")
        return true
    }

    /**
     * 用户花费硬币总数，硬币不够则返回false
     * @param qqId Long
     * @param cost Long
     * @return Boolean
     */
    fun subBaseCoin(qqId: Long, cost: Long): Boolean {
        val result = DataMysql.query<User>("select money,usedMoney from user where qqId=${qqId}")
        val money = result[0].money - result[0].usedMoney
        if(money < cost) return false
        DataMysql.executeSql("update user set money=money-${cost} where qqId=${qqId}")
        return true
    }

    /**
     * 用户获取硬币
     * @param qqId Long
     * @param value Long
     */
    fun addCoin(qqId: Long, value:Long) {
        DataMysql.executeSql("update user set money=money+${value} where qqId=${qqId}")
    }

    /**
     * 减少用户用掉的硬币
     * @param qqId Long
     * @param value Long
     */
    fun subUsedCoin(qqId: Long, value: Long) {
        DataMysql.executeSql("update user set usedMoney=usedMoney-${value} where qqId=${qqId}")
    }

    /**
     * 用户获取材料
     * @param qqId Long
     * @param value Long
     */
    fun addMaterial(qqId: Long, value: Long) {
        DataMysql.executeSql("update user set material=material+${value} where qqId=${qqId}")
    }

    /**
     * 用户失去材料
     * @param qqId Long
     * @param value Long
     */
    fun subMaterial(qqId: Long, value: Long) {
        DataMysql.executeSql("update user set material=material-${value} where qqId=${qqId}")
    }
}