package app.xuankai.xhys.utils

import net.mamoe.mirai.message.data.PlainText

fun PlainText.Companion.format(format: String, vararg args: Any?): PlainText = PlainText(java.lang.String.format(format, *args))