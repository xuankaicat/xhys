package app.xuankai.xhys

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import java.io.File


fun XhysMiraiBot.sendimg(){
    apply {
        miraiBot.eventChannel.subscribeMessages {
            (containsAny("?", "？")) {
                if(message[1].toString() == "?" || message[1].toString() == "？"){
                    this.javaClass.getResourceAsStream("/wenhao/不许问号.jpg")!!.sendAsImageTo(subject)
                    //val file = File(".", "testpic.jpg")
                    //file.inputStream().sendAsImageTo(subject)
                }else{
                    if((1..4).random() != 1){
                        id = if (subject is Group) source.targetId else source.fromId
                        val dir = when((1..14).random()){
                            1->"/wenhao/wenhao1.jpg"
                            2->"/wenhao/wenhao2.png"
                            3->"/wenhao/wenhao3.gif"
                            4->"/wenhao/wenhao4.png"
                            5->"/wenhao/wenhao5.jpg"
                            6->"/wenhao/wenhao6.png"
                            7->"/wenhao/wenhao7.jpg"
                            8->"/wenhao/wenhao8.jpg"
                            9->"/wenhao/wenhao9.jpg"
                            10->"/wenhao/wenhao10.jpg"
                            11->"/wenhao/wenhao11.jpg"
                            12->"/wenhao/wenhao12.jpg"
                            13->"/wenhao/wenhao13.png"
                            else->"/wenhao/不许问号.jpg"
                        }
                        this.javaClass.getResourceAsStream(dir)!!.sendAsImageTo(subject)
                    }
                }
                lastMsg.remove(id)
            }
            (containsAny("三年之期", "龙王")) {
                id = if (subject is Group) source.targetId else source.fromId
                when((1..2).random()){
                    1->this.javaClass.getResourceAsStream("/隐忍.jpg")!!.sendAsImageTo(subject)
                    else->this.javaClass.getResourceAsStream("/歪嘴.jpg")!!.sendAsImageTo(subject)
                }

                lastMsg.remove(id)
            }
            contains("隐忍"){
                id = if (subject is Group) source.targetId else source.fromId
                this.javaClass.getResourceAsStream("/隐忍.jpg")!!.sendAsImageTo(subject)
                lastMsg.remove(id)
            }
            contains("power"){
                if((1..10).random() == 1 && !message.toString().contains("小黄勇士power")){
                    id = if (subject is Group) source.targetId else source.fromId
                    this.javaClass.getResourceAsStream("/了不起的猫咪.jpg")!!.sendAsImageTo(subject)
                    lastMsg.remove(id)
                }
            }
            contains("小企鹅冲击"){
                id = if (subject is Group) source.targetId else source.fromId
                this.javaClass.getResourceAsStream("/小企鹅冲击.jpg")!!.sendAsImageTo(subject)
                lastMsg.remove(id)
            }
        }
    }
}