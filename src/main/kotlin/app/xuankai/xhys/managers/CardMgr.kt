package app.xuankai.xhys.managers

import app.xuankai.xhys.Vault
import app.xuankai.xhys.mysql.enums.CardRarity
import app.xuankai.xhys.mysql.enums.CardRarity.*
import app.xuankai.xhys.mysql.model.Card
import app.xuankai.xhys.mysql.model.CardBackpack
import app.xuankai.xhys.mysql.model.CardPool
import app.xuankai.xhys.mysql.model.User
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.IIOException
import javax.imageio.ImageIO
import kotlin.collections.ArrayList

object CardMgr {
    private val cardList: ArrayList<Card> = Card.all()
    private val RCardPool = cardList.filter { it.rarity == R && it.inPool }
    private val SRCardPool = cardList.filter { it.rarity == SR && it.inPool }
    private val SSRCardPool = cardList.filter { it.rarity == SSR && it.inPool }
    private val URCardPool = cardList.filter { it.rarity == UR && it.inPool }
    private val SPCardPool = HashMap<String, ArrayList<Card>>()
    lateinit var cardPoolList : List<Char>
    lateinit var cardPoolText : String

    private val CardImgPool = HashMap<Int, BufferedImage>()

    private val background: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/bg.png"))
    private val silverCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_silver.png"))
    private val yellowCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_yellow.png"))
    private val colorCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_color.png"))
    private val blueCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/square_blue.png"))
    private val backpackCover: BufferedImage = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/itemCover.png"))

    init {
        imgPoolInit()

        poolInit()
    }

    fun imgPoolInit() {
        CardImgPool.clear()
        val notFound = ImageIO.read(CardMgr.javaClass.getResourceAsStream("/images/not_found.jpg"))
        cardList.forEach {
            try {
                CardImgPool[it.id] = ImageIO.read(File("./images", it.pic))
            } catch (e: IIOException) {
                CardImgPool[it.id] = notFound
            }
        }
    }

    fun poolInit() {
        val cardPools = CardPool.all()
        cardPoolList = cardPools.map { it.pool }.distinct()

        SPCardPool.clear()
        val sb = StringBuilder()

        cardPoolList.forEach {
            sb.appendLine("卡池${it}")
            val ssrSb = StringBuilder("抽到SSR时60%是")
            var ssrFlag = false
            val srSb = StringBuilder("抽到SR时20%是")
            var srFlag = false

            val tmpSSR = ArrayList<Card>()
            val tmpSR = ArrayList<Card>()
            val ids = cardPools.filter { c -> c.pool == it }.map { c -> c.cardId }
            cardList.forEach { card ->
                if(ids.contains(card.id)) {
                    if(card.rarity == SSR) {
                        tmpSSR.add(card)
                        if(ssrFlag) ssrSb.append(',') else ssrFlag = true
                        ssrSb.append(card.name)
                    } else {
                        tmpSR.add(card)
                        if(srFlag) srSb.append(',') else srFlag = true
                        srSb.append(card.name)
                    }
                }
            }

            ssrSb.append("中的一个")
            srSb.append("中的一个")

            sb.appendLine(ssrSb)
            sb.appendLine(srSb)
            sb.append(".drawcard ${it}或.十连 $it")
            if(it == 'A')
                sb.appendLine("或.活动十连")
            else
                sb.append('\n')

            SPCardPool["${it}SSR"] = tmpSSR
            SPCardPool["${it}SR"] = tmpSR
        }
        cardPoolText = sb.toString().trim('\n')
    }

    /**
     * 返回随机抽到的一张卡
     * @return Card
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

    fun getRandomR() = RCardPool.random()
    fun getRandomSR() = SRCardPool.random()
    fun getRandomSPSR(pool: String) = SPCardPool[pool+"SR"]?.random() ?: SRCardPool.random()
    fun getRandomSPSSR(pool: String): Card {
        if((1..50).random() == 1) return URCardPool.random()
        return SPCardPool[pool+"SSR"]?.random() ?: SSRCardPool.random()
    }
    fun getRandomSSR(): Card {
        if((1..50).random() == 1) return URCardPool.random()
        return SSRCardPool.random()
    }

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
                g2d.color = card.rarity.fontColor()
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
                if(card.rarity == R) silverTimes++
                if(silverTimes == 10) {
                    card = SRCardPool.random()
                }
                val icon = card.rarity.icon()
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
     * 处理用户的梭哈十连抽，返回生成的图像
     * @param name String
     * @param user User
     * @param avatarUrl String
     * @return BufferedImage
     */
    fun getPokerCards(name : String, user: User, avatarUrl : String, pool: String? = null): BufferedImage {
        val image = BufferedImage(640, 480, background.type)
        val g2d = image.createGraphics()
        g2d.drawImage(background, 0, 0, null)
        val fm: FontMetrics = g2d.fontMetrics

        var silverTimes = 0
        val list = ArrayList<Card>(10)
        val newList = ArrayList<Card>(3)
        var srCount = 0
        var rCount = 0
        var ssrFlag = false
        var totalCoin = 0
        var newSr = false
        var newR = false
        while(Vault.subCoin(user.qqId, 100)){
            totalCoin += 100
            for (i in 0 until 10) {
                var card = getRandomCard(pool)
                when(card.rarity){
                    R -> {
                        silverTimes++
                        if(silverTimes != 10) {
                            rCount++
                        } else {
                            card = SRCardPool.random()
                            srCount++
                        }
                        if(getCardEvent(user.qqId, card)) newR = true
                    }
                    SR -> {
                        srCount++
                        if(getCardEvent(user.qqId, card)) {
                            newList.add(card)
                            newSr = true
                        }
                    }
                    else -> {
                        ssrFlag = true

                        if(getCardEvent(user.qqId, card)) {
                            newList.add(card)
                        } else {
                            list.add(card)
                        }
                    }
                }
            }

            if(ssrFlag) break
        }

        newList.sortBy { it.rarity }

        //画图
        var overFlag = false
        for (y in 120..280 step 160){
            for(x in 60..500 step 110){
                var card : Card? = null
                val icon : BufferedImage
                val cardImg : BufferedImage?
                var newFlag = false
                if((y != 280 || x <390) && newList.isNotEmpty() && newList.last().rarity == SSR) {
                    card = newList.last()
                    newFlag = true
                    icon = card.rarity.icon()
                    cardImg = CardImgPool[card.id]
                    newList.removeLast()
                } else if((y != 280 || x <390) && list.isNotEmpty()) {
                    card = list.last()
                    icon = card.rarity.icon()
                    cardImg = CardImgPool[card.id]
                    list.removeLast()
                } else if((y != 280 || x <390) && newList.isNotEmpty()) {
                    card = newList.last()
                    newFlag = true
                    icon = card.rarity.icon()
                    srCount--
                    cardImg = CardImgPool[card.id]
                    newList.removeLast()
                } else if (srCount != 0) {
                    newFlag = newSr && newList.any()
                    icon = yellowCover
                    cardImg = BufferedImage(icon.width, icon.height, icon.type)
                    g2d.color = SR.fontColor()
                    val str = srCount.toString()
                    srCount = 0
                    val strX = x + (80 - fm.stringWidth(str)) / 2
                    g2d.drawString(str, strX, y + 50)
                } else if (rCount != 0) {
                    newFlag = newR
                    icon = silverCover
                    cardImg = BufferedImage(icon.width, icon.height, icon.type)
                    g2d.color = R.fontColor()
                    val str = rCount.toString()
                    rCount = 0
                    val strX = x + (80 - fm.stringWidth(str)) / 2
                    g2d.drawString(str, strX, y + 50)
                } else {
                    if(newList.any()) {
                        card = newList.last()
                        newFlag = true
                        icon = card.rarity.icon()
                        srCount--
                        cardImg = CardImgPool[card.id]
                        newList.removeLast()
                    } else {
                        overFlag = true
                        break
                    }
                }

                //背景方块
                g2d.drawImage(icon, x, y, icon.width, icon.height, null)
                //实际内容
                g2d.drawImage(cardImg, x, y, icon.width, icon.height, null)
                if(card != null) {
                    val strX = x + (80 - fm.stringWidth(card.name)) / 2
                    g2d.drawCardName(card, strX, y + 100)
                }
                if(newFlag){
                    //首次获得显示new标识
                    g2d.color = Color.red
                    g2d.drawString("new!", x + 70, y + 90)
                }
            }
            if(overFlag) break
        }
        g2d.drawAvatar(name, avatarUrl)
        g2d.color = Color.red
        g2d.drawString("硬币花费总计：${totalCoin}", 63, 68)
        g2d.dispose()
        return image
    }

    private fun CardRarity.icon() = when(this){
        UR -> blueCover
        SSR-> colorCover
        SR -> yellowCover
        else-> silverCover
    }

    private fun CardRarity.fontColor() = when(this){
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

    /**
     * 绘制卡牌名称，绘制结束后不会把画笔颜色还原
     * @receiver Graphics2D
     * @param card Card 要绘制的卡牌
     * @param x Int
     * @param y Int
     */
    private fun Graphics2D.drawCardName(card: Card, x: Int, y:Int) {
        if(card.rarity == UR) {
            var charX = x
            for(char in card.name) {
                this.color = UR.fontColor()
                this.drawString(char.toString(), charX, y)
                charX += this.fontMetrics.charWidth(char)
            }
        } else {
            this.color = card.rarity.fontColor()
            this.drawString(card.name, x, y)
        }
    }

    /**
     * 用户抽到卡牌之后的处理，返回是否第一次获得该卡牌
     * @param qqId Long
     * @param card Card
     * @return Boolean
     */
    private fun getCardEvent(qqId : Long, card : Card): Boolean {
        //增加卡牌总计数
        card.addExistingAmount()
        //增加玩家背包内道具
        return CardBackpack.userGetNewCard(qqId, card.id)
    }
}