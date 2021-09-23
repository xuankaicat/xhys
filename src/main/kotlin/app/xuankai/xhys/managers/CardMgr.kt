package app.xuankai.xhys.managers

import app.xuankai.xhys.mysql.model.CardBackpack
import app.xuankai.xhys.mysql.model.Card
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.enums.CardRarity
import app.xuankai.xhys.mysql.enums.CardRarity.*
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.*
import java.lang.StringBuilder
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object CardMgr {
    private val cardList: ArrayList<Card> = Card.all()
    private val RCardPool = cardList.filter { it.rarity == R && it.inPool }
    private val SRCardPool = cardList.filter { it.rarity == SR && it.inPool }
    private val SSRCardPool = cardList.filter { it.rarity == SSR && it.inPool }
    private val URCardPool = cardList.filter { it.rarity == UR && it.inPool }
    private val SPCardPool = HashMap<String, ArrayList<Card>>()
    val cardPoolList = listOf("A")

    private val CardImgPool = HashMap<Int, BufferedImage>()

    private val background: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/bg.png"))
    private val silverCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_silver.png"))
    private val yellowCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_yellow.png"))
    private val colorCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_color.png"))
    private val blueCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_blue.png"))
    private val backpackCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/itemCover.png"))

    init {
        cardList.forEach {
            CardImgPool[it.id] = ImageIO.read(File("./images", it.pic))
        }
        //ssr UP
        SPCardPool["ASSR"] = Card.where("id=153 or id=154 or id=155 or id=156")
        //sr UP
        SPCardPool["ASR"] = Card.where("id=157 or id=158 or id=159 or id=160")
    }

    /**
     * 返回随机抽到的一张卡
     * @return Cards
     */
    private fun getRandomCard(pool: String?): Card =
        when((1..100).random()) {
            in 24..99 -> getRandomR()
            in 3..23 -> {
                if(pool == null){
                    getRandomSR()
                } else {
                    //特殊卡池抽取情况20%是限定
                    if((1..5).random() == 1) {
                        getRandomSPSR(pool)
                    } else {
                        getRandomSR()
                    }
                }
            }
            else -> if(pool == null){
                getRandomSSR()
            } else {
                //特殊卡池抽取情况60%是限定
                if((1..5).random() <= 3) {
                    getRandomSPSSR(pool)
                } else {
                    getRandomSSR()
                }
            }
        }

    fun getRandomR(): Card = RCardPool.random()
    fun getRandomSR(): Card = SRCardPool.random()
    fun getRandomSPSR(pool: String): Card = SPCardPool[pool+"SR"]?.random() ?: SRCardPool.random()
    fun getRandomSPSSR(pool: String): Card {
        if((1..50).random() == 1) return URCardPool.random()
        return SPCardPool[pool+"SSR"]?.random() ?: SSRCardPool.random()
    }
    fun getRandomSSR(): Card {
        if((1..50).random() == 1) return URCardPool.random()
        return SSRCardPool.random()
    }

//    fun getPictorialBook(name : String, qqId: Long, avatarUrl : String, page: Int): BufferedImage? {
//        val pageStart = 4 * (page - 1)
//        val groups = CardGroup.getAll().subList(pageStart, pageStart + 4)
//        val regex = Regex("(?=|${groups.joinToString("|") { it.name }})")
//        cardList.forEach {
//            if(it.group.contains(regex)) {
//
//            }
//        }
//    }

    private const val onePageAmount = 40
    /**
     * 处理用户的查看背包，返回生成的图像。如果页数不正确则返回null
     * @param name String
     * @param qqId Long
     * @param avatarUrl String
     * @param page Int
     * @param sort Boolean
     * @return BufferedImage?
     */
    fun getBackpack(name : String, qqId: Long, avatarUrl : String, page: Int, sort: Boolean = true): BufferedImage? {
        val amount = CardBackpack.userGetItemAmount(qqId)
        val pageStart = onePageAmount * (page - 1)
        if(amount < pageStart + 1) return null
        val pageAmount = amount / onePageAmount + 1

        val image = BufferedImage(640, 480, background.type)
        val g2d = image.createGraphics()
        val fm: FontMetrics = g2d.fontMetrics
        g2d.drawImage(background, 0, 0, null)
        val items = CardBackpack.userGetBackpackItems(qqId, pageStart, onePageAmount, sort)
        var index = 0
        //画图
        for(y in 80..440 step 40){
            for(x in 5..640 step 160){
                val card = items[index].card
                g2d.color = getCardFontColor(card.rarity)
                g2d.drawImage(backpackCover, x - 4, y - 4, 163, 40, null)
                g2d.drawImage(CardImgPool[card.id], x, y, 32, 32, null)
                g2d.drawCardName(card, x + 40, y + 28)
                g2d.color = Color.BLACK
                val amountStr = "x ${items[index].amount}"
                g2d.drawString(amountStr, x + 150 - fm.stringWidth(amountStr), y + 14)
                g2d.drawString("ID: ${card.id}", x + 40, y + 14)
                index++
                if(index >= items.size) break
            }
            if(index >= items.size) break
        }
        g2d.drawAvatar(name, avatarUrl)
        g2d.drawString("第 $page  /  $pageAmount 页", 63, 41)
        g2d.dispose()
        return image
    }

    /**
     *处理用户的单抽，返回生成的字符串
     * @param qqId Long
     * @return String
     */
    fun getCard(qqId: Long, pool: String?): String {
        val card = getRandomCard(pool)
        val stringBuilder = StringBuilder()
        stringBuilder.append(card.name).append("(${card.rarity})")
        //处理获取卡牌事件
        if(getCardEvent(qqId, card)){
            //首次获得显示new标识
            stringBuilder.append("(New!)")
        }
        return stringBuilder.toString()
    }

    /**
     * 处理用户的十连抽，返回生成的图像
     * @param name String
     * @param qqId Long
     * @param avatarUrl String
     * @return BufferedImage
     */
    fun getTenCards(name : String, qqId: Long, avatarUrl : String, pool: String? = null): BufferedImage {
        val image = BufferedImage(640, 480, background.type)
        val g2d = image.createGraphics()
        g2d.drawImage(background, 0, 0, null)
        val fm: FontMetrics = g2d.fontMetrics
        //画图
        var silverTimes = 0
        for (y in 120..280 step 160){
            for(x in 60..500 step 110){
                var card = getRandomCard(pool)
                val icon = when(card.rarity){
                    R -> {
                        silverTimes++
                        if(silverTimes != 10) {
                            silverCover
                        } else {
                            card = SRCardPool.random()
                            yellowCover
                        }
                    }
                    SR -> yellowCover
                    UR -> blueCover
                    else-> colorCover
                }
                //背景方块
                g2d.drawImage(icon, x, y, icon.width, icon.height, null)
                //实际内容
                val cardImg = CardImgPool[card.id]
                g2d.drawImage(cardImg, x, y, icon.width, icon.height, null)
                val strX = x + (80 - fm.stringWidth(card.name)) / 2
                g2d.drawCardName(card, strX, y + 100)
                //处理获取卡牌事件
                if(getCardEvent(qqId, card)){
                    //首次获得显示new标识
                    g2d.color = Color.red
                    g2d.drawString("new!", x + 70, y + 90)
                }
            }
        }
        g2d.drawAvatar(name, avatarUrl)
        g2d.dispose()
        return image
    }

    /**
     * 在图片左上角绘制头像、昵称和时间
     * @receiver Graphics2D
     * @param name String
     * @param avatarUrl String
     */
    private fun Graphics2D.drawAvatar(name : String, avatarUrl : String){
        this.color = Color.BLACK
        val avatar = ImageIO.read(URL(avatarUrl))
        this.drawImage(avatar, 10, 10, 48, 48, null)
        this.drawString("$name ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}", 63, 53)
    }

    private fun getCardFontColor(rarity: CardRarity): Color = when(rarity){
        R -> Color.BLUE
        SR -> Color(255, 0, 255)
        SSR -> Color(255, 155, 0)
        UR -> when((1..6).random()) {
            1-> Color(90, 255, (0..255).random())
            2-> Color(90, (0..255).random(), 255)
            3-> Color(255, 90, (0..255).random())
            4-> Color(255, (0..255).random(), 90)
            5-> Color((0..255).random(), 90, 255)
            6-> Color((0..255).random(), 255, 90)
            else -> Color(255,255,255)
        }
    }

    /**
     * 绘制卡牌名称，绘制结束后不会把画笔颜色还原
     * @receiver Graphics2D
     * @param card Cards 要绘制的卡牌
     * @param x Int
     * @param y Int
     */
    private fun Graphics2D.drawCardName(card: Card, x: Int, y:Int) {
        if(card.rarity == UR) {
            var charX = x
            for(char in card.name) {
                this.color = getCardFontColor(UR)
                this.drawString(char.toString(), charX, y)
                charX += this.fontMetrics.charWidth(char)
            }
        } else {
            this.color = getCardFontColor(card.rarity)
            this.drawString(card.name, x, y)
        }
    }

    /**
     * 用户抽到卡牌之后的处理，返回是否第一次获得该卡牌
     * @param qqId Long
     * @param card Cards
     * @return Boolean
     */
    private fun getCardEvent(qqId : Long, card : Card): Boolean {
        //增加卡牌总计数
        card.addExistingAmount()
        //增加玩家背包内道具
        return CardBackpack.userGetNewCard(qqId, card.id)
    }
}