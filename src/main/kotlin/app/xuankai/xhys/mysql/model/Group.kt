package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.IObjectMysql

class Group : IObjectMysql, IRuleObject {
    var id : Int = 0
    var groupId : Long = 0L
    var repeat : Int = 0
    override var rule : Long = 0L

    override fun add(varName: String, value: Any?) {
        when(varName){
            "id"->id= value as Int
            "groupId"->groupId = value as Long
            "repeat"->repeat = value as Int
            "rule"-> rule = value as Long
        }
    }

    companion object {
        fun isGroupExisted(groupId: Long) =
            DataMysql.getValue<Long>("select count(*) from group where groupId=$groupId") != 0L

        fun addInitGroup(groupId: Long) =
            DataMysql.executeSql("insert into group(groupId,repeat,rule) " +
                    "values($groupId,100,${Rule.maxValue()})")
    }
}