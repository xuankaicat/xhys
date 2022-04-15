package com.github.xuankaicat.xhys.utils

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent

inline fun MessageEvent.senderId(groupValidate: () -> Boolean = {true}): Long? {
    val id: Long
    if (subject is Group) {
        id = source.targetId
        if(!groupValidate()) return null
    } else {
        id = source.fromId
    }
    return id
}