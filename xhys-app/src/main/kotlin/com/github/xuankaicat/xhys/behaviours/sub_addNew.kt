package com.github.xuankaicat.xhys.behaviours

import com.github.xuankaicat.xhys.XhysMiraiBot
import com.github.xuankaicat.xhys.ksp.annotation.Behaviour
import net.mamoe.mirai.event.ConcurrencyKind
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.globalEventChannel
import kotlin.coroutines.EmptyCoroutineContext

@Behaviour
fun XhysMiraiBot.addNew(){
    apply {
        miraiBot.globalEventChannel().subscribeAlways(
            NewFriendRequestEvent::class,
            EmptyCoroutineContext,
            ConcurrencyKind.CONCURRENT,
            EventPriority.NORMAL
        ) {
            if (fromGroup != null) {
                accept()
            }
        }
        miraiBot.globalEventChannel().subscribeAlways(
            BotInvitedJoinGroupRequestEvent::class,
            EmptyCoroutineContext,
            ConcurrencyKind.CONCURRENT,
            EventPriority.NORMAL
        ) {
            accept()
        }
    }
}