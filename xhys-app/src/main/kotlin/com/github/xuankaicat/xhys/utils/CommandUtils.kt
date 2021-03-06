package com.github.xuankaicat.xhys.utils

class CommandUtils {
    companion object {
        private val existingCommands = arrayOf("jrrp", "dice", "help", "coin", "pool", "item", "bag", "money", "pshelp")
        /*尝试修复用户输入*/
        fun tryCheck(str : String) : String? {
            for(s in existingCommands){
                if(str.length != s.length) continue
                var count = 0
                for(i in str.indices){
                    if(str[i] != s[i]) count++
                    if(count == 2) break
                }
                if(count < 2){
                    return s
                }
            }
            return null
        }
    }
}