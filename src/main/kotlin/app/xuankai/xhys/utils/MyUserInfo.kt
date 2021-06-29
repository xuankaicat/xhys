package app.xuankai.xhys.utils

import net.mamoe.mirai.contact.User
import java.util.regex.Pattern

class MyUserInfo(
    val birthday: Int,
    val birthmonth: Int,
    val birthyear: Int) {

    companion object {
        private val pattern = Pattern.compile("\"birthday\":(?<birthday>\\d+),\"birthmonth\":(?<birthmonth>\\d+),\"birthyear\":(?<birthyear>\\d+)",
            Pattern.CASE_INSENSITIVE)

        fun User.getMyUserInfo() : MyUserInfo? {
            val url = "https://mobile.qzone.qq.com"
            val result = HttpUtil.sendGet(url, "profile_get?format=JSon&hostuin=${this.id}") ?: return null
            val matcher = pattern.matcher(result) ?: return null
            return MyUserInfo(matcher.group("birthday").toInt(),
                matcher.group("birthmonth").toInt(),
                matcher.group("birthyear").toInt())
        }
    }

}