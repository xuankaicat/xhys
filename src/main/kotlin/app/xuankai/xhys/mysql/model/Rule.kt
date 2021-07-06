package app.xuankai.xhys.mysql.model

import kotlin.math.pow

interface IRuleObject {
    var rule: Long
}

class Rule {
    companion object {
        private val ruleList = listOf(
            Rule::responseAtEvent,
            Rule::responseEatKeyword,
            Rule::sendQuestionImage,
        )

        fun IRuleObject.parseRule(): Rule {
            val ruleObj = Rule()
            val ruleArray = BooleanArray(ruleList.size) { true } //规则默认为true
            //除二取余得到布尔型规则组
            var tmp = rule
            var cursor = 0
            while (tmp != 0L) {
                ruleArray[cursor++] = tmp % 2 == 1L
                tmp /= 2
            }
            //将规则组转换为规则对象
            for((index, ruleArrayIndex) in (cursor-1).downTo(0).withIndex()) {
                ruleList[index].set(ruleObj, ruleArray[ruleArrayIndex])
            }
            return ruleObj
        }

        fun maxValue() = (2F).pow(ruleList.size).toInt()
    }

    var responseAtEvent = true //响应at事件
    var responseEatKeyword = true //响应吃关键字
    var sendQuestionImage = true //发送问号表情
}