package app.xuankai.xhys.mysql

interface ObjectMysql {
    companion object{
        fun new():Any{
            return 0
        }
    }
    fun add(varName:String, value:Any?)
}