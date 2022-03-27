package com.github.xuankaicat.xhys.commands

import com.github.xuankaicat.xhys.XhysMiraiBot
import com.github.xuankaicat.xhys.ksp.annotation.Command
import com.github.xuankaicat.xhys.managers.CommandMgr
import com.github.xuankaicat.xhys.mysql.model.Rule
import com.github.xuankaicat.xhys.mysql.model.RuleDescription
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import kotlin.reflect.full.findAnnotation

object CommandRule {
    private fun getHelpMessage() : Message {
        val builder = StringBuilder("使用.rule <规则名> <bool>以设置规则\n这些是可以设置的规则：\n")
        for (rule in Rule.ruleList) {
            builder.append(rule.name).append(' ')
            builder.appendLine(rule.findAnnotation<RuleDescription>()?.description
                ?: "神秘规则")
        }
        return PlainText(builder.toString())
    }

    @Command("rule")
    fun get(data: CommandMgr.CommandResult) : Message {
        val msg = data.msg
        val args = data.args
        //无参数或参数为?则显示规则帮助
        if(args.isEmpty() || args[0] == "?" || args[0] == "？") return getHelpMessage()
        if(msg.subject !is Group) return PlainText("目前只支持设置群聊规则！请在你的群中使用rule指令")
        if(!((msg.sender as Member).isAdministrator() || (msg.sender as Member).isOwner())) {
            return PlainText("权限不足，无法使用rule指令！")
        }
        if(args.size != 2) return PlainText("参数异常，需要规则帮助请输入.rule ?")
        val ruleName = args[0]
        val ruleNewState = args[1] == "true" || args[1] == "1"
        for(rule in Rule.ruleList) {
            if(rule.name != ruleName) continue
            for(group in XhysMiraiBot.groupList) {
                if(group.groupId == msg.subject.id) {
                    rule.set(group.ruleObj, ruleNewState)
                    group.updateRuleValue()
                    return PlainText("成功将群${(msg.subject as Group).name}的${rule.name}规则设置为$ruleNewState！")
                }
            }
            //没有找到群组的情况
            return PlainText("找不到群${msg.subject.id}！")
        }
        //没有找到规则的情况
        return PlainText("找不到规则$ruleName")
    }
}