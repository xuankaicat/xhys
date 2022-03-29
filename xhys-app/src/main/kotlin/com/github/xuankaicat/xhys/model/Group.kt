package com.github.xuankaicat.xhys.model

import com.github.xuankaicat.xhys.core.mysql.DataMysql
import com.github.xuankaicat.xhys.core.mysql.IObjectMysql
import com.github.xuankaicat.xhys.model.Rule.Companion.parseRule

class Group : IObjectMysql, IRuleObject {
    var id : Int = 0
    var groupId : Long = 0L
    var repeat : Int = 100
    override var rule: Long = 0L
    lateinit var ruleObj : Rule

    constructor()

    constructor(groupId: Long) {
        this.groupId = groupId
        rule = Rule.maxValue()
        ruleObj = this.parseRule()
    }

    override fun add(varName: String, value: Any?) {
        when(varName){
            "id"->id= value as Int
            "groupId"->groupId = value as Long
            "repeat"->repeat = value as Int
            "rule"-> {
                rule = value as Long
                ruleObj = this.parseRule()
            }
        }
    }

    fun updateRuleValue() {
        rule = ruleObj.getValue()
        DataMysql.executeSql("update `group` set  `rule`=${rule} where `groupId`=$groupId")
    }

    companion object {
        fun insert(groupId: Long) : Group {
            val newGroup = Group(groupId)
            DataMysql.executeSql("insert into `group`(`groupId`,`repeat`,`rule`) " +
                    "values($groupId,100,${newGroup.rule})")
            return newGroup
        }

        fun all() =
            DataMysql.query<Group>("select * from `group`")

        fun exist(groupId: Long) =
            DataMysql.getValue<Long>("select count(*) from `group` where `groupId`=$groupId") != 0L

        fun updateRepeat(groupId: Long, repeat: Int) =
            DataMysql.executeSql("update `group` set `repeat`=$repeat where `groupId`=$groupId")
    }
}