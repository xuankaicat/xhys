package com.github.xuankaicat.xhys

import com.github.xuankaicat.xhys.core.IXhysBot
import com.github.xuankaicat.xhys.model.Group
import net.mamoe.mirai.Bot

object XhysMiraiBot : IXhysBot {
    val lastMsg = HashMap<Long, String>()
    var eatTimer = 0

    val registeredQQid = ArrayList<Long>()
    lateinit var groupList : ArrayList<Group>
    var foodBlackList = ArrayList<String>()

    override lateinit var miraiBot : Bot
}