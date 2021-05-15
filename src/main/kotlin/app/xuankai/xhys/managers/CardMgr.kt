package app.xuankai.xhys.managers

import app.xuankai.xhys.mysql.model.CardBackpack
import app.xuankai.xhys.mysql.model.Cards
import app.xuankai.xhys.mysql.DataMysql
import app.xuankai.xhys.mysql.enums.CardRarity
import app.xuankai.xhys.mysql.model.CardGroup
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
    private val cardList: ArrayList<Cards> = DataMysql.query("select * from cards")
    private val RCardPool = cardList.filter { it.rarity == CardRarity.R && it.inPool }
    private val SRCardPool = cardList.filter { it.rarity == CardRarity.SR && it.inPool }
    private val SSRCardPool = cardList.filter { it.rarity == CardRarity.SSR && it.inPool }
    private val SPCardPool = HashMap<String, ArrayList<Cards>>()
    val cardPoolList = listOf("A")

    private val CardImgPool = HashMap<Int, BufferedImage>()

    private val background: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/bg.png"))
    private val silverCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_silver.png"))
    private val yellowCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_yellow.png"))
    private val colorCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_color.png"))
    private val backpackCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/itemCover.png"))

    init {
        cardList.forEach {
            CardImgPool[it.id] = ImageIO.read(File("./images", it.pic))
        }
        SPCardPool["A2"] = DataMysql.query("select * from cards where id=55")
        SPCardPool["A3"] = DataMysql.query("select * from cards where id=57")
    }

    /**
     * 返回随机抽到的一张卡
     * @return Cards
     */
    private fun getRandomCard(pool: String?): Cards =
        when((1..100).random()) {
            in 24..100 -> RCardPool.random()
            in 3..23 -> {
                if(pool == null){
                    SRCardPool.random()
                } else {
                    //特殊卡池抽取情况20%是限定
                    if((1..5).random() == 1) {
                        SPCardPool[pool+"2"]?.random() ?: SRCardPool.random()
                    } else {
                        SRCardPool.random()
                    }
                }
            }
            else -> if(pool == null){
                SSRCardPool.random()
            } else {
                //特殊卡池抽取情况40%是限定
                if((1..5).random() <= 2) {
                    SPCardPool[pool+"3"]?.random() ?: SSRCardPool.random()
                } else {
                    SSRCardPool.random()
                }
            }
        }

    fun getRandomSSR(): Cards = SSRCardPool.random()

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
                g2d.drawString(card.name, x + 40, y + 28)
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
                    CardRarity.R-> {
                        silverTimes++
                        if(silverTimes != 10) {
                            silverCover
                        } else {
                            card = SRCardPool.random()
                            yellowCover
                        }
                    }
                    CardRarity.SR-> yellowCover
                    else-> colorCover
                }
                //背景方块
                g2d.drawImage(icon, x, y, icon.width, icon.height, null)
                //实际内容
                g2d.color = getCardFontColor(card.rarity)
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
        CardRarity.R -> Color.BLUE
        CardRarity.SR -> Color(255, 0, 255)
        CardRarity.SSR -> Color(255, 155, 0)
    }

    /**
     * 用户抽到卡牌之后的处理，返回是否第一次获得该卡牌
     * @param qqId Long
     * @param card Cards
     * @return Boolean
     */
    private fun getCardEvent(qqId : Long, card : Cards): Boolean {
        //增加卡牌总计数
        card.addExistingAmount()
        //增加玩家背包内道具
        return CardBackpack.userGetNewCard(qqId, card.id)
    }
}