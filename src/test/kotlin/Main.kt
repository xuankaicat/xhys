import com.sun.xml.internal.fastinfoset.util.StringArray
import java.util.*

fun main(args: Array<String>) {
    val sc : Scanner = Scanner(System.`in`)
    val str = sc.next()
    System.out.println(check(str))
}

fun check(str : String) : String? {
    val existingCommands = arrayOf("jrrp", "dice", "money", "atetext", "blackfood", "unblackfood")
    for(s in existingCommands){
        if(str.length != s.length) continue
        var count = 0;
        for(i in str.indices){
            if(str[i] != s[i]) count++;
            if(count == 2) break;
        }
        if(count < 2){
            return s;
        }
    }
    return null;
}