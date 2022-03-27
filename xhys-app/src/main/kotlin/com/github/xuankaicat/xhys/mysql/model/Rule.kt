package com.github.xuankaicat.xhys.mysql.model

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class RuleDescription(val description: String)

interface IRuleObject {
    var rule: Long
}

class Rule {
    @RuleDescription("响应at事件")
    var responseAtEvent = true
    @RuleDescription("响应吃关键字")
    var responseEatKeyword = true
    @RuleDescription("发送问号表情")
    var sendQuestionImage = true
    @RuleDescription("发送关键词表情")
    var sendKeyWordImage = true

    companion object {
        val ruleList = listOf(
            Rule::responseAtEvent, //1
            Rule::responseEatKeyword, //2
            Rule::sendQuestionImage, //4
            Rule::sendKeyWordImage, //8
        )

        /**
         * 将Long型的rule解析为规则对象
         * @receiver IRuleObject
         * @return Rule
         */
        fun IRuleObject.parseRule(): Rule {
            val ruleObj = Rule()
            val ruleArray = BooleanArray(ruleList.size) { false } //规则默认为false
            //除二取余得到布尔型规则组
            var tmp = rule
            var cursor = 0
            while (tmp != 0L) {
                ruleArray[cursor++] = tmp % 2 == 1L
                tmp /= 2
            }
            //将规则组转换为规则对象
//            for((ruleArrayIndex, index) in (cursor-1).downTo(0).withIndex()) {
//                ruleList[index].set(ruleObj, ruleArray[ruleArrayIndex])
//            }
            for(index in ruleList.indices) {
                ruleList[index].set(ruleObj, ruleArray[index])
            }
            return ruleObj
        }

        /**
         * 返回所有规则都开启的最大值
         * @return Long
         */
        fun maxValue() : Long {
            val tmp = (1L).shl(ruleList.size - 1)
            return tmp - 1 + tmp
        }
    }

    fun getValue() : Long {
        var tmp = 1L
        var total = 0L
        ruleList.forEach {
            if(it.get(this)) total += tmp
            tmp *= 2
        }
        return total
    }
}