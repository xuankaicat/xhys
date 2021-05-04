package app.xuankai.xhys.mysql

class FoodBlackList : ObjectMysql {
    var id: Int = 0
    var eatStr : String = ""

    override fun add(varName: String, value: Any?) {
        when(varName){
            "id"->id= value as Int
            "eatStr"->eatStr = value as String
        }
    }
}