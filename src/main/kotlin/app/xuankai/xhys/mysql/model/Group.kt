package app.xuankai.xhys.mysql.model

import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.IObjectMysql
import app.xuankai.xhys.mysql.model.Rule.Companion.parseRule

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
        fun getAll() =
            DataMysql.query<Group>("select * from `group`")

        fun isGroupExisted(groupId: Long) =
            DataMysql.getValue<Long>("select count(*) from `group` where `groupId`=$groupId") != 0L

        fun addInitGroup(groupId: Long) : Group {
            val newGroup = Group(groupId)
            DataMysql.executeSql("insert into `group`(`groupId`,`repeat`,`rule`) " +
                    "values($groupId,100,${newGroup.rule})")
            return newGroup
        }

        fun updateRepeat(groupId: Long, repeat: Int) =
            DataMysql.executeSql("update `group` set `repeat`=$repeat where `groupId`=$groupId")
    }
}