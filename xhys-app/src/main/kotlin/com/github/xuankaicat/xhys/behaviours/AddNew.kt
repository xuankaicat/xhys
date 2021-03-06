package com.github.xuankaicat.xhys.behaviours

import com.github.xuankaicat.xhys.core.IXhysBot
import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import net.mamoe.mirai.event.ConcurrencyKind
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import kotlin.coroutines.EmptyCoroutineContext

@Behaviour
fun IXhysBot.addNew(){
    miraiBot.eventChannel.subscribeAlways(
        NewFriendRequestEvent::class,
        EmptyCoroutineContext,
        ConcurrencyKind.CONCURRENT,
        EventPriority.NORMAL
    ) {
        if (fromGroup != null) {
            accept()
        }
    }
    miraiBot.eventChannel.subscribeAlways(
        BotInvitedJoinGroupRequestEvent::class,
        EmptyCoroutineContext,
        ConcurrencyKind.CONCURRENT,
        EventPriority.NORMAL
    ) {
        accept()
    }
}