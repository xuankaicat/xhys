package app.xuankai.xhys.managers

import app.xuankai.xhys.mysql.CardBackpack
import app.xuankai.xhys.mysql.Cards
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.enums.CardRarity
import app.xuankai.xhys.utils.toInputStream
import com.sun.image.codec.jpeg.JPEGCodec
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object CardMgr {
    private val RCardPool: ArrayList<Cards> = DataMysql.query<Cards>("select * from cards where rarity='R' and inPool is true")
    private val SRCardPool = DataMysql.query<Cards>("select * from cards where rarity='SR' and inPool is true")
    private val SSRCardPool = DataMysql.query<Cards>("select * from cards where rarity='SSR' and inPool is true")
    private val CardImgPool = HashMap<Int, BufferedImage>()

    private val background: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/bg.png"))
    private val silverCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_silver.png"))
    private val yellowCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_yellow.png"))
    private val colorCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_color.png"))

    init {
        RCardPool.forEach {
            CardImgPool[it.id] = ImageIO.read(File("./images", it.pic))
        }
        SRCardPool.forEach {
            CardImgPool[it.id] = ImageIO.read(File("./images", it.pic))
        }
        SSRCardPool.forEach {
            CardImgPool[it.id] = ImageIO.read(File("./images", it.pic))
        }
    }

    private fun getRandomCard(): Cards =
        when((1..100).random()) {
            in 24..100 -> RCardPool.random()
            in 3..23 -> SRCardPool.random()
            else -> SSRCardPool.random()
        }

    fun getTenCards(name : String, qqId: Long, avatarUrl : String): BufferedImage {
        val image = BufferedImage(640, 480, background.type)
        val g2d = image.createGraphics()
        //g2d.drawImage(image.getScaledInstance(640, 480, Image.SCALE_SMOOTH), 0, 0, null)
        g2d.drawImage(background, 0, 0, null)
        val fm: FontMetrics = g2d.fontMetrics
        //画图
        var silverTimes = 0
        for (y in 120..280 step 160){
            for(x in 60..500 step 110){
                var card = getRandomCard()
                val icon = when(card.rarity){
                    CardRarity.R-> {
                        silverTimes++
                        if(silverTimes != 10) {
                            g2d.color = Color.BLUE
                            silverCover
                        } else {
                            g2d.color = Color(255, 0, 255)
                            card = SRCardPool.random()
                            yellowCover
                        }
                    }
                    CardRarity.SR-> {
                        g2d.color = Color(255, 0, 255)
                        yellowCover
                    }
                    else-> {
                        g2d.color = Color(255, 215, 0)
                        colorCover
                    }
                }
                //背景方块
                g2d.drawImage(icon, x, y, icon.width, icon.height, null)
                //实际内容
                val cardImg = CardImgPool[card.id]
                g2d.drawImage(cardImg, x, y, icon.width, icon.height, null)
                g2d.drawString(card.name, x + (80 - fm.stringWidth(card.name)) / 2, y + 100)
                //处理获取卡牌事件
                if(getCardEvent(qqId, card)){
                    //首次获得显示new标识
                    g2d.color = Color.red
                    g2d.drawString("new!", x + 70, y + 90)
                }
            }
        }
        //签名
        val avatar = ImageIO.read(URL(avatarUrl))
        g2d.color = Color.BLACK
        g2d.drawImage(avatar, 10, 10, 48, 48, null)
        g2d.drawString("$name ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}", 63, 53)
        g2d.dispose()
        return image
    }

    private fun getCardEvent(qqId : Long, card : Cards): Boolean {
        //增加卡牌总计数
        card.addExistingAmount()
        //增加玩家背包内道具
        return CardBackpack.userGetNewCard(qqId, card.id)
    }
}